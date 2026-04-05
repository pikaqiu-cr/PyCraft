package io.github.sweetzonzi.py_port.network;

import io.github.sweetzonzi.py_port.PyCraft;
import io.github.sweetzonzi.py_port.client.render.LineRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;  // 新增
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DrawLinePayload(
        double x1, double y1, double z1,
        double x2, double y2, double z2,
        int color,
        int duration
) implements CustomPacketPayload {

    // 类型定义
    public static final CustomPacketPayload.Type<DrawLinePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PyCraft.MOD_ID, "draw_line"));

    public static final StreamCodec<FriendlyByteBuf, DrawLinePayload> STREAM_CODEC =
            StreamCodec.ofMember(DrawLinePayload::write, DrawLinePayload::new);

    // 从 ByteBuf 读取（解码器）
    public DrawLinePayload(FriendlyByteBuf buf) {
        this(
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readDouble(), buf.readDouble(), buf.readDouble(),
                buf.readInt(), buf.readInt()
        );
    }

    // 写入 ByteBuf（编码器）
    public void write(FriendlyByteBuf buf) {
        buf.writeDouble(x1).writeDouble(y1).writeDouble(z1);
        buf.writeDouble(x2).writeDouble(y2).writeDouble(z2);
        buf.writeInt(color);
        buf.writeInt(duration);
    }

    @Override
    public CustomPacketPayload.Type<DrawLinePayload> type() {
        return TYPE;
    }

    // 客户端处理
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            // 日志
            PyCraft.LOGGER.info("[DrawLine] Client received: ({},{},{}) -> ({},{},{}) color={} duration={}",
                    x1, y1, z1, x2, y2, z2, color, duration);


            LineRenderer.addLine(
                    new Vec3(x1, y1, z1),
                    new Vec3(x2, y2, z2),
                    color,
                    duration
            );
        });
    }
}