package org.amdex.features.duties

class DutyStateMachineBuilder(private val lambda : DutyStateMachineBuilder.() -> Unit) {
    class NodeBuilder(private val lambda : NodeBuilder.() -> Unit) {
        lateinit var name: String
        lateinit var actor: String
        var currentState: DutyStateType = DutyStateType.CREATED

        fun build() : Duty {
            lambda(this)
            return Duty(name, actor)
        }
    }

    private val nodes = mutableListOf<Duty>()
    fun addNode(node: Duty) = nodes.add(node)
    fun addNode(actor: String, duty: String) = nodes.add(Duty(actor, duty))
    fun addNode(lambda: NodeBuilder.() -> Unit) = nodes.add(NodeBuilder(lambda).build())
    fun getNodes() = nodes.toList()


    fun build() : DutyStateMachine {
        lambda(this)
        return DutyStateMachine()
    }
}