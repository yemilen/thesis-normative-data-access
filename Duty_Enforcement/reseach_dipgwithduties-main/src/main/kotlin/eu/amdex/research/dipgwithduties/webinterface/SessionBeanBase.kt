package eu.amdex.research.dipgwithduties.webinterface

import org.amdex.dossiers.dossierManager
import java.io.Serializable

open class SessionBeanBase : Serializable {
    protected open fun getDossierManager() = dossierManager
}
