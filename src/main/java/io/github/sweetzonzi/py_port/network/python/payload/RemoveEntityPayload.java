package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.concurrent.CompletableFuture;

public record RemoveEntityPayload(
        int entity_id
) implements PyPayload {
    public static final Codec<RemoveEntityPayload> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("entity_id").forGetter(RemoveEntityPayload::entity_id)
            ).apply(instance, RemoveEntityPayload::new));

    public static final PyPayloadType<RemoveEntityPayload> TYPE = new PyPayloadType<>("remove_entity", CODEC);

    @Override
    public PyPayloadType<?> type() {return TYPE;}

    public static PyHandleResult handle(RemoveEntityPayload payload, PyContext context) {
        MinecraftServer server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server is not running");
        }

        CompletableFuture<JsonObject> future = new CompletableFuture<>();

        server.execute(() -> {
            try {
                JsonObject result = new JsonObject();
                boolean removed = false;
                // 遍历所有维度查找 entity
                for (ServerLevel level : server.getAllLevels()) {
                    Entity entity = level.getEntity(payload.entity_id());
                    if (entity != null) {
                        entity.discard();
                        removed = true;
                        break;
                    }
                }
                if (!removed) {
                    future.completeExceptionally(
                            new RuntimeException("Entity not found: " + payload.entity_id())
                    );
                    return;
                }
                result.addProperty("removed", true);
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        try {
            JsonObject result = future.get();
            return PyHandleResult.success(result);
        } catch (Exception e) {
            return PyHandleResult.fail("Remove failed: " + e.getMessage());
        }
    }
}
