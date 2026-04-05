package io.github.sweetzonzi.py_port.network.python.payload;
import io.github.sweetzonzi.py_port.agent.AgentManager;

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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record SpawnEntityPayload(
        ResourceLocation level,
        double x,
        double y,
        double z,
        ResourceLocation entity_type,
        Optional<Boolean> is_agent  // 是否禁用AI
) implements PyPayload {

    public static final Codec<SpawnEntityPayload> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("level").forGetter(SpawnEntityPayload::level),
            Codec.DOUBLE.fieldOf("x").forGetter(SpawnEntityPayload::x),
            Codec.DOUBLE.fieldOf("y").forGetter(SpawnEntityPayload::y),
            Codec.DOUBLE.fieldOf("z").forGetter(SpawnEntityPayload::z),
            ResourceLocation.CODEC.fieldOf("entity_type").forGetter(SpawnEntityPayload::entity_type),
            Codec.BOOL.optionalFieldOf("is_agent").forGetter(SpawnEntityPayload::is_agent)
    ).apply(instance, SpawnEntityPayload::new));

    public static final PyPayloadType<SpawnEntityPayload> TYPE =
            new PyPayloadType<>("spawn_entity", CODEC);

    @Override
    public PyPayloadType<?> type() { return TYPE; }

    public static PyHandleResult handle(SpawnEntityPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server is not running");
        }

        ServerLevel serverLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, payload.level()));
        if (serverLevel == null) {
            return PyHandleResult.fail("Level not found");
        }

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(payload.entity_type());
        if (type == null) {
            return PyHandleResult.fail("Invalid entity type");
        }

        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        server.execute(() -> {
            try {
                JsonObject result = new JsonObject();

                Entity entity = type.create(serverLevel);
                if (entity == null) {
                    future.completeExceptionally(new RuntimeException("Create failed"));
                    return;
                }

                entity.moveTo(payload.x(), payload.y(), payload.z(), 0, 0);

                if (entity instanceof Mob mob) {
                    mob.finalizeSpawn(
                            serverLevel,
                            serverLevel.getCurrentDifficultyAt(entity.blockPosition()),
                            MobSpawnType.COMMAND,
                            null
                    );

                    // 注册为 agent（即玩家自行控制）
                    if (payload.is_agent().orElse(false)) {
                        mob.setNoAi(true);

                        mob.setDeltaMovement(0, 0, 0);
                        mob.setNoGravity(false); // 保留物理

                        AgentManager.register(mob);
                    }
                }

                serverLevel.addFreshEntity(entity);

                result.addProperty("id", entity.getId());
                result.addProperty("type", payload.entity_type().toString());

                future.complete(result);

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        try {
            return PyHandleResult.success(future.get());
        } catch (Exception e) {
            return PyHandleResult.fail(e.getMessage());
        }
    }
}