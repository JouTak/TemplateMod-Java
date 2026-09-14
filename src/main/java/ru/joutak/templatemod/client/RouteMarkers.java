package ru.joutak.templatemod.client;
import java.util.List;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;
import ru.joutak.templatemod.TemplateMod;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Util;

public class RouteMarkers {
    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(
                    TemplateMod.MOD_ID, "textures/waypoint.png"
            );
    private static List<Vec3> renderPoints = List.of();
    private static double bobOffset = 0.0;
    public static void extract(List<Vec3> points) {
        renderPoints = List.copyOf(points);
        double seconds = Util.getMillis() / 1000.0;
        double period = 4.0;
        double amplitude = 0.1;
        bobOffset = Math.sin(seconds * 2 * Math.PI / period) * amplitude;
    }
    public static void draw(LevelRenderContext context) {
        Vec3 camera = context.levelState().cameraRenderState.pos;
        PoseStack matrices = context.poseStack();
        for (Vec3 point : renderPoints) {
            double x = point.x - camera.x;
            double y = point.y - camera.y;
            double z = point.z - camera.z;
            matrices.pushPose();
            matrices.translate(x, y + 0.5 + bobOffset, z);
            matrices.mulPose(context.levelState().cameraRenderState.orientation);

            context.submitNodeCollector().submitCustomGeometry(
                    matrices,
                    RenderTypes.entityTranslucentEmissive(TEXTURE),
                    (pose, buffer) -> {
                        vertex(buffer, pose, -0.25f, -0.25f, 0f, 1f);
                        vertex(buffer, pose, 0.25f, -0.25f, 1f, 1f);
                        vertex(buffer, pose, 0.25f, 0.25f, 1f, 0f);
                        vertex(buffer, pose, -0.25f, 0.25f, 0f, 0f);

                    }
            );
            matrices.popPose();
        }
    }
    private static void vertex(
            VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v
    ) {
        buffer.addVertex(pose,x,y,0f)
                .setColor(255,255,255,255)
                .setUv(u,v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose,0.0F,0.0F, 1f);
    }
}
