package eu.amdex.research.dipgwithduties.policyengine

import kotlinx.serialization.json.Json
import org.amdex.common.amdexLogger
import org.amdex.common.util.LocalDirectory
import org.amdex.common.util.newUUIDString
import java.io.*
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.stream.Collectors
import javax.naming.CommunicationException

enum class EFlintServerEvent {
    STARTED, STOPPED
}

class EFlintSession(
    private val host: String = "127.0.0.1",
    private var port: Int = randomPort(),
    private val uuid: String = newUUIDString(),
    private val file: File,
    private val filePaths: List<String> = emptyList(),
    private val callback: (session: EFlintSession, event: EFlintServerEvent, data: Any?) -> Unit
) : Thread(), AutoCloseable {
    override fun start() {
        super.start()
        sleep(1000)
    }
    override fun run() {
        // -- Linux --
        val exitVal = -1
        try {
            val command: MutableList<String> = ArrayList()
            command.add(EFLINT_COMMAND)
            command.add(file.absolutePath)
            command.add(port.toString())
            for (filePath in filePaths) {
                command.add("-i")
                command.add(filePath)
            }
            val ps = ProcessBuilder(command)
            amdexLogger.info { ps.command().stream().collect(Collectors.joining(" ")) }
            ps.redirectErrorStream(true)
            //            ps.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            val pr = ps.start()
            val `in` = BufferedReader(InputStreamReader(pr.inputStream))
            callback(this, EFlintServerEvent.STARTED, null)
            var line: String? = ""
            while (true) {
                line = `in`.readLine()
                amdexLogger.info { "eFlint - here:$line" }
                try {
                    sleep(100)
                } catch (e: InterruptedException) {
                    break
                }
            }
            amdexLogger.info { "eFlint - ok!" }
            `in`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        callback(this, EFlintServerEvent.STOPPED, exitVal)
    }

    fun communicate(request: String, communicationPort: Int = port): EngineResponse {
        try {
            Socket(InetAddress.getByName(host), communicationPort).use { socket ->
                socket.reuseAddress = true
                PrintWriter(socket.getOutputStream()).use { writer ->
                    BufferedReader(
                        InputStreamReader(socket.getInputStream())
                    ).use { reader ->
                        amdexLogger.info("sending request to $host on port $communicationPort")
                        amdexLogger.info("request: " + kotlinxJson.encodeToString (eFlintPhrase( request)))
                        writer.println(kotlinxJson.encodeToString(eFlintPhrase(request)))
                        writer.flush()
                        val responseStr: String? = reader.readLine()
                        amdexLogger.info("response:$responseStr")
                        //   val gson = Gson()
                        if (responseStr != null) {
                            return kotlinxJson.decodeFromString(responseStr)
//                            amdexLogger.debug("eFlint Debug input: $input")
//                            amdexLogger.debug("eFlint Debug response: $responseStr")
//                            return StandardResponse(
//                                status = StatusResponse.SUCCESS,
//                                data = responseObj
//                            )
                            //                    if (response.get("response").equals("success")) {
//                        //TODO report any violations
//                    }
//                    else if(response.get("response").equals("invalid input")) {
//                        //throw new CompilationError();
//                        System.out.info(response.get("error"));
//                    }
//                    return response;
                        } else {
                            amdexLogger.error("communication error") //TODO: make this actually report on the error
                        }
                    }
                }
            }
        } catch (e: IOException) {
            amdexLogger.error("cannot communicate with $host on port $communicationPort", e)
            throw e
        }
        throw CommunicationException("cannot communicate with localhost on port $communicationPort")
    }

    override fun close() {
        interrupt()
    }

    fun eFlintPhrase(phrase: String) = mapOf(Pair("command", "phrase"), Pair("text", phrase + if (phrase.endsWith('.')) "" else "."))

    companion object {
        val kotlinxJson = Json {
            ignoreUnknownKeys = true
        }

        fun createForModel(
            model: String,
            host: String = "127.0.0.1",
            port: Int = randomPort(),
            callback: (session: EFlintSession, event: EFlintServerEvent, data: Any?) -> Unit
        ): EFlintSession {
            val policyDir = tmpEFlintFiles.openDirectory("tmp_${newUUIDString()}")
            val modelFile = policyDir.openFile("model.eflint").also { it.writeText(model) }

            return EFlintSession(host = host, port = port, file = modelFile, callback = callback).also { it.start() }
        }

        private val eFlintFiles =
            LocalDirectory("/home/merrick/AMdEX-Framework/dossiers/src/main/resources/policies", false)
        private val tmpEFlintFiles =
            LocalDirectory("/home/merrick/AMdEX-Framework/dossiers/src/main/resources/policies", false)
        private const val EFLINT_COMMAND =
            "/home/merrick/.cabal/bin/eflint-server"
        private const val PORT_MIN_NUM = 20000
        private const val PORT_MAX_NUM = 40000

        private fun randomPort(): Int {
            val r = Random()
            while (true) {
                val testPort = PORT_MIN_NUM + r.nextInt(PORT_MAX_NUM - PORT_MIN_NUM)
                var ss: ServerSocket? = null
                try {
                    ss = ServerSocket(testPort)
                    ss.reuseAddress = true
                    return testPort
                } catch (_: IOException) {
                } finally {
                    if (ss != null) {
                        try {
                            ss.close()
                        } catch (e: IOException) {
                            /* should not be thrown */
                        }
                    }
                }
            }
        }
    }
}
