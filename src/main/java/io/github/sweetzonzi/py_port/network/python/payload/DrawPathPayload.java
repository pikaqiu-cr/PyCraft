package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.PyCraft;
import io.github.sweetzonzi.py_port.network.java.payload.DrawLinePayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;  // 新增

import java.util.List;

public record DrawPathPayload(
        List<List<Double>> points,
        int color,
        int duration
) implements PyPayload {

    public static final Codec<DrawPathPayload> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.list(Codec.list(Codec.DOUBLE))
                            .fieldOf("points")
                            .forGetter(DrawPathPayload::points),
                    Codec.INT.fieldOf("color")
                            .forGetter(DrawPathPayload::color),
                    Codec.INT.fieldOf("duration")
                            .forGetter(DrawPathPayload::duration)
            ).apply(instance, DrawPathPayload::new));

    public static final PyPayloadType<DrawPathPayload> TYPE =
            new PyPayloadType<>("draw_path", CODEC);

    @Override
    public PyPayloadType<?> type() {
        return TYPE;
    }

    public static PyHandleResult handle(DrawPathPayload payload, PyContext context) {
        var server = context.getServer();
        if (server == null) {
            return PyHandleResult.fail("Server not running");
        }

        server.execute(() -> {
            ServerLevel level = server.overworld();
            List<List<Double>> pts = payload.points();

            PyCraft.LOGGER.info("[DrawPath] Received {} points, sending to clients", pts.size());
            // 把路径拆分成线段，发送到所有客户端
            for (int i = 0; i < pts.size() - 1; i++) {
                List<Double> a = pts.get(i);
                List<Double> b = pts.get(i + 1);

                // 创建线段数据包
                DrawLinePayload linePacket = new DrawLinePayload(
                        a.get(0), a.get(1), a.get(2),
                        b.get(0), b.get(1), b.get(2),
                        payload.color(),
                        payload.duration()
                );

                // 日志
                PyCraft.LOGGER.info("[DrawPath] Sending line: ({},{},{}) -> ({},{},{})",
                        a.get(0), a.get(1), a.get(2), b.get(0), b.get(1), b.get(2));

                // 发送到所有在线玩家
                PacketDistributor.sendToAllPlayers(linePacket);
            }
        });

        return PyHandleResult.success(new JsonObject());
    }
}