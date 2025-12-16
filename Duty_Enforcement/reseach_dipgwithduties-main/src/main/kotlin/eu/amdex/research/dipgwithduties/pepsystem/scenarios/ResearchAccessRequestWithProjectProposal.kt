package eu.amdex.research.dipgwithduties.pepsystem.scenarios

import eu.amdex.research.dipgwithduties.webinterface.SessionBean
import org.amdex.dossiers.dataclasses.DataDossier
import org.amdex.dossiers.session.ISession
import org.amdex.dossiers.templates.DataDossierTemplate
import org.amdex.dossiers.templates.IDataDossierTemplateCompanion
import org.amdex.features.duties.DutyStateMachine

class ResearchAccessRequestWithProjectProposal(dossier: DataDossier, session: ISession) : DataDossierTemplate(dossier,
    session
) {

    fun process() {
        /*
 1. Researcher initiates propose-project action - Submits research proposal to Executive Committee
  2. PEP requests a policy decision from eFLINT - "Can this researcher propose this project?"
  3. eFLINT checks researcher affiliation - Verifies researcher belongs to member institution
  4. eFLINT Returns decision with:
    - proposal(member, project) created
    - duty-complete-proposal(researcher, deadline: day+30)
  5. PEP registers duty in Duty Registry - Proposal completion duty in ACTIVE state
  6. PEP executes proposal-form plugin - Provides interface for proposal submission
  7. Researcher completes proposal details - Fills in research objectives, methods, data needs
  8. Plugin reports completion to PEP - Proposal form submitted
  9. PEP updates Duty Registry - duty-complete-proposal marked COMPLETED
  10. PEP informs eFLINT of completed proposal - Triggers next phase
  11. Researcher initiates submit-proposal action - Formally submits for review
  12. PEP requests submission from eFLINT - "Can proposal be submitted?"
  13. eFLINT updates proposal status - Changes from "draft" to "submitted"
  14. Ethics Board initiates review-ethics action - Begins ethical review
  15. PEP requests ethics review from eFLINT - "Can ethics board review this?"
  16. eFLINT validates review authority - Confirms ethics board can review
  17. eFLINT marks ethics-reviewed - Proposal passes ethical review
  18. Executive Committee initiates review-scientific action - Scientific merit review
  19. PEP requests scientific review from eFLINT - "Can EC do scientific review?"
  20. eFLINT validates and marks scientifically-reviewed - Proposal passes scientific review
  21. EC initiates approve-project action - Final approval decision
  22. PEP requests approval from eFLINT - "Can project be approved?"
  23. eFLINT approves project - Creates approved (project, member) fact
  24. EC initiates send-letter-of-approval action - Sends LoA to researcher
  25. PEP requests LoA sending from eFLINT - "Can LoA be sent?"
  26. eFLINT creates Letter of Approval duties:
  - letter-of-approval-sent(project, member)
  - data-sharing-agreement(project, member, "Terms")
  - duty-to-sign-agreement(member, EC, deadline: day+30)
  27. PEP registers signing duty in Duty Registry - Member must sign within 30 days
  28. PEP executes notification plugin - Notify LoA and agreement to researcher
  29. Researcher initiates sign-letter-of-approval action - Signs the
  agreement
  30. PEP requests signature acceptance from eFLINT - "Can researcher sign?"
  31. eFLINT accepts signature and creates duties:
  - duty-to-select-data(EC, member, deadline: day+14)
  - duty-to-send-data(EC, member, deadline: day+21)
  - duty-purpose-limitation(member, EC, "Research only")
  - duty-no-duplication(member, EC)
  - duty-data-security(member, EC, "Encrypted storage")
  32. PEP registers all duties in Duty Registry - Both EC and researcher duties
  33. PEP updates duty-to-sign-agreement - Marked COMPLETED
  34. EC initiates select-data action - Chooses appropriate datasets
  35. PEP requests data selection from eFLINT - "Can EC select these datasets?"
  36. eFLINT validates selection criteria if any
  37. eFLINT marks datasets as selected - selected(dataset1, project), selected(dataset2, project)
  38. PEP executes data-selection plugin - Prepares datasets for transfer
  39. Plugin confirms selection complete - Datasets ready
  40. PEP updates Duty Registry - duty-to-select-data marked COMPLETED when
  sufficient datasets selected
  41. Executive Committee initiates send-data action - Transfer selected data
  42. PEP requests data sending from eFLINT - "Can data be sent?"
  43. eFLINT validates all preconditions - LoA signed, data selected
  44. eFLINT creates data transfer records:
  - data-sent(dataset1, member, project)
  - data-sent(dataset2, member, project)
  - download-link-created(project, secure-url)
  45. eFLINT creates monitoring duties:
  - duty-progress-report(member, EC, deadline: day+90)
  - duty-deletion(member, EC, deadline: day+365)
  46. PEP registers monitoring duties in Duty Registry - Ongoing compliance duties
  47. PEP executes transfer plugin - Creates download link
  48. Plugin provides access credentials - Secure link sent to researcher
  49. PEP updates Duty Registry - duty-to-send-data marked COMPLETED
  50. PEP informs eFLINT of completed transfer - All initial duties fulfilled
  51. Researcher accesses data - Downloads through link

  The steps after this are to just complete the process and not necessarily for now
  52. Monitoring Service begins periodic checks - Monitors progress report deadline
  53. At day+90: Monitoring detects approaching deadline - Progress report due
  54. PEP executes reminder plugin - Notifies researcher of upcoming deadline
  55. Researcher initiates submit-progress-report action - Submits report
  56. PEP updates Duty Registry - duty-progress-report marked COMPLETED
  57. PEP informs eFLINT - Creates next duty-progress-report(deadline: day+180)
  58. At day+365: Monitoring detects deletion deadline - Data must be deleted
  59. Researcher initiates submit-deletion action - Confirms data deleted
  60. PEP updates Duty Registry - duty-deletion-attestation marked COMPLETED
  61. PEP informs eFLINT - All duties completed, project closed successfully
         */
    }

    companion object : IDataDossierTemplateCompanion{
        override fun create(
            dossier: DataDossier,
            parameters: Map<String, Any?>,
            session: ISession
        ): DataDossierTemplate {
            val duties = DutyStateMachine()

            return ResearchAccessRequestWithProjectProposal(dossier, session)
        }

    }

}