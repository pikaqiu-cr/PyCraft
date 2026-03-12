package io.github.sweetzonzi.py_port.network.python.infrastructure;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

class PyChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) {

        ChannelPipeline p = ch.pipeline();

        p.addLast("frameDecoder",
                new LengthFieldBasedFrameDecoder(
                        65536,
                        0,
                        4,
                        0,
                        4
                ));

        p.addLast("frameEncoder",
                new LengthFieldPrepender(4));
        // 字节消息编解码器，转回json
        p.addLast("packetDecoder", new PyMessageDecoder());
        p.addLast("packetEncoder", new PyMessageEncoder());
        // 业务分发处理器，将json内容分发处理
        p.addLast("dispatcher", new PyMessageDispatcher());
    }
}
