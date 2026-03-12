package io.github.sweetzonzi.py_port.network.python.infrastructure;

@FunctionalInterface
public interface PyPayloadHandler<T extends PyPayload> {
    //TODO: 分为同步处理器和异步处理器，可能通过CompletableFuture来实现
    PyHandleResult handle(T payload, PyContext ctx);
}
