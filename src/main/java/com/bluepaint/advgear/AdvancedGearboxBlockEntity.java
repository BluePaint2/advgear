package com.bluepaint.advgear;

import com.simibubi.create.content.kinetics.gearbox.GearboxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedGearboxBlockEntity extends GearboxBlockEntity {

    public AdvancedGearboxBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
