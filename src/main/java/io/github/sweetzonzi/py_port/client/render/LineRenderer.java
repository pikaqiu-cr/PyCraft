package io.github.sweetzonzi.py_port.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.sweetzonzi.py_port.PyCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = PyCraft.MOD_ID, value = Dist.CLIENT)
public class LineRenderer {

    private static final List<Line> LINES = new ArrayList<>();

    public static void addLine(Vec3 from, Vec3 to, int color, int life) {
        LINES.add(new Line(from, to, color, life));
    }

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();

        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());

        var matrix = poseStack.last().pose();

        Iterator<Line> it = LINES.iterator();
        while (it.hasNext()) {
            Line l = it.next();

            buffer.addVertex(matrix, (float) l.from.x, (float) l.from.y, (float) l.from.z)
                    .setNormal((float) (l.to.x-l.from.x), (float) (l.to.y-l.from.y), (float) (l.to.z-l.from.z))
                    .setColor(Color.WHITE.getRGB());

            buffer.addVertex(matrix, (float) l.to.x, (float) l.to.y, (float) l.to.z)
                    .setNormal((float) (l.to.x-l.from.x), (float) (l.to.y-l.from.y), (float) (l.to.z-l.from.z))
                    .setColor(Color.WHITE.getRGB());

            if (--l.life <= 0) it.remove();
        }

        bufferSource.endBatch(RenderType.gui());

        poseStack.popPose();
    }

    private static class Line {
        Vec3 from, to;
        int color;
        int life;

        Line(Vec3 f, Vec3 t, int c, int l) {
            from = f;
            to = t;
            color = c;
            life = l;
        }
    }
}