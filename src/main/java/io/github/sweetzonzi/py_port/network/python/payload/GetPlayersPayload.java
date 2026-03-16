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

public record GetPlayersPayload(
        ResourceLocation level // 维度ID，例如 minecraft:overworld
) implements PyPayload {
    public static final Codec<GetPlayersPayload> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("level")
                            .forGetter(GetPlayersPayload::level)
            ).apply(instance, GetPlayersPayload::new));

    public static final PyPayloadType<GetPlayersPayload> TYPE = new PyPayloadType<>("get_players", CODEC);

    @Override
    public PyPayloadType<?> type() {return TYPE;}

    public static PyHandleResult handle(GetPlayersPayload payload, PyContext context) {
        // 获取服务器实例
        var server = context.getServer();
        if (server == null) {return PyHandleResult.fail("Server is not running");}
        // 根据维度ID获取 Level
        var level = server.getLevel(ResourceKey.create(Registries.DIMENSION, payload.level()));
        if (level == null) {return PyHandleResult.fail("Level " + payload.level() + " not found");}
        // 构造返回 JSON
        JsonObject data = new JsonObject();
        JsonArray players = new JsonArray();
        // 获取服务器所有玩家
        var playerList = server.getPlayerList().getPlayers();
        for (var player : playerList) {
            // 只返回指定维度中的玩家
            if (player.level() != level) continue;
            JsonObject p = new JsonObject();
            // entity id
            p.addProperty("id", player.getId());
            // 玩家名字
            p.addProperty("name", player.getGameProfile().getName());
            players.add(p);
        }
        data.add("players", players);
        return PyHandleResult.success(data);
    }
}