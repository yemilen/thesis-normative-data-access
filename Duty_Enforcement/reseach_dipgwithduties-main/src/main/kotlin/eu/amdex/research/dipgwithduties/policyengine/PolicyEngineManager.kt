package eu.amdex.research.dipgwithduties.policyengine

import org.amdex.common.amdexLogger
import org.amdex.common.util.LocalDirectory
import org.amdex.common.util.newUUIDString
import org.amdex.features.duties.Duty
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.stream.Collectors

data class DutiesData(val duties: List<Duty>, val data: Map<String, Any>)

object PolicyEngineManager {
    private val eFlintCMD = "/home/merrick/Documents/GitHub/haskell-implementation/dist-newstyle/build/x86_64-linux/ghc-9.2.8/eflint-3.1.0.2/x/eflint-repl/build/eflint-repl/eflint-repl"

    private val eFlintFiles = LocalDirectory("/home/merrick/AMdEX-Framework/dossiers/src/main/resources/policies", false)
    private val tmpEFlintFiles = LocalDirectory("/home/merrick/AMdEX-Framework/dossiers/src/main/resources/policies", false)

    fun executePolicy(policy: String, data: Map<String, Any>): Pair<Boolean, DutiesData?> {
        return executePolicy(policy, DutiesData(emptyList(), data))
    }

    fun executePolicy(policy: String, dutiesData: DutiesData): Pair<Boolean, DutiesData?> {
        val sourcePolicyDir = eFlintFiles.openDirectory(policy)
        val policyDir = tmpEFlintFiles.openDirectory("tmp_${newUUIDString()}")
        sourcePolicyDir.copyRecursively(policyDir)

        val initializer = sourcePolicyDir.openFile("init.markup").readText()

        val instructions = initializer + dutiesData.duties.map { duty -> "+${duty.name}_${duty.currentState.name}" }
        policyDir.openFile("main.eflint").writeText(instructions)

        val result = runEFlintProcess(policyDir)

        // Process the result
        return if (result == null) Pair(false, null)
        else Pair(true, processResultMessage(result, dutiesData))
    }

    fun executeEFlint(code : String) : String?{
        println("Executing EFlint code")
        println(code)
        val policyDir = tmpEFlintFiles.openDirectory("tmp_${newUUIDString()}")
        policyDir.openFile("main.eflint").writeText(code)
        return runEFlintProcess(policyDir)
    }


    private fun processResultMessage(msg: String, sourceDutiesData: DutiesData): DutiesData {
        return DutiesData(emptyList(), emptyMap())
    }

    private fun runEFlintProcess(tmpDirectory: LocalDirectory): String? {
        var prOutput: BufferedReader? = null
        try {
            val command: MutableList<String> = ArrayList()
            command.add("/home/merrick/AMdEX-Framework/dossiers/src/main/resources/ubuntu/hackerman")
            command.add("--eflint-cmd")
            command.add("/home/merrick/RustroverProjects/clingo-implementation/dist-newstyle/build/x86_64-linux/ghc-9.2.8/eflint-4.1.0.1/x/eflint-repl/build/eflint-repl/eflint-repl")
            command.add("--include-dirs")
            command.add(tmpDirectory.absolutePath)
            command.add("main.eflint")

            val ps = ProcessBuilder(command)
            amdexLogger.debug { ps.command().stream().collect(Collectors.joining(" ")) }
            ps.redirectErrorStream()
            val pr = ps.start()
            prOutput = BufferedReader(InputStreamReader(pr.inputStream))

            val result = StringBuilder()
            prOutput.forEachLine { line ->
                result.appendLine(line)
                amdexLogger.debug { "eFlint - here:$line" }
            }
            amdexLogger.debug { "eFlint - ok!" }
            return result.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            prOutput?.close()
        }
        return null
    }
}