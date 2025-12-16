package eu.amdex.research.dipgwithduties.dutiesmessaging

import org.amdex.common.AMdEX
import org.amdex.common.util.TimeStamp
import org.amdex.common.util.UUIDString
import org.amdex.common.util.newUUIDString

class DutiesMessageServer private constructor() {
    private val dataSource = AMdEX.getDataSource("DossierStoreDB")
    private var connection = dataSource?.connection

    init {
        try {
            if (createTables())
                insertSampleData()

        } catch (e: Exception) {
            throw RuntimeException("Failed to initialize database", e)
        }
    }

    private fun createTables(): Boolean {
        val tableExists = connection?.createStatement()?.executeQuery(
            """
            SELECT COUNT(*) 
            FROM INFORMATION_SCHEMA.TABLES 
            WHERE TABLE_NAME = 'DUTY_MESSAGES'
            """
        )?.use { rs ->
            rs.next()
            rs.getInt(1) > 0
        } ?: false

        if (!tableExists) {
            connection?.createStatement()?.execute(
                """
                CREATE TABLE IF NOT EXISTS duty_messages (
                    uuid VARCHAR(50) PRIMARY KEY,
                    sender VARCHAR(255),
                    recipient VARCHAR(255),
                    subject VARCHAR(255),
                    body TEXT,
                    datetime TIMESTAMP,
                    completed_button VARCHAR(255),
                    violated_button VARCHAR(255),
                    status VARCHAR(50),
                    duties_dossier_uuid VARCHAR(50),
                    duty_uuid VARCHAR(50)
                )
            """
            )
        }
        return !tableExists
    }

    private fun insertSampleData() {
        val sampleMessages = listOf(
            DutiesMessage(
                from = "alice@example.com",
                to = "bob@example.com",
                subject = "Test Duty 1",
                body = "Please complete this duty",
                completedButton = "Complete",
                violatedButton = "Violated",
                status = DutyStatus.ACTIVE,
                dutiesDossierUuid = newUUIDString(),
                dutyUuid = newUUIDString()
            ),
            DutiesMessage(
                from = "bob@example.com",
                to = "alice@example.com",
                subject = "Test Duty 2",
                body = "Another test duty",
                completedButton = "Done",
                violatedButton = "Failed",
                status = DutyStatus.INFO,
                dutiesDossierUuid = newUUIDString(),
                dutyUuid = newUUIDString()
            )
        )

        sampleMessages.forEach { send(it) }
    }

    fun close() {
        connection?.close()
    }

    fun getUsers(): Set<String> {
        return try {
            val allUsers = mutableSetOf<String>()
            connection?.createStatement()?.executeQuery(
                """
                SELECT DISTINCT recipient as user FROM duty_messages
                UNION
                SELECT DISTINCT sender as user FROM duty_messages
                """
            )?.use { rs ->
                while (rs.next()) {
                    allUsers.add(rs.getString("user"))
                }
            }
            allUsers
        } catch (e: java.sql.SQLException) {
            throw RuntimeException("Failed to get users", e)
        }
    }

    fun send(dutiesMessage: DutiesMessage) {
        try {
            connection?.prepareStatement(
                "INSERT INTO duty_messages (sender, recipient, subject, body, datetime, completed_button, violated_button, status, uuid, duties_dossier_uuid, duty_uuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            )?.use { stmt ->
                stmt.setString(1, dutiesMessage.from)
                stmt.setString(2, dutiesMessage.to)
                stmt.setString(3, dutiesMessage.subject)
                stmt.setString(4, dutiesMessage.body)
                stmt.setTimestamp(5, dutiesMessage.datetime.sqlTimestamp)
                stmt.setString(6, dutiesMessage.completedButton)
                stmt.setString(7, dutiesMessage.violatedButton)
                stmt.setString(8, dutiesMessage.status.toString())
                stmt.setString(9, dutiesMessage.uuid)
                stmt.setString(10, dutiesMessage.dutiesDossierUuid)
                stmt.setString(11, dutiesMessage.dutyUuid)
                stmt.executeUpdate()
            }
        } catch (e: java.sql.SQLException) {
            throw RuntimeException("Failed to send email", e)
        }
    }

    fun getDutyMessage(uuid: UUIDString): DutiesMessage? {
        try {
            connection?.prepareStatement("SELECT * FROM duty_messages WHERE uuid = ?")?.use { stmt ->
                stmt.setString(1, uuid)
                stmt.executeQuery().use { rs ->
                    if (rs.next()) {
                        return rsToDutiesMessage(rs)
                    }
                }
            }
            return null
        } catch (e: java.sql.SQLException) {
            throw RuntimeException("Failed to get message", e)
        }
    }

    fun listDutyMessages(): List<DutiesMessage> {
        val dutiesMessages = mutableListOf<DutiesMessage>()
        connection?.createStatement()?.executeQuery("SELECT * FROM duty_messages")?.use { rs ->
            while (rs.next()) {
                dutiesMessages.add(rsToDutiesMessage(rs))
            }
        }
        return dutiesMessages
    }

    fun listDutyMessagesTo(to: String): List<DutiesMessage> {
        try {
            val dutiesMessages = mutableListOf<DutiesMessage>()
            connection?.prepareStatement("SELECT * FROM duty_messages WHERE recipient = ?")?.use { stmt ->
                stmt.setString(1, to)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        dutiesMessages.add(rsToDutiesMessage(rs))
                    }
                }
            }
            return dutiesMessages
        } catch (e: java.sql.SQLException) {
            throw RuntimeException("Failed to list emails", e)
        }
    }

    fun listDutyMessagesFrom(from: String): List<DutiesMessage> {
        try {
            val dutiesMessages = mutableListOf<DutiesMessage>()
            connection?.prepareStatement("SELECT * FROM duty_messages WHERE sender = ?")?.use { stmt ->
                stmt.setString(1, from)
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        dutiesMessages.add(rsToDutiesMessage(rs))
                    }
                }
            }
            return dutiesMessages
        } catch (e: java.sql.SQLException) {
            throw RuntimeException("Failed to list emails", e)
        }
    }

    private fun rsToDutiesMessage(rs: java.sql.ResultSet): DutiesMessage {
        return DutiesMessage(
            uuid = rs.getString("uuid"),
            from = rs.getString("sender"),
            to = rs.getString("recipient"),
            subject = rs.getString("subject"),
            body = rs.getString("body"),
            datetime = TimeStamp(rs.getTimestamp("datetime")),
            completedButton = rs.getString("completed_button"),
            violatedButton = rs.getString("violated_button"),
            status = DutyStatus.valueOf(rs.getString("status")),
            dutiesDossierUuid = rs.getString("duties_dossier_uuid"),
            dutyUuid = rs.getString("duty_uuid")
        )
    }

    companion object {
        val instance by lazy { DutiesMessageServer() }
    }
}