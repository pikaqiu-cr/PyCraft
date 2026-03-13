package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record GetBlockPayload(
        ResourceLocation level, // 维度ID，例如 minecraft:overworld
        int x,                  // 方块X坐标
        int y,                  // 方块Y坐标
        int z                  // 方块Z坐标
) implements PyPayload {
    public static final Codec<GetBlockPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("level").forGetter(GetBlockPayload::level),
            Codec.INT.fieldOf("x").forGetter(GetBlockPayload::x),
            Codec.INT.fieldOf("y").forGetter(GetBlockPayload::y),
            Codec.INT.fieldOf("z").forGetter(GetBlockPayload::z)
    ).apply(instance, GetBlockPayload::new));

    public static final PyPayloadType<GetBlockPayload> TYPE = new PyPayloadType<>("get_block", CODEC);

    @Override
    public PyPayloadType<?> type() { return TYPE; }

    public static PyHandleResult handle(GetBlockPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server is not running");
        }
        // 获取维度
        var level = server.getLevel(ResourceKey.create(Registries.DIMENSION, payload.level()));
        if (level == null) {return PyHandleResult.fail("Level " + payload.level() + " not found");}
        // 创建坐标
        BlockPos pos = new BlockPos(payload.x(), payload.y(), payload.z());
        // 获取方块状态
        BlockState state = level.getBlockState(pos);
        // 获取方块ID
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        // 构造返回数据
        JsonObject data = new JsonObject();
        data.addProperty("block", blockId.toString());

        return PyHandleResult.success(data);
    }
}