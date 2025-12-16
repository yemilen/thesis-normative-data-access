package eu.amdex.research.dipgwithduties.pepsystem.scenarios

import eu.amdex.research.dipgwithduties.webinterface.SessionBean
import org.amdex.common.util.toPrettyJsonString
import org.amdex.dossiers.dataclasses.DataDossier
import org.amdex.dossiers.session.ISession
import org.amdex.dossiers.templates.DataDossierTemplate
import org.amdex.dossiers.templates.IDataDossierTemplateCompanion
import org.amdex.features.duties.DutyStateMachine
import org.amdex.features.duties.DutyStateType

class DataContributionLifeCycle(dossier: DataDossier, session: ISession) : DataDossierTemplate(dossier, session) {

    fun process() {
        /*
1. Member initiates prepare-dataset action - Institution requests to contribute dataset to registry
2. PEP requests policy decision from eFLINT - "Can this member prepare dataset for contribution?"
3. eFLINT evaluates member authorization - Checks if member is authorized institution
4. eFLINT creates validation duties - Returns decision with duties:
    - duty-check-consent(dataset, deadline: day+7)
5. PEP registers duties in Duty Registry - duty created in ACTIVE state
6. PEP executes consent-check plugin - Could be:
    - Notification plugin: emails member to upload consent documents
    - API plugin: queries institutional consent management system
7. Member provides consent documentation - Through whatever mechanism the
  plugin requires
8. Plugin reports completion to PEP - Consent check successful
9. PEP updates Duty Registry - duty-check-consent marked COMPLETED
10. PEP detects all validation duties completed - Monitoring service or PEP checks duty states
11. PEP informs eFLINT of completed validations - "All validation duties for dataset X completed"
12. eFLINT automatically enables make-data-available - Since all preconditions are met
13. eFLINT transfers ownership to DCOG - Updates owner-of fact automatically
14. eFLINT creates post-transfer duties:
  - duty-verify-transfer(DCOG, deadline: day+1)
  - duty-establish-retention(DCOG, deadline: day+30)
  - duty-log-contribution(DCOG, deadline: day+1)
15. PEP registers new duties in Duty Registry - Post-transfer duties
16. PEP executes transfer-verification plugin - Confirms DCOG received data
17. Plugin reports successful transfer - Data integrity verified
18. PEP updates Duty Registry - duty-verify-transfer marked COMPLETED
19. PEP executes retention-setup plugin - Configures 1-year retention policy
20. Plugin confirms retention established - Retention rules applied
21. PEP updates Duty Registry - duty-establish-retention marked COMPLETED
22. PEP executes audit-log plugin - Records complete contribution activity
23. Plugin confirms logging complete - Audit entry created
24. PEP updates Duty Registry - duty-log-contribution marked COMPLETED
25. PEP informs eFLINT of completion - All post-transfer duties fulfilled
26Contribution process complete - Dataset successfully contributed to DCOG registry
         */


    }


    companion object : IDataDossierTemplateCompanion{
        override fun create(
            dossier: DataDossier,
            parameters: Map<String, Any?>,
            session: ISession
        ): DataDossierTemplate {
            println("Creating DataContributionLifeCycle template")
            println("Parameters: ${parameters.toPrettyJsonString()}")
            println("Member: ${session.toPrettyJsonString()}")
            //            val duties = DutyStateMachine()
//            duties.addNode()
//
//            val activeDuties = duties.getDutyInState(DutyStateType.ACTIVE)

            return DataContributionLifeCycle(dossier, session)
        }
    }

}