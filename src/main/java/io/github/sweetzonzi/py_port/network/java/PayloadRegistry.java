package io.github.sweetzonzi.py_port.network.java;

import io.github.sweetzonzi.py_port.PyCraft;
import io.github.sweetzonzi.py_port.network.java.payload.AgentComponentSyncPayload;
import io.github.sweetzonzi.py_port.network.java.payload.AgentCreatePayload;
import io.github.sweetzonzi.py_port.network.java.payload.AgentRemovePayload;
import io.github.sweetzonzi.py_port.network.java.payload.AgentSyncPayload;
import io.github.sweetzonzi.py_port.network.java.payload.DrawLinePayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = PyCraft.MOD_ID)
public class PayloadRegistry {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar agent = event.registrar("agent:1.0.0");
        final PayloadRegistrar client = event.registrar("line:1.0.0");
        agent.playToClient( // 服务端向客户端同步智能体数据
                AgentSyncPayload.TYPE,
                AgentSyncPayload.STREAM_CODEC,
                AgentSyncPayload::handler
        );
        agent.playToClient( // 服务端通知客户端新建智能体
                AgentCreatePayload.TYPE,
                AgentCreatePayload.STREAM_CODEC,
                AgentCreatePayload::handler
        );
        agent.playToClient( // 服务端通知客户端移除智能体
                AgentRemovePayload.TYPE,
                AgentRemovePayload.STREAM_CODEC,
                AgentRemovePayload::handler
        );
        agent.playToClient( // 服务端向客户端同步智能体组件数据
                AgentComponentSyncPayload.TYPE,
                AgentComponentSyncPayload.STREAM_CODEC,
                AgentComponentSyncPayload::handler
        );
        client.playToClient( // 服务端向客户端发送绘制线指令
                DrawLinePayload.TYPE,
                DrawLinePayload.STREAM_CODEC,
                DrawLinePayload::handle
        );
    }
}
