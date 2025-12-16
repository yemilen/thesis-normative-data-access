package eu.amdex.research.dipgwithduties.policyengine

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EngineResponse(
    @SerialName("all-disabled-transitions")
    val allDisabledTransitions: List<FactOrTransition> = emptyList(),

    @SerialName("all-duties")
    val allDuties: List<FactOrTransition> = emptyList(),

    @SerialName("all-enabled-transitions")
    val allEnabledTransitions: List<FactOrTransition> = emptyList(),

    @SerialName("created_facts")
    val createdFacts: List<FactOrTransition> = emptyList(),

    val errors: List<String> = emptyList(),

    @SerialName("inst-query-results")
    val instQueryResults: List<FactOrTransition> = emptyList(),

    @SerialName("new-disabled-transitions")
    val newDisabledTransitions: List<FactOrTransition> = emptyList(),

    @SerialName("new-duties")
    val newDuties: List<FactOrTransition> = emptyList(),

    @SerialName("new-enabled-transitions")
    val newEnabledTransitions: List<FactOrTransition> = emptyList(),

    @SerialName("new-state")
    val newState: Int,

    @SerialName("old-state")
    val oldState: Int,

    @SerialName("output-events")
    val outputEvents: List<FactOrTransition> = emptyList(),

    @SerialName("query-results")
    val queryResults: List<FactOrTransition> = emptyList(),

    val response: String,

    @SerialName("source_contents")
    val sourceContents: List<FactOrTransition> = emptyList(),

    @SerialName("target_contents")
    val targetContents: List<FactOrTransition> = emptyList(),

    @SerialName("terminated-duties")
    val terminatedDuties: List<FactOrTransition> = emptyList(),

    @SerialName("terminated_facts")
    val terminatedFacts: List<FactOrTransition> = emptyList(),

    val violations: List<ViolationFactOrTransition> = emptyList()
) {
    fun filterResult(factType: String) : Set<Pair<String, String>>{
        return targetContents
            .filter { it.factType == factType }
            .mapNotNull { it.toActorDutyPair() }
            .toSet()
    }

    fun transitionsWithFactType(
        factType: String
    ): Set<Pair<String, String>> {
        // You can extend this union if other lists contain relevant transitions
        val allTransitions = sequenceOf(
            allDisabledTransitions,
            allEnabledTransitions,
            newDisabledTransitions,
            newEnabledTransitions,
            createdFacts,
            terminatedFacts,
            outputEvents
        ).flatten()

        return allTransitions
            .filter { it.factType == factType }
            .mapNotNull { it.toActorDutyPair() }
            .toSet()
    }
}



/**
 * Represents both:
 *  - a simple fact with value
 *  - a “product”/transition with arguments
 */
@Serializable
data class FactOrTransition(
    @SerialName("fact-type")
    val factType: String,

    @SerialName("tagged-type")
    val taggedType: String,

    val textual: String,

    // Present for simple facts like actor("C") and external_duty("example-duty")
    val value: String? = null,

    // Present for product terms like assign_external_duty(...),
    // external_duty_approved(...), completed(...), etc.
    val arguments: List<FactOrTransition>? = null
) {
    fun toActorDutyPair(): Pair<String, String>? {
        // We expect: functor(external_duty("..."), actor("..."), actor("..."))
        val dutyArg = arguments?.getOrNull(0)
        val actorArg = arguments?.getOrNull(1)

        val duty = dutyArg?.value ?: return null
        val actor = actorArg?.value ?: return null

        return actor to duty
    }
}

@Serializable
data class ViolationFactOrTransition(
    val value: FactOrTransition? = null,
    val violation : String? = null
) {
    fun toActorDutyPair(): Pair<String, String>? = value?.toActorDutyPair()
}