package io.github.sweetzonzi.py_port.network.python.infrastructure;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class PyMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 此时 in 已经是一个完整的帧（LengthFieldBasedFrameDecoder 已经处理了长度）
        String jsonStr = in.readCharSequence(in.readableBytes(), StandardCharsets.UTF_8).toString();
        try {
            JsonObject root = JsonParser.parseString(jsonStr).getAsJsonObject();
            String type = root.get("type").getAsString();
            UUID uuid = UUID.fromString(root.get("uuid").getAsString());
            JsonObject data = root.get("data").getAsJsonObject();
            out.add(new PyMessage(type, uuid, data));
        } catch (JsonParseException e) {
            // 可发送错误消息或直接关闭连接
            ctx.fireExceptionCaught(e);
        }
    }
}
