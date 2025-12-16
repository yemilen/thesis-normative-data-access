package org.amdex.features.duties

import org.amdex.common.AMdEX
import org.amdex.common.util.Json
import org.amdex.dossiers.dataclasses.IDossier
import org.amdex.dossiers.dataclasses.IDossierFeature
import org.amdex.common.util.cast
import org.amdex.common.util.toPrettyJsonString
import org.amdex.dossiers.features.DataFeature
import org.amdex.dossiers.features.data
import kotlin.collections.mutableMapOf
import kotlin.text.set

private val dutiesFeatureManager by lazy { AMdEX.getImplementation<IDutiesFeatureManager>("dutiesFeatureManager") !! }

interface IDutiesFeatureManager {
    fun getDutiesByDossier(dossier: IDossier): DutyStateMachine
    fun deleteDuties(dossier: IDossier)
    fun saveDuties(dossier : IDossier, duties: DutyStateMachine)
}

class DutiesFeature(override val dossier: IDossier) : IDossierFeature<DutyStateMachine> {
    override val feature: String = "dutiesStateMachine"
    override val compute: () -> DutyStateMachine = {
        dutiesFeatureManager.getDutiesByDossier(dossier)
    }
    override val save: (Any) -> Unit = { duties ->
        if (duties !is DutyStateMachine) throw IllegalArgumentException()
        dutiesFeatureManager.saveDuties(dossier,cast(duties))
    }
}

// We use the data feature to store the duties state machine state

val IDossier.dutiesStateMachine: DutyStateMachine
    get() = cast(getFeatureValue(DutiesFeature(this)))