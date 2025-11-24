package com.bluepaint.advgear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class AdvancedGearboxRenderer extends KineticBlockEntityRenderer<AdvancedGearboxBlockEntity> {

    public AdvancedGearboxRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(AdvancedGearboxBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getLevel())) return;

        final BlockPos pos = be.getBlockPos();
        float time = AnimationTickHolder.getRenderTime(be.getLevel());

        for (Direction direction : Iterate.directions) {
            boolean flag = false;
            switch (direction) {
                case Direction.UP -> flag = be.getBlockState().getValue(BlockStateProperties.UP);
                case Direction.DOWN -> flag = be.getBlockState().getValue(BlockStateProperties.DOWN);
                case Direction.NORTH -> flag = be.getBlockState().getValue(BlockStateProperties.NORTH);
                case Direction.EAST -> flag = be.getBlockState().getValue(BlockStateProperties.EAST);
                case Direction.SOUTH -> flag = be.getBlockState().getValue(BlockStateProperties.SOUTH);
                case Direction.WEST -> flag = be.getBlockState().getValue(BlockStateProperties.WEST);
            }
            if (!flag) continue;

            final Direction.Axis axis = direction.getAxis();

            SuperByteBuffer shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), direction);
            float offset = getRotationOffsetForPosition(be, pos, axis);
            float angle = (time * be.getSpeed() * 3f / 10) % 360;

            if (be.getSpeed() != 0 && be.hasSource()) {
                BlockPos source = be.source.subtract(be.getBlockPos());
                Direction sourceFacing = Direction.getNearest(source.getX(), source.getY(), source.getZ());
                if (sourceFacing.getAxis() == direction.getAxis())
                    angle *= sourceFacing == direction ? 1 : -1;
                else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
                    angle *= -1;
            }

            angle += offset;
            angle = angle / 180f * (float) Math.PI;

            kineticRotationTransform(shaft, be, axis, angle, light);
            shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
        }
    }

}
