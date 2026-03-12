package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;

public record ListLevelPayload() implements PyPayload {
    public static final Codec<ListLevelPayload> CODEC = Codec.unit(ListLevelPayload::new);
    public static final PyPayloadType<ListLevelPayload> TYPE = new PyPayloadType<>("list_level", CODEC);

    @Override
    public PyPayloadType<?> type() {
        return TYPE;
    }

    public static PyHandleResult handle(ListLevelPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server is not running");
        }
        // 获取所有已加载维度的 ResourceKey，提取 ResourceLocation 的字符串形式
        var levelNames = server.levelKeys().stream()
                .map(key -> key.location().toString())
                .toList();
        JsonObject data = new JsonObject();
        JsonArray levelsArray = new JsonArray();
        levelNames.forEach(levelsArray::add);
        data.add("levels", levelsArray);
        return PyHandleResult.success(data);
    }
}
