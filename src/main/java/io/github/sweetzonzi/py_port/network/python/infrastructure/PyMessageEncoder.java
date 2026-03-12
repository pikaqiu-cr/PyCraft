package io.github.sweetzonzi.py_port.network.python.infrastructure;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.nio.charset.StandardCharsets;

public class PyMessageEncoder extends MessageToByteEncoder<JsonObject> {
    // 我们约定响应也是一个 JsonObject，包含需要返回的数据
    @Override
    protected void encode(ChannelHandlerContext ctx, JsonObject msg, ByteBuf out) {
        String jsonStr = msg.toString();
        out.writeBytes(jsonStr.getBytes(StandardCharsets.UTF_8));
    }
}
