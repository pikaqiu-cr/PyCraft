package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record GetTimePayload(
        ResourceLocation level
) implements PyPayload {
    public static final Codec<GetTimePayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("level").forGetter(GetTimePayload::level)
    ).apply(instance, GetTimePayload::new));
    public static final PyPayloadType<GetTimePayload> TYPE = new PyPayloadType<>("get_time", CODEC);

    @Override
    public PyPayloadType<?> type() {
        return TYPE;
    }

    public static PyHandleResult handle(GetTimePayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server is not running");
        }
        // 尝试获取维度
        var level = server.getLevel(ResourceKey.create(Registries.DIMENSION, payload.level()));
        if (level != null) {
            JsonObject data = new JsonObject();
            data.addProperty("time", level.getGameTime()); // 获取游戏时间，存入数据
            return PyHandleResult.success(data);
        } else return PyHandleResult.fail("Level + " + payload.level() + " not found");
    }
}
