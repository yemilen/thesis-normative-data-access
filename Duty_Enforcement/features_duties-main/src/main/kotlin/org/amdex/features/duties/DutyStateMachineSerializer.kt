package org.amdex.features.duties

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class DutyStateMachineSerializer : StdSerializer<DutyStateMachine>(DutyStateMachine::class.java) {
    override fun serialize(value: DutyStateMachine, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        serializeFields(value, gen, provider)
        gen.writeEndObject()
    }

    override fun serializeWithType(
        value: DutyStateMachine,
        gen: JsonGenerator,
        provider: SerializerProvider,
        typeSer: TypeSerializer
    ) {
        val typeId = typeSer.typeId(value, JsonToken.START_OBJECT)
        typeSer.writeTypePrefix(gen, typeId)
        serializeFields(value, gen, provider)
        typeSer.writeTypeSuffix(gen, typeId)
    }

    private fun serializeFields(value: DutyStateMachine, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeFieldName("nodes")
        provider.defaultSerializeValue(value.getNodes(), gen)
    }
}

class DutyStateMachineDeserializer : StdDeserializer<DutyStateMachine>(DutyStateMachine::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): DutyStateMachine {
        val mapper = p.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(p)
        val nodesNode = node.get("nodes")

        val nodes = mutableListOf<Duty>()
        if (nodesNode != null && nodesNode.isArray) {
            for (item in nodesNode) {
                nodes.add(mapper.treeToValue(item, Duty::class.java))
            }
        }
        return DutyStateMachine(nodes)
    }
}