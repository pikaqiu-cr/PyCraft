package io.github.sweetzonzi.py_port.agent;

import lombok.Getter;
import net.minecraft.world.entity.Mob;

public class Agent {
    @Getter
    private final Mob mob;

    public Agent(Mob mob) {
        this.mob = mob;
    }

    public void tick() {

    }
}
