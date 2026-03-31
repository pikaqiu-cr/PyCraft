package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.concurrent.CompletableFuture;

public record GetEntitiesPayload(
        ResourceLocation level,
        String entity_filter   // 避免和 type() 冲突
) implements PyPayload {
    public static final Codec<GetEntitiesPayload> CODEC =
            RecordCodecBuilder.create(instance ->
                    instance.group(
                            ResourceLocation.CODEC.fieldOf("level").forGetter(GetEntitiesPayload::level),
                            Codec.STRING.optionalFieldOf("type", "all").forGetter(GetEntitiesPayload::entity_filter)
                    ).apply(instance, GetEntitiesPayload::new)
            );

    public static final PyPayloadType<GetEntitiesPayload> TYPE = new PyPayloadType<>("get_entities", CODEC);

    @Override
    public PyPayloadType<?> type() {return TYPE;}

    public static PyHandleResult handle(GetEntitiesPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server is not running");
        }

        ServerLevel level = server.getLevel(
                ResourceKey.create(Registries.DIMENSION, payload.level())
        );

        if (level == null) {
            return PyHandleResult.fail("Level not found");
        }

        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        server.execute(() -> {
            try {
                JsonArray arr = new JsonArray();
                for (Entity e : level.getAllEntities()) {
                    if ("monster".equals(payload.entity_filter())) {
                        if (!(e instanceof Mob)) continue;
                    }
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", e.getId());
                    obj.addProperty("type", e.getType().toString());
                    obj.addProperty("x", e.getX());
                    obj.addProperty("y", e.getY());
                    obj.addProperty("z", e.getZ());
                    if (e instanceof LivingEntity living) {
                        obj.addProperty("health", living.getHealth());
                    } else {
                        obj.addProperty("health", -1);
                    }
                    arr.add(obj);
                }
                JsonObject result = new JsonObject();
                result.add("entities", arr);
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