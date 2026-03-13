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

public record SetBlockPayload(
        ResourceLocation level, // 维度ID，例如 minecraft:overworld
        int x,                  // 方块X坐标
        int y,                  // 方块Y坐标
        int z,                  // 方块Z坐标
        ResourceLocation block  // 方块ID，例如 minecraft:stone
) implements PyPayload {

    /**
     * Codec用于JSON-Java对象的序列化与反序列化
     */
    public static final Codec<SetBlockPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("level").forGetter(SetBlockPayload::level),
            Codec.INT.fieldOf("x").forGetter(SetBlockPayload::x),
            Codec.INT.fieldOf("y").forGetter(SetBlockPayload::y),
            Codec.INT.fieldOf("z").forGetter(SetBlockPayload::z),
            ResourceLocation.CODEC.fieldOf("block").forGetter(SetBlockPayload::block)
    ).apply(instance, SetBlockPayload::new));

    /**
     * Payload类型名称
     * Python请求时type字段必须为 "set_block"
     */
    public static final PyPayloadType<SetBlockPayload> TYPE = new PyPayloadType<>("set_block", CODEC);

    @Override
    public PyPayloadType<?> type() { return TYPE; }

    /**
     * 处理Python请求
     */
    public static PyHandleResult handle(SetBlockPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server is not running");
        }
        // 获取维度
        var level = server.getLevel(ResourceKey.create(Registries.DIMENSION, payload.level()));
        if (level == null) {return PyHandleResult.fail("Level " + payload.level() + " not found");}
        // 创建方块坐标
        BlockPos pos = new BlockPos(payload.x(), payload.y(), payload.z());
        // 根据ID获取方块对象
        Block block = BuiltInRegistries.BLOCK.get(payload.block());
        if (block == null) {
            return PyHandleResult.fail("Block " + payload.block() + " not found");
        }
        // 获取默认方块状态
        BlockState state = block.defaultBlockState();
        // 设置方块
        level.setBlock(pos, state, 3);
        // 返回成功
        return PyHandleResult.success(new JsonObject());
    }
}