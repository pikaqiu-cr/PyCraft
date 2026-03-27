package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;

public record SpawnEntityPayload(
        ResourceLocation level,      // 维度ID，例如 minecraft:overworld
        double x,                    // 坐标X
        double y,                    // 坐标Y
        double z,                    // 坐标Z
        ResourceLocation entity_type // 实体类型ID，例如 minecraft:pig
) implements PyPayload {

    public static final Codec<SpawnEntityPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("level").forGetter(SpawnEntityPayload::level),
            Codec.DOUBLE.fieldOf("x").forGetter(SpawnEntityPayload::x),
            Codec.DOUBLE.fieldOf("y").forGetter(SpawnEntityPayload::y),
            Codec.DOUBLE.fieldOf("z").forGetter(SpawnEntityPayload::z),
            ResourceLocation.CODEC.fieldOf("entity_type").forGetter(SpawnEntityPayload::entity_type)
    ).apply(instance, SpawnEntityPayload::new));

    public static final PyPayloadType<SpawnEntityPayload> TYPE = new PyPayloadType<>("spawn_entity", CODEC);

    @Override
    public PyPayloadType<?> type() { return TYPE; }

    public static PyHandleResult handle(SpawnEntityPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server is not running");
        }

        // 1. 获取目标维度
        ServerLevel serverLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, payload.level()));
        if (serverLevel == null) {
            return PyHandleResult.fail("Level " + payload.level() + " not found");
        }

        // 2. 获取实体类型
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(payload.entity_type());
        if (type == null) {
            return PyPayloadType.fail("Entity type " + payload.entity_type() + " not found");
        }

        // 3. 必须在服务器主线程执行生成操作
        // 由于需要返回 UUID，我们可以在主线程外先准备好 UUID（或者直接返回成功）
        // 这里为了确保实体 ID 能传回，我们假设直接返回，生成过程交由 Server Queue
        server.execute(() -> {
            Entity entity = type.create(serverLevel);
            if (entity != null) {
                entity.moveTo(payload.x(), payload.y(), payload.z(), 0, 0);

                // 如果是生物，初始化其默认行为（如随机花色）
                if (entity instanceof Mob mob) {
                    mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(entity.blockPosition()),
                            MobSpawnType.COMMAND, null);
                }

                serverLevel.addFreshEntity(entity);
            }
        });

        // 返回成功，如果需要追踪，可以根据 context 进一步优化返回 entity.getUUID()
        return PyHandleResult.success(new JsonObject());
    }
}