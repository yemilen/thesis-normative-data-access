package eu.amdex.research.dipgwithduties.lifecycle

import eu.amdex.research.dipgwithduties.pepsystem.pluginmanager.PluginManager
import eu.amdex.research.dipgwithduties.policyengine.PolicyEngineManager
import eu.amdex.research.dipgwithduties.webinterface.SessionBean
import jakarta.ws.rs.core.Response
import org.amdex.common.util.DossierMethod
import org.amdex.common.util.cast
import org.amdex.dossiers.dataclasses.DataDossier
import org.amdex.dossiers.dataclasses.DataDossierGroupDossier
import org.amdex.dossiers.dataclasses.DataDossierHeader
import org.amdex.dossiers.features.data
import org.amdex.dossiers.session.ISession
import org.amdex.dossiers.templates.DataDossierGroupTemplate
import org.amdex.dossiers.templates.IDataDossierGroupDossierTemplateCompanion
import org.amdex.features.duties.Duty
import org.amdex.features.duties.DutyStateType
import org.amdex.features.duties.dutiesStateMachine

class LifecycleTemplate(dossier: DataDossierGroupDossier, session: ISession) : DataDossierGroupTemplate(
    dossier,
    session
) {
    @DossierMethod
    fun startLifecycle(parameters: Map<String, Any>): Response {
        val checkPolicy: String = cast(dossier.data["Lifecycle"])
        // Eflint check with checkPolicy and parameters
        val eFlintResult = PolicyEngineManager.executePolicy(checkPolicy, parameters)
        if (eFlintResult.first) {
            return Response.ok("FAILURE").build()
        } else {
            //Create Duties Dossier
            val dutiesDossier = DataDossier(
                DataDossierHeader(
                    templateClass = DutiesTemplate::class.qualifiedName!!,
                    ownerUUID = "PEP",
                    dossierGroupUUID = uuid,
                )
            )
            dutiesDossier.data["LifecycleId"] = dossier.data["LifecycleId"]
            // Initialize dutiesStateMachine
            eFlintResult.second?.let { dutiesData ->
                dutiesData.duties.forEach { duty ->
                    dutiesDossier.dutiesStateMachine.addNode(duty)
                }
                dutiesDossier.dutiesStateMachine.getDutiesInState(DutyStateType.ACTIVE).forEach { duty ->
                    PluginManager.dutyTransition(duty as Duty)
                }
            }
            dutiesDossier.save()

            return Response.ok("SUCCESS").build()
        }
    }

    companion object : IDataDossierGroupDossierTemplateCompanion {
        override fun create(
            dossier: DataDossierGroupDossier,
            parameters: Map<String, Any?>,
            session: ISession
        ): DataDossierGroupTemplate {
            dossier.data["LifecycleId"] =
                parameters["lifecycleId"] as String? ?: throw IllegalArgumentException("lifecycleId not found")
            dossier.save()
            return LifecycleTemplate(dossier, session)
        }
    }
}