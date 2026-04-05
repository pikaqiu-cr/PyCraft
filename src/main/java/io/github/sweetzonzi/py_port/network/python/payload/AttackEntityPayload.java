package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.concurrent.CompletableFuture;

public record AttackEntityPayload(
        int player_id,
        int target_id
) implements PyPayload {

    public static final Codec<AttackEntityPayload> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT.fieldOf("player_id").forGetter(AttackEntityPayload::player_id),
                            Codec.INT.fieldOf("target_id").forGetter(AttackEntityPayload::target_id)
                    ).apply(instance, AttackEntityPayload::new)
            );

    public static final PyPayloadType<AttackEntityPayload> TYPE = new PyPayloadType<>("attack_entity", CODEC);

    @Override
    public PyPayloadType<?> type() {return TYPE;}

    public static PyHandleResult handle(AttackEntityPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server not running");
        }

        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        server.execute(() -> {
            try {
                JsonObject result = new JsonObject();
                ServerPlayer player = null;
                for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                    if (p.getId() == payload.player_id()) {
                        player = p;
                        break;
                    }
                }
                if (player == null) {
                    future.completeExceptionally(
                            new RuntimeException("Player not found: " + payload.player_id())
                    );
                    return;
                }

                ServerLevel level = player.serverLevel();

                // 获取目标
                Entity target = level.getEntity(payload.target_id());
                if (target == null) {
                    result.addProperty("hit", false);
                    future.complete(result);
                    return;
                }

                // 距离检测
                double dist = player.distanceTo(target);
                if (dist > 4.5) {
                    result.addProperty("hit", false);
                    future.complete(result);
                    return;
                } // 限制玩家攻击范围需在4.5格之内

                // 攻击冷却
                if (player.getAttackStrengthScale(0.5f) < 0.9f) {
                    result.addProperty("hit", false);
                    future.complete(result);
                    return;
                }

                // 自动朝向
                player.lookAt(
                        net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,
                        target.position()
                );

                // 执行攻击
                player.attack(target);
                result.addProperty("hit", true);
                if (target instanceof LivingEntity living) {
                    result.addProperty("target_health", living.getHealth());
                } else {
                    result.addProperty("target_health", -1);
                }
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