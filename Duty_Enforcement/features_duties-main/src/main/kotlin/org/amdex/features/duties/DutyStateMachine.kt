package org.amdex.features.duties

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.amdex.common.util.IStateMachineEvent
import org.amdex.common.util.IStateMachineNodeState
import org.amdex.common.util.StateMachine
import org.amdex.common.util.StateMachineNode
import org.amdex.common.util.newUUIDString

enum class DutyStateType : IStateMachineNodeState {
    CREATED, ACTIVE, PENDING, COMPLETED, VIOLATED, TERMINATED, UNKNOWN, ANY
}

data class DutyEvent(override val nodeName: String) : IStateMachineEvent

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonIgnoreProperties("states")
class Duty @JsonCreator constructor(
    @JsonProperty("actor") val actor: String,
    @JsonProperty("duty") val duty: String,
    @JsonProperty("uuid") uuid: String? = null,
    @JsonProperty("currentState") currentState: DutyStateType? = null
) : StateMachineNode<DutyEvent, DutyStateType>(
    uuid = uuid ?: newUUIDString(),
    name = "$actor#$duty",
    states = DutyStateType.entries,
    startState = currentState ?: DutyStateType.CREATED
)


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonSerialize(using = DutyStateMachineSerializer::class)
@JsonDeserialize(using = DutyStateMachineDeserializer::class)
class DutyStateMachine(nodes: Collection<Duty> = emptyList()) :
    StateMachine<DutyEvent, DutyStateType>(nodes) {

    fun getDuty(name: String): Duty = nodes[name] as Duty

    // ... existing code ...
    fun getDutiesInState(state: DutyStateType): Set<Duty> =
        nodes.values.filter { node -> node.currentState == state }.toSet() as Set<Duty>
}

