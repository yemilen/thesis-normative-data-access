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

class Scenario2DutiesTemplate(
    dossier: DataDossier,
    session: ISession
) : ADutiesDataDossierTemplate(dossier, session) {

    override val universe: String = """
        // ===== Scenario 2 universe (DEX DIPG) =====
        // #require "access_union.eflint"   // assumed loaded by engine

        // Actors (mapping): HospitalB -> hospital, DIPGBoard -> dipgboard, DIPG -> research
        +actor(hospital)
        +actor(dipgboard)
        +actor(research)

        // 1) hospital & staff
        +member(hospital).
        +affiliated-with(Eve, hospital).

        // 2) project P1 by Y is proposed and then (externally) approved/signed
        +project(P1).
        propose-project(hospital, dipgboard, P1).

        // External duties (no assign act; base facts instantiate duties)
        +external_duty(approve_project_P1)
        +external_duty(send_letter_approval_P1)
        +external_duty(sign_letter_approval_P1)
        +external_duty(select_data_P1_X1)

        // Who must act (holder → duty)
        +approve_external_duty(dipgboard, approve_project_P1)
        +approve_external_duty(research,  send_letter_approval_P1)
        +approve_external_duty(hospital,  sign_letter_approval_P1)
        +approve_external_duty(dipgboard, select_data_P1_X1)

        // When an external duty is approved, assert the original scenario facts
        rule r_approve_project_P1:
          approved_external_duty(dipgboard, approve_project_P1) -> +approve-project(dipgboard, hospital, P1)

        rule r_send_letter_of_approval_P1:
          approved_external_duty(research, send_letter_approval_P1) -> +send-letter-of-approval(research, hospital, P1)

        rule r_sign_letter_of_approval_P1:
          approved_external_duty(hospital, sign_letter_approval_P1) -> +sign-letter-of-approval(hospital, dipgboard, P1)

        // 2b) dataset selection (external duty on DIPGBoard)
        +dataset(X1).
        rule r_select_data_P1_X1:
          approved_external_duty(dipgboard, select_data_P1_X1) -> +select-data(dipgboard, hospital, P1, X1)

        // 3) final capability query from the original scenario
        ?Enabled(read(Eve, X1)).
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
            val dutiesSm = DutyStateMachineBuilder {
                addNode("approve_project_P1",     "dipgboard")
                addNode("send_letter_approval_P1", "research")
                addNode("sign_letter_approval_P1", "hospital")
                addNode("select_data_P1_X1",      "dipgboard")
            }.build()

            dossier.dutiesStateMachine.loadStateMachine(dutiesSm)
            dossier.save()

            return Scenario2DutiesTemplate(dossier, session).also { it.updateDuties() }
        }
    }
}
