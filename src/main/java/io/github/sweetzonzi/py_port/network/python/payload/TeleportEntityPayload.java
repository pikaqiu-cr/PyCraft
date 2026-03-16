package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;
import net.minecraft.world.entity.Entity;

public record TeleportEntityPayload(
        int entity_id,
        double x,
        double y,
        double z
) implements PyPayload {

    public static final Codec<TeleportEntityPayload> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("entity_id").forGetter(TeleportEntityPayload::entity_id),
                    Codec.DOUBLE.fieldOf("x").forGetter(TeleportEntityPayload::x),
                    Codec.DOUBLE.fieldOf("y").forGetter(TeleportEntityPayload::y),
                    Codec.DOUBLE.fieldOf("z").forGetter(TeleportEntityPayload::z)
            ).apply(instance, TeleportEntityPayload::new));

    public static final PyPayloadType<TeleportEntityPayload> TYPE =
            new PyPayloadType<>("teleport_entity", CODEC);

    @Override
    public PyPayloadType<?> type() {return TYPE;}

    /**
     * 执行实体瞬移
     */
    public static PyHandleResult handle(TeleportEntityPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server not running");
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
        entity.teleportTo(
                payload.x(),
                payload.y(),
                payload.z()
        );
        return PyHandleResult.success(new JsonObject());
    }
}