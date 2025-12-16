package eu.amdex.research.dipgwithduties.pepsystem.scenarios

import eu.amdex.research.dipgwithduties.dutiesmessaging.DutiesMessage
import eu.amdex.research.dipgwithduties.dutiesmessaging.DutiesMessageServer
import eu.amdex.research.dipgwithduties.dutiesmessaging.DutyStatus
import org.amdex.common.util.toPrettyJsonString
import org.amdex.dossiers.dataclasses.DataDossier
import org.amdex.dossiers.session.ISession
import org.amdex.dossiers.templates.IDataDossierTemplateCompanion
import org.amdex.features.duties.Duty
import org.amdex.features.duties.DutyStateMachineBuilder
import org.amdex.features.duties.dutiesStateMachine

class ExampleDutiesTemplate(dossier: DataDossier, session: ISession) : ADutiesDataDossierTemplate(dossier, session) {

    override val universe: String = """ 
            // Example scenario
            new_external_duty("example-duty", H, C).
            ?!created("example-duty", H, C).
            !?Holds(example-duty(H,C)).
            ?pending("example-duty", H, C).
    
            assign_external_duty("example-duty", H, C).
            ?created("example-duty", H, C).
            ?Holds(example-duty(H,C)).
            ?!pending("example-duty", H, C).
""".trimIndent()

//            external_duty_rejected("example-duty", H, C).
//            ?!created("example-duty", H, C).
//            ?violated("example-duty", H, C).
//            ?Violated(example-duty(H, C)).

//            external_duty_approved("example-duty", H, C).
//            ?!created("example-duty", H, C).
//            ?!Holds(example-duty(H,C)).
//            ?!violated("example-duty", H, C).
//            ?!Violated(example-duty(H, C)).
//            ?!pending("example-duty", H, C).
//            ?completed("example-duty", H, C).
//        """.trimIndent()

    override fun onDutyActivated(d: Duty) {
        println("Duty activated: ${d.name}")
        val msg = DutiesMessage(
            from = "orchestrator@dipg",
            to = d.actor,
            subject = d.name,
            body = "Please mark completed or violated",
            completedButton = "Completed",
            violatedButton = "Violated",
            status = DutyStatus.INFO,
            dutiesDossierUuid = dossier.uuid,
            dutyUuid = d.uuid
        )
        DutiesMessageServer.instance.send(msg)
    }

    companion object : IDataDossierTemplateCompanion {
        override fun create(
            dossier: DataDossier,
            parameters: Map<String, Any?>,
            session: ISession
        ): ADutiesDataDossierTemplate {
            val duties = DutyStateMachineBuilder {
                addNode("H", "example-duty")
            }
            dossier.dutiesStateMachine.loadStateMachine(duties.build())
            println("State Machine: " + dossier.dutiesStateMachine.toPrettyJsonString())
            val dutiesDataDossierTemplate = ExampleDutiesTemplate(dossier, session)
            dutiesDataDossierTemplate.updateDuties()

            dossier.save()
            return dutiesDataDossierTemplate
        }
    }
}