package eu.amdex.research.dipgwithduties.lifecycle

import org.amdex.dossiers.dataclasses.DataDossier
import org.amdex.dossiers.session.ISession
import org.amdex.dossiers.templates.DataDossierTemplate
import org.amdex.dossiers.templates.IDataDossierTemplateCompanion

class DutiesTemplate(dossier: DataDossier, session: ISession) : DataDossierTemplate(dossier, session) {

    companion object : IDataDossierTemplateCompanion {
        override fun create(
            dossier: DataDossier,
            parameters: Map<String, Any?>,
            session: ISession
        ): DataDossierTemplate {
            return DutiesTemplate(dossier, session)
        }
    }
}