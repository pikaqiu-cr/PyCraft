package io.github.sweetzonzi.py_port.network.python.infrastructure;
import io.github.sweetzonzi.py_port.network.python.PyPayloadRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class PyMessageDispatcher extends SimpleChannelInboundHandler<PyMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PyMessage msg) {
        // 为当前连接创建上下文对象（包含发送回复的能力）
        PyContext pyCtx = new PyContext(ctx, msg.type(), msg.uuid());
        // 将消息交给注册表处理
        PyPayloadRegistry.handle(msg, pyCtx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 记录异常，避免连接泄漏
        cause.printStackTrace();
        ctx.close();
    }
}
