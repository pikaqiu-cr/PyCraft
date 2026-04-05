package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public record SpawnParticlePayload(
        String particle,
        double x,
        double y,
        double z,
        int count
) implements PyPayload {

    public static final Codec<SpawnParticlePayload> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("particle").forGetter(SpawnParticlePayload::particle),
                    Codec.DOUBLE.fieldOf("x").forGetter(SpawnParticlePayload::x),
                    Codec.DOUBLE.fieldOf("y").forGetter(SpawnParticlePayload::y),
                    Codec.DOUBLE.fieldOf("z").forGetter(SpawnParticlePayload::z),
                    Codec.INT.fieldOf("count").forGetter(SpawnParticlePayload::count)
            ).apply(instance, SpawnParticlePayload::new));

    public static final PyPayloadType<SpawnParticlePayload> TYPE = new PyPayloadType<>("spawn_particle", CODEC);

    @Override
    public PyPayloadType<?> type() {return TYPE;}

    public static PyHandleResult handle(SpawnParticlePayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server not running");
        }

        ServerLevel level = server.overworld();
        ParticleOptions particle = ParticleTypes.FLAME;
        level.sendParticles(
                particle,
                payload.x(),
                payload.y(),
                payload.z(),
                payload.count(),
                0.1, 0.1, 0.1,
                0.05
        );
        return PyHandleResult.success(new JsonObject());
    }
}