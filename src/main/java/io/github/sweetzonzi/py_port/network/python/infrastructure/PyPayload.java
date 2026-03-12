package io.github.sweetzonzi.py_port.network.python.infrastructure;

public interface PyPayload {
    /**
     * 每个网络包的唯一类型，包含注册名和编解码器
     *
     * @return 网络包类型
     */
    PyPayloadType<?> type();
}
