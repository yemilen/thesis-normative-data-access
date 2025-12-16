package eu.amdex.research.dipgwithduties.config

import eu.amdex.research.dipgwithduties.lifecycle.LifecycleTemplate
import eu.amdex.research.dipgwithduties.members.DipgBoardMember
import eu.amdex.research.dipgwithduties.members.HospitalMember
import eu.amdex.research.dipgwithduties.members.ResearchMember
import jakarta.annotation.Resource
import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener
import jakarta.servlet.annotation.WebListener
import kotlinx.serialization.Serializable
import org.amdex.common.AMdEX
import org.amdex.common.util.PostgresDialectH2DataSource
import org.amdex.common.util.ignore
import org.amdex.common.util.toPrettyJsonString
import org.amdex.dossiers.dataclasses.*
import org.amdex.dossiers.defaultimpl.DossierManager
import org.amdex.dossiers.defaultimpl.GenericFeatureManager
import org.amdex.dossiers.defaultimpl.IntegratedDataFeatureStore
import org.amdex.dossiers.dossierManager
import org.amdex.dossiers.features.data
import org.amdex.dossiers.features.dossierLinks
import org.amdex.dossiers.templates.DataDossierGroupTemplate
import org.amdex.dossiers.templates.DefaultMemberTemplate
import org.amdex.features.duties.DutyStateMachine
import org.amdex.features.duties.IDutiesFeatureManager
import java.sql.SQLSyntaxErrorException
import javax.sql.DataSource

@WebListener
class ApplicationInitializer : ServletContextListener {
    @Resource(name = "DossierStoreDB")
    private lateinit var dossierStoreDB: DataSource
    private val postgresDialectH2DataSource by lazy {PostgresDialectH2DataSource(dossierStoreDB)}

    override fun contextInitialized(sce: ServletContextEvent?) {
        AMdEX.registerDataSource("DossierStoreDB", postgresDialectH2DataSource)
        AMdEX.registerDataSource("NotaryDB", postgresDialectH2DataSource)
        AMdEX.registerDataSource("PoliciesDB", postgresDialectH2DataSource)

        AMdEX.implementations["dossierManager"] = DossierManager()
        AMdEX.implementations["dataFeatureManager"] = IntegratedDataFeatureStore()
        AMdEX.implementations["dutiesFeatureManager"] = object : IDutiesFeatureManager {
            private val genericFeatureManager = GenericFeatureManager(dossierStoreDB)
            override fun getDutiesByDossier(dossier: IDossier): DutyStateMachine {
                val data = genericFeatureManager.getDataByDossier(dossier, "dutiesFeatureManager") ?: DutyStateMachine()
                println(data::class.qualifiedName)
                println(data.toPrettyJsonString())
                return data as DutyStateMachine
            }

            override fun deleteDuties(dossier: IDossier) = genericFeatureManager.deleteData(dossier, "dutiesFeatureManager")

            override fun saveDuties(
                dossier: IDossier,
                duties: DutyStateMachine
            ) = genericFeatureManager.saveData(dossier, "dutiesFeatureManager", duties)
        }

        initializeSystem()
    }

    private fun initializeSystem() {
        ignore(SQLSyntaxErrorException::class) { if (dossierManager.getDossierGroupByUUID("ROOT") != null) return }

        dossierManager.createTables()

        // Initialize root group and root owner
        val pepMember = MemberDossier(
            MemberDossierHeader(
                templateClass = DefaultMemberTemplate::class.qualifiedName!!,
                organisationIdentifier = "PEP",
                dossierGroupUUID = "ROOT",
                uuid = "PEP",
            )
        )
        dossierManager.saveMemberDossier(pepMember)

        val rootGroup = DataDossierGroupDossier(
            DataDossierGroupDossierHeader(
                templateClass = DataDossierGroupTemplate::class.qualifiedName!!,
                owningMemberUUID = "PEP",
                parentUUID = null,
                uuid = "ROOT",
                name = "ROOT"
            )
        )
        dossierManager.saveDataDossierGroupDossier(rootGroup)

        // Initialize Lifecycle Dossiers
        val lifecycleDossiers = listOf(
            DataDossierGroupDossier(
                DataDossierGroupDossierHeader(
                    templateClass = LifecycleTemplate::class.qualifiedName!!,
                    owningMemberUUID = "PEP",
                    parentUUID = "ROOT",
                    name = "Contribute Data",
                )
            ).also { dossier ->
                dossier.dossierLinks.addReverseLink("ROOT", DossierType.DossierGroup, "Contribute Data")
            },
            DataDossierGroupDossier(
                DataDossierGroupDossierHeader(
                    templateClass = LifecycleTemplate::class.qualifiedName!!,
                    owningMemberUUID = "PEP",
                    parentUUID = "ROOT",
                    name = "Request Access",
                )
            ).also { dossier ->
                dossier.dossierLinks.addReverseLink("ROOT", DossierType.DossierGroup, "Request Access")
            },
            DataDossierGroupDossier(
                DataDossierGroupDossierHeader(
                    templateClass = LifecycleTemplate::class.qualifiedName!!,
                    owningMemberUUID = "PEP",
                    parentUUID = "ROOT",
                    name = "Select Data",
                )
            ).also { dossier ->
                dossier.dossierLinks.addReverseLink("ROOT", DossierType.DossierGroup, "Select Data")
            }
        )

        // Initialize members
        val members = listOf(
            MemberDossier(
                MemberDossierHeader(
                    templateClass = HospitalMember::class.qualifiedName!!,
                    organisationIdentifier = "Hospital 1",
                    dossierGroupUUID = "ROOT",
                )
            ).also { dossier ->
                dossier.data["EmailExtensions"] = listOf("hospital1")
                dossier.data["admins"] = listOf("admin@hospital1")
                dossier.data["members"] = listOf("admin@hospital1", "alice@hospital1", "bob@hospital1")
                dossier.dossierLinks.addReverseLink("ROOT", DossierType.DossierGroup, "hospital")
            },
            MemberDossier(
                MemberDossierHeader(
                    templateClass = HospitalMember::class.qualifiedName!!,
                    organisationIdentifier = "Hospital 2",
                    dossierGroupUUID = "ROOT",
                )
            ).also { dossier ->
                dossier.data["EmailExtensions"] = listOf("Hospital2")
                dossier.data["admins"] = listOf("admin@Hospital2")
                dossier.data["members"] = listOf("admin@Hospital2", "carol@Hospital2", "dave@Hospital2")
                dossier.dossierLinks.addReverseLink("ROOT", DossierType.DossierGroup, "hospital")
            },
            MemberDossier(
                MemberDossierHeader(
                    templateClass = ResearchMember::class.qualifiedName!!,
                    organisationIdentifier = "Research Institute",
                    dossierGroupUUID = "ROOT",
                )
            ).also { dossier ->
                dossier.data["EmailExtensions"] = listOf("research")
                dossier.data["members"] = listOf("admin@research", "emma@research", "frank@research")
                dossier.dossierLinks.addReverseLink("ROOT", DossierType.DossierGroup, "research")
            },
            MemberDossier(
                MemberDossierHeader(
                    templateClass = DipgBoardMember::class.qualifiedName!!,
                    organisationIdentifier = "DIPG Board",
                    dossierGroupUUID = "ROOT",
                )
            ).also { dossier ->
                dossier.data["EmailExtensions"] = listOf("dipgboard")
                dossier.data["members"] = listOf("admin@dipgboard", "grace@dipgboard", "henry@dipgboard")
                dossier.dossierLinks.addReverseLink("ROOT", DossierType.DossierGroup, "dipgboard")
            }
        )
        members.forEach { member ->
            dossierManager.saveMemberDossier(member)
        }

    }
}