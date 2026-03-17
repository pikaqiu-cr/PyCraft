package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.network.python.infrastructure.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public record SetCameraPayload(
        int player_id,
        int target_entity_id
) implements PyPayload {

    public static final Codec<SetCameraPayload> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("player_id").forGetter(SetCameraPayload::player_id),
                    Codec.INT.fieldOf("target_entity_id").forGetter(SetCameraPayload::target_entity_id)
            ).apply(instance, SetCameraPayload::new));

    public static final PyPayloadType<SetCameraPayload> TYPE =
            new PyPayloadType<>("set_camera", CODEC);

    @Override
    public PyPayloadType<?> type() {return TYPE;}

    public static PyHandleResult handle(SetCameraPayload payload, PyContext context) {
        MinecraftServer server = context.getServer();
        if (server == null) {return PyHandleResult.fail("Server not running");}
        var level = server.overworld(); // 指定维度
        Entity entity = level.getEntity(payload.player_id());
        if (!(entity instanceof ServerPlayer player)) {
            return PyHandleResult.fail("Player not found");
        }
        Entity target = player.level().getEntity(payload.target_entity_id());
        if (target == null) {
            return PyHandleResult.fail("Target entity not found");
        }
        // 切换camera
        player.setCamera(target);
        return PyHandleResult.success(new JsonObject());
    }
}