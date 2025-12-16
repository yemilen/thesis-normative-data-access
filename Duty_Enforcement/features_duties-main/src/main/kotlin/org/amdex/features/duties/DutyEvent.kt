package org.amdex.features.duties

import kotlin.reflect.KClass

@Repeatable
@Target(AnnotationTarget.FUNCTION)
annotation class DutyEvent1(val name: String)

interface IDutyEvent {
    val data: Map<String, Any?>
    val name: String
        get() = data["name"] as String? ?: throw RuntimeException("DutyEvent name is null")

    fun validateDataTypes(vararg data: Pair<String, KClass<*>>) {
        data.forEach { (key, clazz) ->
            val isNullable = clazz.java.isAssignableFrom(Nothing::class.java)
            if (this.data[key] == null && !isNullable) throw RuntimeException("DutyEvent $name data $key is null")
            if (this.data[key] != null && !clazz.isInstance(this.data[key])) throw RuntimeException("DutyEvent $name data $key is not instance of ${clazz.qualifiedName}")
        }
    }
}

abstract class AbstractDutyEvent(data: Map<String, Any?>) : IDutyEvent {
    override val data: Map<String, Any?> = run {
        val initData = data.toMutableMap()
        if (!initData.containsKey("type")) initData["type"] = this::class.simpleName
        if (!initData.containsKey("name")) initData["name"] = this::class.simpleName
        initData.toMap()
    }

    init {
        validateDataTypes("name" to String::class, "type" to String::class)
        if (data["type"] != this::class.simpleName) throw RuntimeException("DutyEvent type ${data["type"]} does not match the DutyEvent class name ${this::class.simpleName}")
    }

    constructor(vararg data: Pair<String, Any?>) : this(data.toMap())
}
