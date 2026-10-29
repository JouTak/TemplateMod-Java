package ru.Jor.BeaconRadius.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.injection.struct.InjectorGroupInfo;
import ru.Jor.BeaconRadius.BRStorage;
import ru.Jor.BeaconRadius.mixin.accessor.BeaconBlockEntityAccessor;
import ru.Jor.BeaconRadius.BeaconRadius;

import java.util.*;

public class RenderAndDraw {
    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    private static BufferBuilder buffer;
    private static MappableRingBuffer vertexBuffer;
    
    public static void renderAndDraw(LevelRenderContext context) {
        if(render(context))
            drawLine(Minecraft.getInstance(), RenderPipelines.LINES);
    }
    
    private static boolean render(LevelRenderContext context) {
        if(BRStorage.blockCache.isEmpty())
         return false;
        ClientLevel world = Minecraft.getInstance().level;

        PoseStack matrices = context.poseStack();
        Vec3 camera = context.levelState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (buffer == null) {
            buffer = new BufferBuilder(ALLOCATOR, RenderPipelines.LINES.getVertexFormatMode(), RenderPipelines.LINES.getVertexFormat());
        }

        boolean hasContent=false;
        List<BlockPos> toDel = new ArrayList<>();
        if (world != null) {
            for(Map.Entry<BlockPos,Boolean> entry:BRStorage.blockCache.entrySet()) {
                if (entry.getValue()) {
                    if (world.getBlockState(entry.getKey()).is(Blocks.BEACON)) {
                        if (renderMyObject(matrices.last().pose(), buffer, entry.getKey(), ((BeaconBlockEntityAccessor) world.getBlockEntity(entry.getKey())).BeaconRadius$getLevels()))
                            hasContent = true;
                    }
                    else
                        toDel.add(entry.getKey());
                }
            }
            toDel.forEach(block ->{BRStorage.blockCache.remove(block);});
            if(!hasContent)
                return false;
        }
        else
            return false;
        matrices.popPose();

        return true;
    }

    private static boolean renderMyObject(Matrix4fc positionMatrix, BufferBuilder buffer, BlockPos pos, int level){
        if(level == 0) return false;

        float radius = (level+1f)*10;
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+radius+1, Float.valueOf(pos.getY()),Float.valueOf(pos.getZ())+radius+1).setColor(0f,0f,1f,1f).setLineWidth(2).setNormal(0,0,0);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+radius+1, Float.valueOf(pos.getY()),Float.valueOf(pos.getZ())-radius  ).setColor(0f,0f,1f,1f).setLineWidth(2).setNormal(0,0,0);

        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+radius+1, Float.valueOf(pos.getY()),Float.valueOf(pos.getZ())-radius  ).setColor(0f,0f,1f,1f).setLineWidth(2).setNormal(0,0,0);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())-radius,   Float.valueOf(pos.getY()),Float.valueOf(pos.getZ())-radius  ).setColor(0f,0f,1f,1f).setLineWidth(2).setNormal(0,0,0);

        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())-radius,   Float.valueOf(pos.getY()),Float.valueOf(pos.getZ())-radius  ).setColor(0f,0f,1f,1).setLineWidth(2).setNormal(0,0,0);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())-radius,   Float.valueOf(pos.getY()),Float.valueOf(pos.getZ())+radius+1).setColor(0f,0f,1f,1f).setLineWidth(2).setNormal(0,0,0);

        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())-radius,   Float.valueOf(pos.getY()),Float.valueOf(pos.getZ())+radius+1).setColor(0f,0f,1f,1f).setLineWidth(2).setNormal(0,0,0);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+radius+1, Float.valueOf(pos.getY()),Float.valueOf(pos.getZ())+radius+1).setColor(0f,0f,1f,1f).setLineWidth(2).setNormal(0,0,0);
        return true;
    }

    private static void drawLine(Minecraft client, RenderPipeline pipeline) {
        MeshData builtBuffer = buffer.buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();

        GpuBuffer vertices = upload(drawParameters, format, builtBuffer);

        draw(client, pipeline, builtBuffer, drawParameters, vertices, format);

        vertexBuffer.rotate();
        buffer = null;
    }

    private static GpuBuffer upload(MeshData.DrawState drawParameters, VertexFormat format, MeshData builtBuffer) {
        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }

            vertexBuffer = new MappableRingBuffer(() -> BeaconRadius.MOD_ID + " example render pipeline", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }

        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        return vertexBuffer.currentBuffer();
    }

    private static void draw(Minecraft client, RenderPipeline pipeline, MeshData builtBuffer, MeshData.DrawState drawParameters, GpuBuffer vertices, VertexFormat format){
        RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
        GpuBuffer indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
        VertexFormat.IndexType indexType = shapeIndexBuffer.type();
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> BeaconRadius.MOD_ID + " example render pipeline rendering", client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(), client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);

            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);

            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);

            renderPass.drawIndexed(0 / format.getVertexSize(), 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
    }

    public void close() {
        ALLOCATOR.close();

        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }
}
