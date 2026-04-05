package io.github.sweetzonzi.py_port.agent;

import net.minecraft.world.entity.Mob;

public class Agent {

    private final Mob mob;

    private double vx, vy, vz;
    private float yaw, pitch;
    private boolean jump;

    public Agent(Mob mob) {
        this.mob = mob;
    }

    public void setAction(double vx, double vy, double vz, float yaw, float pitch, boolean jump) {
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.yaw = yaw;
        this.pitch = pitch;
        this.jump = jump;
    }

    public void tick() {

        // 应用控制
        mob.setDeltaMovement(vx, vy, vz);

        mob.setYRot(yaw);
        mob.setXRot(pitch);

        if (jump && mob.onGround()) {
            mob.setDeltaMovement(
                    mob.getDeltaMovement().x,
                    0.42,
                    mob.getDeltaMovement().z
            );
        }

        // 强制压制 AI / pathfinding
        mob.getNavigation().stop();
        mob.setTarget(null);

        // 防止残留移动
        if (Math.abs(vx) < 1e-4 && Math.abs(vz) < 1e-4) {
            mob.setDeltaMovement(0, mob.getDeltaMovement().y, 0);
        }
    }
}
