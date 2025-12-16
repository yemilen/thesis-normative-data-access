package eu.amdex.research.dipgwithduties.pepsystem.pluginmanager

import org.amdex.features.duties.Duty
import java.util.concurrent.ConcurrentLinkedDeque

object PluginManager {
    private class PluginManagerThread : Runnable {
        override fun run() {
            while (true) {
                while (dutyTransitions.isNotEmpty()) {
                    val transition = dutyTransitions.pollFirst()
                    //TODO Process transition
                    println("Processed transition: $transition")
                }
                Thread.sleep(1000)
            }
        }

    }

    private val dutyTransitions = ConcurrentLinkedDeque<Duty>()

    fun dutyTransition(duty: Duty) {
        dutyTransitions.addLast(duty)
    }

}