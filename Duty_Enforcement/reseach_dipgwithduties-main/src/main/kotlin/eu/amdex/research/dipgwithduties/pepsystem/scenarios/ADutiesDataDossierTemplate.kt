package eu.amdex.research.dipgwithduties.pepsystem.scenarios

import eu.amdex.research.dipgwithduties.policyengine.EFlintServerEvent
import eu.amdex.research.dipgwithduties.policyengine.EFlintSession
import org.amdex.common.util.DossierMethod
import org.amdex.common.util.toPrettyJsonString
import org.amdex.dossiers.dataclasses.DataDossier
import org.amdex.dossiers.session.ISession
import org.amdex.dossiers.templates.DataDossierTemplate
import org.amdex.features.duties.Duty
import org.amdex.features.duties.DutyStateType
import org.amdex.features.duties.dutiesStateMachine

data class SyncSummary(
    val active: Set<Pair<String, String>>,
    val pending: Set<Pair<String, String>>,
    val completed: Set<Pair<String, String>>,
    val violated: Set<Pair<String, String>>
)

abstract class ADutiesDataDossierTemplate(dossier: DataDossier, session: ISession) :
    DataDossierTemplate(dossier, session) {

    abstract val universe: String

    abstract fun onDutyActivated(d: Duty)

    /** Resolve a duty node from an incoming external UUID. Override in concrete templates if needed. */
    protected open fun resolveDutyByUuid(dutyUuid: String): Duty? {
        // Default: treat UUID as the node name (actor#duty)
        return dossier.dutiesStateMachine.getNodeByUuid(dutyUuid) as? Duty
    }

    /** Mark a duty as COMPLETED (approved) and sync to eFlint. */
    fun dutyCompleted(dutyUuid: String): SyncSummary {
        val duty = resolveDutyByUuid(dutyUuid)
            ?: error("Duty for uuid '$dutyUuid' not found")
        // idempotent: only transition when needed
        if (duty.currentState != DutyStateType.COMPLETED) {
            duty.transition(DutyStateType.COMPLETED)
        }
        return updateDuties() // pushes external_duty_approved(...) and re-pulls state
    }

    /** Mark a duty as VIOLATED (rejected) and sync to eFlint. */
    fun dutyViolated(dutyUuid: String): SyncSummary {
        val duty = resolveDutyByUuid(dutyUuid)
            ?: error("Duty for uuid '$dutyUuid' not found")
        if (duty.currentState != DutyStateType.VIOLATED) {
            duty.transition(DutyStateType.VIOLATED)
        }
        return updateDuties() // pushes external_duty_rejected(...) and re-pulls state
    }

    @DossierMethod
    fun updateDuties(): SyncSummary {
        fun atom(x: String) = x.trim().replace("""[^\w]""".toRegex(), "_")

        val push = StringBuilder(model).apply { appendLine(universe) }

        dossier.dutiesStateMachine.getDutiesInState(DutyStateType.CREATED).forEach { d ->
            push.appendLine("assign_external_duty(${atom(d.duty)}, ${atom(d.actor)}, \"C\")")
        }
        dossier.dutiesStateMachine.getDutiesInState(DutyStateType.COMPLETED).forEach { d ->
            push.appendLine("external_duty_approved(${atom(d.duty)}, ${atom(d.actor)}, \"C\")")
        }
        dossier.dutiesStateMachine.getDutiesInState(DutyStateType.VIOLATED).forEach { d ->
            push.appendLine("external_duty_rejected(${atom(d.duty)}, ${atom(d.actor)}, \"C\")")
        }

        var syncSummary: SyncSummary? = null
        EFlintSession.createForModel(push.toString()) { session, event, data ->
            println("Event: $event, data: $data")
            if (event == EFlintServerEvent.STOPPED && syncSummary == null) syncSummary =
                SyncSummary(emptySet(), emptySet(), emptySet(), emptySet())
        }.use { eFlintSession ->
            eFlintSession.communicate("?--pending(duty, actor1, actor2).")
            eFlintSession.communicate("?--created(duty, actor1, actor2).")
            eFlintSession.communicate("?--violated(duty, actor1, actor2).")
            val resComletedFull = eFlintSession.communicate("?--completed(duty, actor1, actor2).")

            val pPending: Set<Pair<String, String>> =
                resComletedFull.filterResult("pending").also { println("pending: " + it.toPrettyJsonString()) }

            val pApproved: Set<Pair<String, String>> =
                resComletedFull.filterResult("completed").also { println("completed: " + it.toPrettyJsonString()) }

            val pActive: Set<Pair<String, String>> =
                resComletedFull.filterResult("created").also { println("created: " + it.toPrettyJsonString()) }

            val pViolated: Set<Pair<String, String>> =
                resComletedFull.filterResult("violated").also { println("violated: " + it.toPrettyJsonString()) }

            fun ensure(actor: String, duty: String): Duty {
                val name = "$actor#$duty"
                return (dossier.dutiesStateMachine.getNode(name) as? Duty)
                    ?: Duty(actor, duty).also { dossier.dutiesStateMachine.addNode(it) }
            }
            (pPending + pApproved + pViolated + pActive).forEach { (a, r) -> ensure(a, r) }

            // --- Transition with hook: CREATED -> ACTIVE fires once ---
            dossier.dutiesStateMachine.getNodes()
                .map { it as Duty }
                .forEach { node ->
                    val old = node.currentState
                    val key = node.actor to node.duty
                    val new = when (key) {
                        in pViolated -> DutyStateType.VIOLATED
                        in pApproved -> DutyStateType.COMPLETED
                        in pPending -> DutyStateType.PENDING
                        in pActive -> DutyStateType.ACTIVE
                        else -> if (old != DutyStateType.CREATED) DutyStateType.TERMINATED else old
                    }
                    if (new != old) {
                        node.transition(new)
                        if (old == DutyStateType.CREATED && new == DutyStateType.ACTIVE) {
                            onDutyActivated(node) // 🔔 fire action
                        }
                    }
                }

            // --- save the updated dossier ---
            dossier.save()
            syncSummary = SyncSummary(
                active = pActive - pApproved - pViolated,
                pending = pPending,
                completed = pApproved,
                violated = pViolated
            )
            println("Sync summary: $syncSummary")
        }
        return syncSummary!!
    }


    val model = """
            // representation of external duties
            Fact external_duty Identified by String
            Placeholder duty For external_duty
            
            // which may be in various states
            Fact created 	  Identified by duty * actor1 * actor2
            Fact completed  Identified by duty * actor1 * actor2
            Fact violated	  Identified by duty * actor1 * actor2
            Fact pending    Identified by duty * actor1 * actor2
            
            // and transition between states by triggering these events:
            Event new_external_duty Related to duty, actor1, actor2
              Creates pending(), duty, actor1, actor2
            
            Event assign_external_duty Related to duty, actor1, actor2
              Creates created()
              Terminates completed(), violated(), pending()
              
            Event external_duty_approved Related to duty, actor1, actor2
              Creates completed()
              Terminates created(), violated(), pending()
              
            Event external_duty_rejected Related to duty, actor1, actor2
              Creates violated()
              Terminates completed(), created(), pending()
              
            // An example specification of a duty and how it would respond to its external status 
            Duty example-duty Holder actor1 Claimant actor2
              Conditioned by Not(completed(duty="example-duty"))
              Holds when created(duty="example-duty")
                        ,violated(duty="example-duty")
              Violated when violated(duty="example-duty")
            Extend Event new_external_duty 
              Creates example-duty() When external_duty == "example-duty".
            
        
        """.trimIndent()
}