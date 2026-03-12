package io.github.sweetzonzi.py_port.network.python.infrastructure;

import com.google.gson.JsonObject;

import java.util.UUID;

public record PyMessage(String type, UUID uuid, JsonObject data) {
}
