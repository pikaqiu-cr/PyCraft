package io.github.sweetzonzi.py_port.network.python.infrastructure;

import io.github.sweetzonzi.py_port.PyCraft;
import io.github.sweetzonzi.py_port.network.python.PyPayloadRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

@EventBusSubscriber
public final class PyNetwork {

    private static EventLoopGroup boss;
    private static EventLoopGroup worker;
    private static Channel serverChannel;

    public static void start(int port) {
        boss = new NioEventLoopGroup(1);
        worker = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(new PyChannelInitializer());

        try {
            serverChannel = bootstrap.bind(port).sync().channel();
            PyCraft.LOGGER.info("[PyNetwork] PyCraft Netty server started on port {}", port);
            PyPayloadRegistry.registerAll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stop() {
        if (serverChannel != null) serverChannel.close();
        if (worker != null) worker.shutdownGracefully();
        if (boss != null) boss.shutdownGracefully();
        PyCraft.LOGGER.info("[PyNetwork] PyCraft Netty server stopped");
    }

    @SubscribeEvent
    public static void onSeverStart(ServerStartedEvent event) {
        start(8086);
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppingEvent event) {
        stop();
    }
}
