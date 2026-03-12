package io.github.sweetzonzi.py_port.network.python.infrastructure;

import com.mojang.serialization.Codec;

public record PyPayloadType<T extends PyPayload>(String type, Codec<T> codec) {
    // 可以添加工厂方法，如 of(String id, Codec<T> codec)
}
