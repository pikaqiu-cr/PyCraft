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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public record MoveEntityPayload(
        int entity_id,
        double x,
        double y,
        double z,
        double speed
) implements PyPayload {

    public static final Codec<MoveEntityPayload> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("entity_id").forGetter(MoveEntityPayload::entity_id),
                    Codec.DOUBLE.fieldOf("x").forGetter(MoveEntityPayload::x),
                    Codec.DOUBLE.fieldOf("y").forGetter(MoveEntityPayload::y),
                    Codec.DOUBLE.fieldOf("z").forGetter(MoveEntityPayload::z),
                    Codec.DOUBLE.fieldOf("speed").forGetter(MoveEntityPayload::speed)
            ).apply(instance, MoveEntityPayload::new));

    public static final PyPayloadType<MoveEntityPayload> TYPE = new PyPayloadType<>("move_entity", CODEC);

    @Override
    public PyPayloadType<?> type() {
        return TYPE;
    }

    public static PyHandleResult handle(MoveEntityPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server not running");
        }
        Entity entity = null;
        // 在所有维度中查找实体
        for (ServerLevel level : server.getAllLevels()) {
            entity = level.getEntity(payload.entity_id());
            if (entity != null) {
                break;
            }
        }
        if (entity == null) {
            return PyHandleResult.fail("Entity not found");
        }
        double targetX = payload.x();
        double targetY = payload.y();
        double targetZ = payload.z();

        if (entity instanceof ServerPlayer player) {

            Vec3 current = player.position();
            Vec3 target = new Vec3(targetX, targetY, targetZ);

            // 忽略Y轴
            Vec3 delta = new Vec3(target.x - current.x, 0, target.z - current.z);

            double distance = delta.length();

            if (distance < 0.05) {
                player.setDeltaMovement(Vec3.ZERO);
            } else {
                // 单位方向
                Vec3 direction = delta.normalize();
                // P控制：距离越近速度越小
                double k = 0.5;  // 增益
                double speed = Math.min(payload.speed(), distance * k);
                Vec3 velocity = direction.scale(speed);
                player.setDeltaMovement(velocity);
                player.hurtMarked = true;
            }
        }
//        else if (entity instanceof Mob mob) {
//            // 带AI生物使用自带寻路
//            mob.getMoveControl().setWantedPosition(targetX, targetY, targetZ, payload.speed());
//        }
        else {
            // 普通实体使用速度移动
            Vec3 current = entity.position();
            Vec3 target = new Vec3(
                    targetX,
                    targetY,
                    targetZ
            );
            Vec3 direction = target.subtract(current).normalize();
            Vec3 velocity = direction.scale(payload.speed());
            entity.setDeltaMovement(velocity);
        }
        return PyHandleResult.success(new JsonObject());
    }
}