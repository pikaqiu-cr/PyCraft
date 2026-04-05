package io.github.sweetzonzi.py_port.agent;

import io.github.sweetzonzi.py_port.agent.Agent;
import net.minecraft.world.entity.Mob;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AgentManager {

    private static final Map<Integer, Agent> agents = new ConcurrentHashMap<>();

    public static void register(Mob mob) {
        agents.put(mob.getId(), new Agent(mob));
    }

    public static Agent get(int id) {
        return agents.get(id);
    }

    public static void tickAll() {
        for (Agent agent : agents.values()) {
            agent.tick();
        }
    }

    public static void remove(int id) {
        agents.remove(id);
    }
}