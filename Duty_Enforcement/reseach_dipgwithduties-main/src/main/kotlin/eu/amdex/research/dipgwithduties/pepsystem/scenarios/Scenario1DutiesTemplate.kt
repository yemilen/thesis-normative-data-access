package eu.amdex.research.dipgwithduties.pepsystem.scenarios

import eu.amdex.research.dipgwithduties.dutiesmessaging.DutiesMessage
import eu.amdex.research.dipgwithduties.dutiesmessaging.DutiesMessageServer
import eu.amdex.research.dipgwithduties.dutiesmessaging.DutyStatus
import org.amdex.dossiers.dataclasses.DataDossier
import org.amdex.dossiers.session.ISession
import org.amdex.dossiers.templates.IDataDossierTemplateCompanion
import org.amdex.features.duties.Duty
import org.amdex.features.duties.DutyStateMachineBuilder
import org.amdex.features.duties.dutiesStateMachine

class Scenario1DutiesTemplate(
    dossier: DataDossier,
    session: ISession
) : ADutiesDataDossierTemplate(dossier, session) {

    override val universe: String = """
        // ===== Scenario 1 universe (DEX DIPG) =====
        // #require "access_union.eflint"   // assumed loaded by engine

        // Actors (mapping: HospitalA -> hospital, DIPGResearch -> research)
        +actor(hospital)
        +actor(research)

        // Hospital & staff
        +member(hospital).
        +affiliated-with(John, hospital).

        // Donors
        +donor(Alice).
        +donor(Bob).

        // Dataset
        +dataset(X1).
        +accurate-for-purpose(X1, research).
        +coded(X1).
        +subject-of(Alice, X1).
        +subject-of(Bob, X1).

        // External duties for consent (no assign act needed)
        +external_duty(give_consent_Alice)
        +external_duty(give_consent_Bob)

        // Duty instances exist directly via the base fact
        +approve_external_duty(hospital, give_consent_Alice)
        +approve_external_duty(hospital, give_consent_Bob)

        // When an external duty is approved, assert the original consent fact
        rule r_consent_Alice:
          approved_external_duty(hospital, give_consent_Alice) -> +give-consent(Alice, hospital, research)

        rule r_consent_Bob:
          approved_external_duty(hospital, give_consent_Bob) -> +give-consent(Bob, hospital, research)

        // The original scenario query (left here for reference/testing)
        ?Enabled(write(John, X1)).
    """.trimIndent()

    override fun onDutyActivated(d: Duty) {
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
                addNode("give_consent_Alice", "hospital")
                addNode("give_consent_Bob",   "hospital")
            }.build()

            dossier.dutiesStateMachine.loadStateMachine(duties)
            dossier.save()

            return Scenario1DutiesTemplate(dossier, session).also { it.updateDuties() }
        }
    }
}
