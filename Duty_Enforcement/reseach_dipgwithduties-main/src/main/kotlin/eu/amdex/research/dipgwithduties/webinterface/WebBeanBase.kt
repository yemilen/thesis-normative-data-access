package eu.amdex.research.dipgwithduties.webinterface

import jakarta.inject.Inject
import org.amdex.dossiers.dataclasses.MemberDossier
import org.amdex.dossiers.dossierManager
import org.amdex.dossiers.templates.TemplateLoader
import java.io.Serializable


open class WebBeanBase : Serializable {
    @Inject
    protected open lateinit var sessionBean: SessionBean

    protected open fun getDossierManager() = dossierManager
    protected open fun getTemplateLoader() = TemplateLoader

    protected open fun getMember(): MemberDossier = sessionBean.getMember()
    protected open fun getUsername(): String = sessionBean.username
}
