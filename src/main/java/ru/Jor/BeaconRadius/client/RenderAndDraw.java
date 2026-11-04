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
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;
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
            drawLine(Minecraft.getInstance(), RenderPipelines.DEBUG_QUADS);
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
            buffer = new BufferBuilder(ALLOCATOR, RenderPipelines.DEBUG_QUADS.getVertexFormatMode(), RenderPipelines.DEBUG_QUADS.getVertexFormat());
        }

        boolean hasContent=false;
        Map<BlockPos, Identifier> toDel = new HashMap<>();

        if (world != null) {
            for(Map.Entry<BlockPos,Identifier> block:BRStorage.blockCache.entrySet()) {
                if (world.isLoaded(block.getKey())&&(block.getValue().equals(world.dimension().identifier()))){
                    if (world.getBlockState(block.getKey()).is(Blocks.BEACON)) {
                        if (!((BeaconBlockEntityAccessor) world.getBlockEntity(block.getKey())).BeaconRadius$getbeamSections().isEmpty())
                            if (renderMyObject(matrices.last(), buffer, block.getKey(),
                                ((BeaconBlockEntityAccessor) world.getBlockEntity(block.getKey())).BeaconRadius$getLevels(),world.getGameTime(),
                                world.getMaxY()))
                                hasContent = true;
                    }
                    else
                        toDel.put(block.getKey(),block.getValue());
                }
            }
            toDel.forEach((block,dim) ->{BRStorage.blockCache.remove(block,dim);});
            if(!hasContent)
                return false;
        }
        else
            return false;
        matrices.popPose();

        return true;
    }

    private static boolean renderMyObject(PoseStack.Pose positionMatrix, BufferBuilder buffer, BlockPos pos, int level, long tick, int maxheight){
        if(level == 0) return false;

        float radius = (level+1f)*10;

        float width = 1f/20f;
        //top-middle rings
        for (float Y=maxheight;Y>(pos.getY()-radius);Y=Y-16) {
            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1,    Y ,         Float.valueOf(pos.getZ()) + radius + 1).setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);
            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1,    Y ,         Float.valueOf(pos.getZ()) - radius)    .setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);
            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1, Y + width , Float.valueOf(pos.getZ()) - radius)    .setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);
            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1, Y + width , Float.valueOf(pos.getZ()) + radius + 1).setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);

            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1,    Y ,         Float.valueOf(pos.getZ()) - radius)     .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);
            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,        Y ,         Float.valueOf(pos.getZ()) - radius)     .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);
            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,     Y + width , Float.valueOf(pos.getZ()) - radius)     .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);
            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1, Y + width , Float.valueOf(pos.getZ()) - radius)     .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);

            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,        Y ,         Float.valueOf(pos.getZ()) - radius)     .setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);
            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,        Y ,         Float.valueOf(pos.getZ()) + radius + 1) .setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);
            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,     Y + width , Float.valueOf(pos.getZ()) + radius + 1) .setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);
            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,     Y + width , Float.valueOf(pos.getZ()) - radius)     .setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);

            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,        Y ,         Float.valueOf(pos.getZ()) + radius + 1) .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);
            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1,    Y ,         Float.valueOf(pos.getZ()) + radius + 1) .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);
            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1, Y + width , Float.valueOf(pos.getZ()) + radius + 1) .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);
            buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,     Y + width , Float.valueOf(pos.getZ()) + radius + 1) .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);
        }
        //bottom ring
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1, pos.getY()-radius,         Float.valueOf(pos.getZ()) + radius + 1).setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1, pos.getY()-radius,         Float.valueOf(pos.getZ()) - radius)    .setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1, pos.getY()-radius + width, Float.valueOf(pos.getZ()) - radius)    .setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1, pos.getY()-radius + width, Float.valueOf(pos.getZ()) + radius + 1).setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);

        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1, pos.getY()-radius ,         Float.valueOf(pos.getZ()) - radius)     .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,     pos.getY()-radius ,         Float.valueOf(pos.getZ()) - radius)     .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,     pos.getY()-radius + width, Float.valueOf(pos.getZ()) - radius)     .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1, pos.getY()-radius + width, Float.valueOf(pos.getZ()) - radius)     .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);

        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,     pos.getY()-radius ,         Float.valueOf(pos.getZ()) - radius)     .setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,     pos.getY()-radius ,         Float.valueOf(pos.getZ()) + radius + 1) .setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,     pos.getY()-radius + width , Float.valueOf(pos.getZ()) + radius + 1) .setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,     pos.getY()-radius + width , Float.valueOf(pos.getZ()) - radius)     .setColor(0f, 0f, 1f, 1f).setNormal(1, 0, 0);

        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,     pos.getY()-radius ,         Float.valueOf(pos.getZ()) + radius + 1) .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1, pos.getY()-radius ,         Float.valueOf(pos.getZ()) + radius + 1) .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) + radius + 1, pos.getY()-radius + width , Float.valueOf(pos.getZ()) + radius + 1) .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX()) - radius,     pos.getY()-radius + width , Float.valueOf(pos.getZ()) + radius + 1) .setColor(0f, 0f, 1f, 1f).setNormal(0, 0, 1);

        double angle = Math.toRadians((tick%451)/5f-45f);
        //criss-cross,beacon level
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+radius+1, Float.valueOf(pos.getY())+0.5f,      Float.valueOf(pos.getZ())+0.5f+((float) Math.tan(angle))*radius).setColor(1f,0f,0f,1f).setNormal((float) Math.sin(angle),0,(float) Math.cos(angle));
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())-radius  , Float.valueOf(pos.getY())+0.5f,      Float.valueOf(pos.getZ())+0.5f-((float) Math.tan(angle))*radius).setColor(1f,0f,0f,1f).setNormal((float) Math.sin(angle),0,(float) Math.cos(angle));
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())-radius  , Float.valueOf(pos.getY())+0.5f+width,Float.valueOf(pos.getZ())+0.5f-((float) Math.tan(angle))*radius).setColor(1f,0f,0f,1f).setNormal((float) Math.sin(angle),0,(float) Math.cos(angle));
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+radius+1, Float.valueOf(pos.getY())+0.5f+width,Float.valueOf(pos.getZ())+0.5f+((float) Math.tan(angle))*radius).setColor(1f,0f,0f,1f).setNormal((float) Math.sin(angle),0,(float) Math.cos(angle));

        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f-((float) Math.tan(angle))*radius, Float.valueOf(pos.getY())+0.5f,      Float.valueOf(pos.getZ())+radius+1).setColor(1f,0f,0f,1f).setNormal((float) Math.cos(angle),0,(float) Math.sin(angle));
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f+((float) Math.tan(angle))*radius, Float.valueOf(pos.getY())+0.5f,      Float.valueOf(pos.getZ())-radius  ).setColor(1f,0f,0f,1f).setNormal((float) Math.cos(angle),0,(float) Math.sin(angle));
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f+((float) Math.tan(angle))*radius, Float.valueOf(pos.getY())+0.5f+width,Float.valueOf(pos.getZ())-radius  ).setColor(1f,0f,0f,1f).setNormal((float) Math.cos(angle),0,(float) Math.sin(angle));
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f-((float) Math.tan(angle))*radius, Float.valueOf(pos.getY())+0.5f+width,Float.valueOf(pos.getZ())+radius+1).setColor(1f,0f,0f,1f).setNormal((float) Math.cos(angle),0,(float) Math.sin(angle));
        //criss-cross, bottom layer
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+radius+1, Float.valueOf(pos.getY())-radius,      Float.valueOf(pos.getZ())+0.5f+((float) Math.tan(angle))*radius).setColor(1f,0f,0f,1f).setNormal((float) Math.sin(angle),0,(float) Math.cos(angle));
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())-radius  , Float.valueOf(pos.getY())-radius,      Float.valueOf(pos.getZ())+0.5f-((float) Math.tan(angle))*radius).setColor(1f,0f,0f,1f).setNormal((float) Math.sin(angle),0,(float) Math.cos(angle));
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())-radius  , Float.valueOf(pos.getY())-radius+width,Float.valueOf(pos.getZ())+0.5f-((float) Math.tan(angle))*radius).setColor(1f,0f,0f,1f).setNormal((float) Math.sin(angle),0,(float) Math.cos(angle));
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+radius+1, Float.valueOf(pos.getY())-radius+width,Float.valueOf(pos.getZ())+0.5f+((float) Math.tan(angle))*radius).setColor(1f,0f,0f,1f).setNormal((float) Math.sin(angle),0,(float) Math.cos(angle));

        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f-((float) Math.tan(angle))*radius, Float.valueOf(pos.getY())-radius,      Float.valueOf(pos.getZ())+radius+1).setColor(1f,0f,0f,1f).setNormal((float) Math.cos(angle),0,(float) Math.sin(angle));
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f+((float) Math.tan(angle))*radius, Float.valueOf(pos.getY())-radius,      Float.valueOf(pos.getZ())-radius  ).setColor(1f,0f,0f,1f).setNormal((float) Math.cos(angle),0,(float) Math.sin(angle));
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f+((float) Math.tan(angle))*radius, Float.valueOf(pos.getY())-radius+width,Float.valueOf(pos.getZ())-radius  ).setColor(1f,0f,0f,1f).setNormal((float) Math.cos(angle),0,(float) Math.sin(angle));
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f-((float) Math.tan(angle))*radius, Float.valueOf(pos.getY())-radius+width,Float.valueOf(pos.getZ())+radius+1).setColor(1f,0f,0f,1f).setNormal((float) Math.cos(angle),0,(float) Math.sin(angle));
        //vertical lines
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+radius+1,    Float.valueOf(maxheight),        Float.valueOf(pos.getZ())+0.5f+((float) Math.tan(angle))*radius-width/2).setColor(1f,0f,0f,1f).setNormal(1,0,0);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+radius+1, Float.valueOf(pos.getY())-radius,Float.valueOf(pos.getZ())+0.5f+((float) Math.tan(angle))*radius-width/2).setColor(1f,0f,0f,1f).setNormal(1,0,0);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+radius+1, Float.valueOf(pos.getY())-radius,Float.valueOf(pos.getZ())+0.5f+((float) Math.tan(angle))*radius+width/2).setColor(1f,0f,0f,1f).setNormal(1,0,0);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+radius+1,    Float.valueOf(maxheight),        Float.valueOf(pos.getZ())+0.5f+((float) Math.tan(angle))*radius+width/2).setColor(1f,0f,0f,1f).setNormal(1,0,0);

        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())-radius,    Float.valueOf(maxheight),        Float.valueOf(pos.getZ())+0.5f-((float) Math.tan(angle))*radius-width/2).setColor(1f,0f,0f,1f).setNormal(1,0,0);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())-radius, Float.valueOf(pos.getY())-radius,Float.valueOf(pos.getZ())+0.5f-((float) Math.tan(angle))*radius-width/2).setColor(1f,0f,0f,1f).setNormal(1,0,0);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())-radius, Float.valueOf(pos.getY())-radius,Float.valueOf(pos.getZ())+0.5f-((float) Math.tan(angle))*radius+width/2).setColor(1f,0f,0f,1f).setNormal(1,0,0);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())-radius,    Float.valueOf(maxheight),        Float.valueOf(pos.getZ())+0.5f-((float) Math.tan(angle))*radius+width/2).setColor(1f,0f,0f,1f).setNormal(1,0,0);

        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f-((float) Math.tan(angle))*radius-width,    Float.valueOf(maxheight),        Float.valueOf(pos.getZ())+radius+1).setColor(1f,0f,0f,1f).setNormal(0,0,1);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f-((float) Math.tan(angle))*radius-width, Float.valueOf(pos.getY())-radius,Float.valueOf(pos.getZ())+radius+1).setColor(1f,0f,0f,1f).setNormal(0,0,1);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f-((float) Math.tan(angle))*radius+width, Float.valueOf(pos.getY())-radius,Float.valueOf(pos.getZ())+radius+1).setColor(1f,0f,0f,1f).setNormal(0,0,1);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f-((float) Math.tan(angle))*radius+width,    Float.valueOf(maxheight),        Float.valueOf(pos.getZ())+radius+1).setColor(1f,0f,0f,1f).setNormal(0,0,1);

        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f+((float) Math.tan(angle))*radius-width,    Float.valueOf(maxheight),        Float.valueOf(pos.getZ())-radius).setColor(1f,0f,0f,1f).setNormal(0,0,1);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f+((float) Math.tan(angle))*radius-width, Float.valueOf(pos.getY())-radius,Float.valueOf(pos.getZ())-radius).setColor(1f,0f,0f,1f).setNormal(0,0,1);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f+((float) Math.tan(angle))*radius+width, Float.valueOf(pos.getY())-radius,Float.valueOf(pos.getZ())-radius).setColor(1f,0f,0f,1f).setNormal(0,0,1);
        buffer.addVertex(positionMatrix,Float.valueOf(pos.getX())+0.5f+((float) Math.tan(angle))*radius+width,    Float.valueOf(maxheight),        Float.valueOf(pos.getZ())-radius).setColor(1f,0f,0f,1f).setNormal(0,0,1);
        float zFightOfset = 0.0001f;
        //beacon block highlight
        //X
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())-zFightOfset,Float.valueOf(pos.getY())+1,Float.valueOf(pos.getZ())+0).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())-zFightOfset,Float.valueOf(pos.getY())+1,Float.valueOf(pos.getZ())+1).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())-zFightOfset,Float.valueOf(pos.getY())+0,Float.valueOf(pos.getZ())+1).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())-zFightOfset,Float.valueOf(pos.getY())+0,Float.valueOf(pos.getZ())+0).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);

        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+1+zFightOfset,Float.valueOf(pos.getY())+1,Float.valueOf(pos.getZ())+0).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+1+zFightOfset,Float.valueOf(pos.getY())+1,Float.valueOf(pos.getZ())+1).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+1+zFightOfset,Float.valueOf(pos.getY())+0,Float.valueOf(pos.getZ())+1).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+1+zFightOfset,Float.valueOf(pos.getY())+0,Float.valueOf(pos.getZ())+0).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        //Y
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+0,Float.valueOf(pos.getY())-zFightOfset,Float.valueOf(pos.getZ())+0).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+0,Float.valueOf(pos.getY())-zFightOfset,Float.valueOf(pos.getZ())+1).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+1,Float.valueOf(pos.getY())-zFightOfset,Float.valueOf(pos.getZ())+1).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+1,Float.valueOf(pos.getY())-zFightOfset,Float.valueOf(pos.getZ())+0).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);

        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+0,Float.valueOf(pos.getY())+1+zFightOfset,Float.valueOf(pos.getZ())+0).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+0,Float.valueOf(pos.getY())+1+zFightOfset,Float.valueOf(pos.getZ())+1).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+1,Float.valueOf(pos.getY())+1+zFightOfset,Float.valueOf(pos.getZ())+1).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+1,Float.valueOf(pos.getY())+1+zFightOfset,Float.valueOf(pos.getZ())+0).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        //Z
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+0,Float.valueOf(pos.getY())+1,Float.valueOf(pos.getZ())-zFightOfset).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+1,Float.valueOf(pos.getY())+1,Float.valueOf(pos.getZ())-zFightOfset).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+1,Float.valueOf(pos.getY())+0,Float.valueOf(pos.getZ())-zFightOfset).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+0,Float.valueOf(pos.getY())+0,Float.valueOf(pos.getZ())-zFightOfset).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);

        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+0,Float.valueOf(pos.getY())+1,Float.valueOf(pos.getZ())+1+zFightOfset).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+1,Float.valueOf(pos.getY())+1,Float.valueOf(pos.getZ())+1+zFightOfset).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+1,Float.valueOf(pos.getY())+0,Float.valueOf(pos.getZ())+1+zFightOfset).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);
        buffer.addVertex(positionMatrix, Float.valueOf(pos.getX())+0,Float.valueOf(pos.getY())+0,Float.valueOf(pos.getZ())+1+zFightOfset).setColor(0f, 1f, 0f, 0.25f).setNormal(1, 0, 0);

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
