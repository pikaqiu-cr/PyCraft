package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;
import net.minecraft.world.entity.Entity;

public record GetEntityPosPayload(
        int entity_id // 实体ID
) implements PyPayload {

    public static final Codec<GetEntityPosPayload> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("entity_id")
                            .forGetter(GetEntityPosPayload::entity_id)
            ).apply(instance, GetEntityPosPayload::new));

    public static final PyPayloadType<GetEntityPosPayload> TYPE = new PyPayloadType<>("get_entity_pos", CODEC);

    @Override
    public PyPayloadType<?> type() {return TYPE;}

    public static PyHandleResult handle(GetEntityPosPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server is not running");
        }
        Entity entity = null;
        // 遍历所有维度查找实体
        for (var level : server.getAllLevels()) {
            entity = level.getEntity(payload.entity_id());
            if (entity != null) {
                break;
            }
        }
        if (entity == null) {
            return PyHandleResult.fail("Entity not found");
        }
        var pos = entity.position();
        JsonObject data = new JsonObject();
        data.addProperty("x", pos.x);
        data.addProperty("y", pos.y);
        data.addProperty("z", pos.z);
        return PyHandleResult.success(data);
    }
}
