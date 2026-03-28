package io.github.sweetzonzi.py_port.network.python.payload;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyContext;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyHandleResult;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayload;
import io.github.sweetzonzi.py_port.network.python.infrastructure.PyPayloadType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;

public record SetPerspectivePayload(
        int mode // 0: 第一人称, 1: 第三人称背面, 2: 第三人称正面
) implements PyPayload {

    public static final Codec<SetPerspectivePayload> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("mode").forGetter(SetPerspectivePayload::mode)
            ).apply(instance, SetPerspectivePayload::new));

    public static final PyPayloadType<SetPerspectivePayload> TYPE =
            new PyPayloadType<>("set_perspective", CODEC);

    @Override
    public PyPayloadType<?> type() {
        return TYPE;
    }

    public static PyHandleResult handle(SetPerspectivePayload payload, PyContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {return PyHandleResult.fail("Minecraft client not available");}
        mc.execute(() -> {
            CameraType cameraType;
            switch (payload.mode()) {
                case 1 -> cameraType = CameraType.THIRD_PERSON_BACK;
                case 2 -> cameraType = CameraType.THIRD_PERSON_FRONT;
                default -> cameraType = CameraType.FIRST_PERSON;
            }
            mc.options.setCameraType(cameraType);
            if (mc.getCameraEntity() != null) {
                mc.gameRenderer.checkEntityPostEffect(mc.getCameraEntity());
            }
        });
        JsonObject data = new JsonObject();
        data.addProperty("status", "success");
        return PyHandleResult.success(data);
    }
}