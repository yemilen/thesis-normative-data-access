package eu.amdex.research.dipgwithduties.dutiesmessaging

import kotlinx.serialization.Serializable
import org.amdex.common.util.TimeStamp
import org.amdex.common.util.UUIDString
import org.amdex.common.util.newUUIDString

@Serializable
enum class DutyStatus {
    INFO, COMPLETED, VIOLATED, ACTIVE
}

data class DutiesMessage(
    val uuid: UUIDString = newUUIDString(),
    val from: String,
    val to: String,
    val subject: String,
    val body: String,
    val datetime: TimeStamp = TimeStamp.NOW,
    val completedButton: String = "Completed",
    val violatedButton: String = "Violated",
    val status: DutyStatus = DutyStatus.ACTIVE,
    val dutiesDossierUuid: UUIDString,
    val dutyUuid: UUIDString,
)
