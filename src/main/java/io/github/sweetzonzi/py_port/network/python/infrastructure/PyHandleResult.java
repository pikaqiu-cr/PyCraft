package io.github.sweetzonzi.py_port.network.python.infrastructure;

import com.google.gson.JsonObject;

public record PyHandleResult(
        boolean isSuccess,
        String errorMessage,
        JsonObject data
) {
    public static PyHandleResult success(JsonObject data) {
        return new PyHandleResult(true, null, data);
    }

    public static PyHandleResult success() {
        return new PyHandleResult(true, null, null);
    }

    public static PyHandleResult fail(String errorMessage) {
        return new PyHandleResult(false, errorMessage, null);
    }
}
