package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record SetBlocksPayload(
        String level,
        int x1,
        int y1,
        int z1,
        int x2,
        int y2,
        int z2,
        String block
) implements PyPayload {

    public static final Codec<SetBlocksPayload> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("level").forGetter(SetBlocksPayload::level),
                    Codec.INT.fieldOf("x1").forGetter(SetBlocksPayload::x1),
                    Codec.INT.fieldOf("y1").forGetter(SetBlocksPayload::y1),
                    Codec.INT.fieldOf("z1").forGetter(SetBlocksPayload::z1),
                    Codec.INT.fieldOf("x2").forGetter(SetBlocksPayload::x2),
                    Codec.INT.fieldOf("y2").forGetter(SetBlocksPayload::y2),
                    Codec.INT.fieldOf("z2").forGetter(SetBlocksPayload::z2),
                    Codec.STRING.fieldOf("block").forGetter(SetBlocksPayload::block)
            ).apply(instance, SetBlocksPayload::new));

    public static final PyPayloadType<SetBlocksPayload> TYPE = new PyPayloadType<>("set_blocks", CODEC);

    @Override
    public PyPayloadType<?> type() {return TYPE;}

    public static PyHandleResult handle(SetBlocksPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server not running");
        }
        // 获取维度
        ServerLevel level = server.getLevel(
                ResourceKey.create(
                        Registries.DIMENSION,
                        ResourceLocation.parse(payload.level())
                )
        );
        if (level == null) {
            return PyHandleResult.fail("Level not found");
        }
        // 获取方块
        Block block = BuiltInRegistries.BLOCK.get(
                ResourceLocation.parse(payload.block())
        );
        if (block == null) {
            return PyHandleResult.fail("Block not found");
        }
        BlockState state = block.defaultBlockState();
        int minX = Math.min(payload.x1(), payload.x2());
        int maxX = Math.max(payload.x1(), payload.x2());
        int minY = Math.min(payload.y1(), payload.y2());
        int maxY = Math.max(payload.y1(), payload.y2());
        int minZ = Math.min(payload.z1(), payload.z2());
        int maxZ = Math.max(payload.z1(), payload.z2());
        // 填充区域
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    level.setBlock(pos, state, 3);
                }
            }
        }
        return PyHandleResult.success(new JsonObject());
    }
}