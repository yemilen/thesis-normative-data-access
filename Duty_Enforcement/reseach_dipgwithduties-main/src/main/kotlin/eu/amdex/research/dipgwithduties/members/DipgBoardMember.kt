package eu.amdex.research.dipgwithduties.members

import eu.amdex.research.dipgwithduties.webinterface.SessionBean
import org.amdex.common.util.UUIDString
import org.amdex.common.util.dropIncluding
import org.amdex.dossiers.dataclasses.MemberDossier
import org.amdex.dossiers.features.data
import org.amdex.dossiers.session.ISession
import org.amdex.dossiers.templates.IMemberDossierTemplatePIP

class DipgBoardMember(override val dossier: MemberDossier, override val session: ISession) : IMemberDossierTemplatePIP {
    override val templates: Map<String, String>
    get() = dossier.header.templates
    override val templateNames: Set<String>
    get() = templates.keys
    override val dossierGroupUUID: UUIDString
    get() = dossier.header.dossierGroupUUID
    override val personalHierarchyName: String
    get() = dossier.header.personalHierarchyName
    override val uuid: UUIDString
    get() = dossier.uuid
    override val emailExtensions: List<String>
    get() = dossier.data["EmailExtensions"] as List<String>? ?: emptyList()

    override fun validateUser(email: String, password: String?): Boolean {
        return email.dropIncluding("@") in emailExtensions
    }
}