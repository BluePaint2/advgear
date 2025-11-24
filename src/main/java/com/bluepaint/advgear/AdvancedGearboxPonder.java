package com.bluepaint.advgear;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerBlock;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class AdvancedGearboxPonder implements PonderPlugin {

    @Override
    public String getModId() {
        return CreateAdvancedGearbox.MODID;
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?,?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.addToTag(AllCreatePonderTags.KINETIC_RELAYS)
                .add(AdvancedGearbox.ADVANCED_GEARBOX);
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?,?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        HELPER.forComponents(AdvancedGearbox.ADVANCED_GEARBOX)
                .addStoryBoard("advanced_gearbox", AdvancedGearboxPonder::advancedGearbox, AllCreatePonderTags.KINETIC_RELAYS)
                .addStoryBoard("advanced_gearbox", AdvancedGearboxPonder::removeAdvancedGearbox);
    }

    public static void advancedGearbox(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("advanced_gearbox", "Relaying rotational force using Advanced Gearboxes");
        scene.configureBasePlate(1, 1, 5);
        scene.setSceneOffsetY(-1);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().fromTo(4, 1, 6, 3, 3, 5), Direction.UP);
        scene.idle(10);

        // Default Values
        BlockPos largeCogBack = util.grid().at(3, 3, 4);
        BlockPos largeCogLeft = util.grid().at(4, 3, 3);
        BlockPos largeCogFront = util.grid().at(3, 3, 2);
        BlockPos largeCogRight = util.grid().at(2, 3, 3);

        BlockPos gearboxCenter = util.grid().at(3,3,3);

        BlockPos smallCogTop = util.grid().at(3,4,3);
        BlockPos smallCogBottom = util.grid().at(3,2,3);

        Selection deployerSelection = util.select().position(largeCogLeft.east())
                .add(util.select().position(largeCogRight.west()));

        // Setup default state
        // Advanced gearbox doesn't work in scenes normally, so we restore at end instead
        {
            BlockState defaultLargeCogState = AllBlocks.LARGE_COGWHEEL.getDefaultState();
            scene.world().setBlock(largeCogBack, defaultLargeCogState.setValue(CogWheelBlock.AXIS, Direction.Axis.Z), false);
            scene.world().setBlock(largeCogLeft, defaultLargeCogState.setValue(CogWheelBlock.AXIS, Direction.Axis.X), false);
            scene.world().setBlock(largeCogFront, defaultLargeCogState.setValue(CogWheelBlock.AXIS, Direction.Axis.Z), false);
            scene.world().setBlock(largeCogRight, defaultLargeCogState.setValue(CogWheelBlock.AXIS, Direction.Axis.X), false);

            BlockState defaultShaftState = AllBlocks.SHAFT.getDefaultState();
            scene.world().setBlock(smallCogTop, defaultShaftState.setValue(CogWheelBlock.AXIS, Direction.Axis.Y), false);
            scene.world().setBlock(smallCogBottom, defaultShaftState.setValue(CogWheelBlock.AXIS, Direction.Axis.Y), false);

            BlockState defaultGearboxState = AllBlocks.GEARBOX.getDefaultState();
            scene.world().setBlock(gearboxCenter, defaultGearboxState.setValue(GearboxBlock.AXIS, Direction.Axis.X), false);
            scene.world().setKineticSpeed(util.select().position(gearboxCenter), 32);
        }

        scene.world().showSection(util.select().position(largeCogBack), Direction.SOUTH);
        scene.idle(5);
        scene.world().showSection(util.select().position(largeCogLeft), Direction.WEST);
        scene.world().showSection(util.select().position(largeCogLeft.east()), Direction.WEST);
        scene.world().showSection(util.select().position(largeCogRight), Direction.EAST);
        scene.world().showSection(util.select().position(largeCogRight.west()), Direction.EAST);
        scene.idle(5);
        scene.world().showSection(util.select().position(largeCogFront), Direction.SOUTH);
        scene.world().showSection(util.select().position(largeCogFront.north()), Direction.SOUTH);
        scene.idle(5);
        scene.world().showSection(util.select().position(gearboxCenter), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(smallCogTop), Direction.DOWN);
        scene.world().showSection(util.select().position(smallCogTop.above()), Direction.DOWN);
        scene.world().showSection(util.select().position(smallCogBottom), Direction.UP);
        scene.world().showSection(util.select().position(smallCogBottom.below()), Direction.UP);

        scene.idle(10);

        scene.overlay().showText(80)
                .colored(PonderPalette.RED)
                .pointAt(util.vector().blockSurface(gearboxCenter.above(), Direction.WEST))
                .placeNearTarget()
                .text("Getting all degrees of freedom is tricky and bulky.");

        scene.idle(80);
        scene.world().hideSection(util.select().fromTo(1, 1, 1, 5, 5, 5), Direction.UP);
        scene.idle(20);

        scene.overlay().showText(80)
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(gearboxCenter))
                .placeNearTarget()
                .attachKeyFrame()
                .text("When placed with cogwheels in the offhand, they will automatically populate.");

        // Set deployers and give default state
        BlockState shaftDefaultState = AllBlocks.SHAFT.getDefaultState().setValue(ShaftBlock.AXIS, Direction.Axis.Z);
        scene.world().setBlock(largeCogBack, shaftDefaultState, false);
        scene.world().setBlock(largeCogBack.south(), shaftDefaultState, false);
        scene.world().setBlock(largeCogFront, shaftDefaultState, false);
        scene.world().setBlock(largeCogFront.north(), shaftDefaultState, false);

        scene.world().showSection(
                util.select().position(largeCogBack)
                        .add(util.select().position(largeCogBack.south()))
                        .add(util.select().position(largeCogFront))
                        .add(util.select().position(largeCogFront.north()))
                , Direction.UP);

        scene.idle(20);

        Vec3 cogwheelImageVec = util.vector().blockSurface(gearboxCenter, Direction.NORTH);
        scene.overlay().showControls(cogwheelImageVec, Pointing.RIGHT, 25)
                .withItem(AllBlocks.COGWHEEL.asStack());

        BlockState gearboxState = AdvancedGearbox.ADVANCED_GEARBOX.getDefaultState();
        scene.world().setBlock(gearboxCenter, gearboxState, false);
        scene.world().showSection(util.select().position(gearboxCenter), Direction.UP);

        scene.idle(10);

        // Set gearbox to moving variant with the proper source
        scene.world().setKineticSpeed(util.select().position(gearboxCenter), -32);
        scene.world().modifyBlockEntityNBT(util.select().position(gearboxCenter), KineticBlockEntity.class, nbt -> {
            nbt.put("Source", NbtUtils.writeBlockPos(largeCogBack));
        });

        gearboxState = gearboxState
                .setValue(AdvancedGearbox.NORTH, true)
                .setValue(AdvancedGearbox.SOUTH, true);
        scene.world().setBlock(gearboxCenter, gearboxState, true);

        scene.idle(75);

        scene.overlay().showText(80)
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(gearboxCenter))
                .placeNearTarget()
                .attachKeyFrame()
                .text("You can use small cogwheels to add outputs to the gearbox.");

        scene.idle(50);
        {
            Vec3 cogwheelHere = util.vector().blockSurface(gearboxCenter, Direction.UP);
            scene.overlay().showControls(cogwheelHere, Pointing.DOWN, 25).rightClick()
                    .withItem(AllBlocks.COGWHEEL.asStack());
        }
        scene.idle(30);

        gearboxState = gearboxState
                .setValue(AdvancedGearbox.UP, true)
                .setValue(AdvancedGearbox.DOWN, true);

        scene.world().setBlock(gearboxCenter, gearboxState, true);

        scene.idle(20);

        scene.overlay().showText(80)
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(gearboxCenter))
                .placeNearTarget()
                .attachKeyFrame()
                .text("You can also use deployers.");

        // Set deployers and give default state
        BlockState defaultState = AllBlocks.DEPLOYER.getDefaultState();
        scene.world().setBlock(largeCogLeft.east(), defaultState.setValue(DeployerBlock.FACING, Direction.WEST), false);
        scene.world().setBlock(largeCogRight.west(), defaultState.setValue(DeployerBlock.FACING, Direction.EAST), false);
        scene.world().showSection(deployerSelection, Direction.DOWN);

        // Set deployer item to cogwheel
        Class<DeployerBlockEntity> teType = DeployerBlockEntity.class;
        scene.world().modifyBlockEntityNBT(deployerSelection, teType,
                nbt -> nbt.put("HeldItem", AllBlocks.COGWHEEL.asStack().saveOptional(scene.world().getHolderLookupProvider())));

        scene.idle(20);

        // Move Deployers and set gearbox to show all shafts
        for (BlockPos pos : deployerSelection) {
            scene.world().moveDeployer(pos, 1, 25);
        }
        scene.idle(26);
        for (BlockPos pos : deployerSelection) {
            scene.world().moveDeployer(pos, -1, 25);
            scene.world().modifyBlockEntityNBT(util.select().position(pos), teType,
                    nbt -> nbt.put("HeldItem", ItemStack.EMPTY.saveOptional(scene.world().getHolderLookupProvider())));
        }

        scene.world().setBlock(gearboxCenter, gearboxState
                        .setValue(AdvancedGearbox.WEST, true)
                        .setValue(AdvancedGearbox.EAST, true)
                , true);

        // Hide deployers
        scene.idle(44);
        scene.world().hideSection(deployerSelection, Direction.UP);
        scene.idle(20);

        // Restore scene to original
        scene.world().restoreBlocks(util.select().fromTo(1,1,1,5,5,5)
                .substract(util.select().position(gearboxCenter))
                .substract(util.select().position(largeCogBack))
                .substract(util.select().position(largeCogBack.south()))
                .substract(util.select().position(largeCogFront))
                .substract(util.select().position(largeCogFront.north()))
        );
        scene.world().showSection(util.select().fromTo(1,1,1,5,5,5)
                .substract(util.select().position(gearboxCenter))
                .substract(util.select().position(largeCogBack))
                .substract(util.select().position(largeCogBack.south()))
                .substract(util.select().position(largeCogFront))
                .substract(util.select().position(largeCogFront.north()))
                , Direction.DOWN);

        scene.effects().rotationDirectionIndicator(largeCogBack.south());
        scene.effects().rotationDirectionIndicator(largeCogFront.north());
        scene.effects().rotationDirectionIndicator(largeCogRight.west());
        scene.effects().rotationDirectionIndicator(largeCogLeft.east());
        scene.effects().rotationDirectionIndicator(smallCogTop.above());
        scene.effects().rotationDirectionIndicator(smallCogBottom.below());

        scene.idle(20);
        scene.overlay().showText(80)
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(3, 2, 3))
                .placeNearTarget()
                .text("An advanced gearbox is a dynamic and compact version of using large cogwheels.");
    }

    public static void removeAdvancedGearbox(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("advanced_gearbox_remove", "Removing an Advanced Gearbox");
        scene.configureBasePlate(1, 1, 5);
        scene.setSceneOffsetY(-1);

        scene.world().showSection(util.select().layer(0), Direction.UP);
        scene.world().showSection(util.select().fromTo(4, 1, 6, 3, 3, 5), Direction.UP);
        scene.idle(10);

        // Default Values
        BlockPos shaftBack = util.grid().at(3, 3, 4);
        BlockPos shaftLeft = util.grid().at(4, 3, 3);
        BlockPos shaftFront = util.grid().at(3, 3, 2);
        BlockPos shaftRight = util.grid().at(2, 3, 3);

        BlockPos gearboxCenter = util.grid().at(3,3,3);

        BlockPos shaftTop = util.grid().at(3,4,3);
        BlockPos shaftBottom = util.grid().at(3,2,3);

        Selection shaftSelection = util.select().position(shaftLeft)
                .add(util.select().position(shaftLeft.east()))
                .add(util.select().position(shaftFront))
                .add(util.select().position(shaftFront.north()))
                .add(util.select().position(shaftRight))
                .add(util.select().position(shaftRight.west()))
                .add(util.select().position(shaftTop))
                .add(util.select().position(shaftTop.above()))
                .add(util.select().position(shaftBottom))
                .add(util.select().position(shaftBottom.below()));

        Selection deployerSelection = util.select().position(shaftLeft.east())
                .add(util.select().position(shaftFront.north()))
                .add(util.select().position(shaftRight.west()));

        // Set gearbox to moving variant with the proper source
        scene.world().setKineticSpeed(util.select().position(gearboxCenter), -32);
        scene.world().modifyBlockEntityNBT(util.select().position(gearboxCenter), KineticBlockEntity.class, nbt -> {
            nbt.put("Source", NbtUtils.writeBlockPos(shaftBack));
        });

        scene.world().showSection(util.select().position(shaftBack), Direction.SOUTH);
        scene.idle(5);
        scene.world().showSection(util.select().position(shaftLeft), Direction.WEST);
        scene.world().showSection(util.select().position(shaftLeft.east()), Direction.WEST);
        scene.world().showSection(util.select().position(shaftRight), Direction.EAST);
        scene.world().showSection(util.select().position(shaftRight.west()), Direction.EAST);
        scene.idle(5);
        scene.world().showSection(util.select().position(shaftFront), Direction.SOUTH);
        scene.world().showSection(util.select().position(shaftFront.north()), Direction.SOUTH);
        scene.idle(5);
        scene.world().showSection(util.select().position(gearboxCenter), Direction.DOWN);
        scene.idle(5);
        scene.world().showSection(util.select().position(shaftTop), Direction.DOWN);
        scene.world().showSection(util.select().position(shaftTop.above()), Direction.DOWN);
        scene.world().showSection(util.select().position(shaftBottom), Direction.UP);
        scene.world().showSection(util.select().position(shaftBottom.below()), Direction.UP);

        scene.idle(10);

        scene.overlay().showText(80)
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().blockSurface(gearboxCenter.above(), Direction.WEST))
                .placeNearTarget()
                .text("You may wish to remove a cogwheel from the gearbox.");

        scene.idle(80);

        scene.world().hideSection(shaftSelection, Direction.UP);

        scene.idle(20);

        scene.overlay().showText(80)
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().blockSurface(gearboxCenter.above(), Direction.WEST))
                .placeNearTarget()
                .attachKeyFrame()
                .text("You can use a wrench to remove cogwheels.");

        scene.idle(80);

        Vec3 wrenchHere = util.vector().blockSurface(gearboxCenter, Direction.UP);
        scene.overlay().showControls(wrenchHere, Pointing.RIGHT, 25).rightClick()
                .withItem(AllItems.WRENCH.asStack());

        scene.idle(30);

        BlockState gearboxState = AdvancedGearbox.ADVANCED_GEARBOX.getDefaultState()
                .setValue(AdvancedGearbox.NORTH, true)
                .setValue(AdvancedGearbox.EAST, true)
                .setValue(AdvancedGearbox.SOUTH, true)
                .setValue(AdvancedGearbox.WEST, true);

        scene.world().setBlock(gearboxCenter, gearboxState, true);

        scene.idle(20);

        scene.overlay().showText(80)
                .colored(PonderPalette.GREEN)
                .pointAt(util.vector().topOf(gearboxCenter))
                .placeNearTarget()
                .attachKeyFrame()
                .text("You can also use deployers.");

        // Set deployers and give default state
        BlockState defaultState = AllBlocks.DEPLOYER.getDefaultState();
        scene.world().setBlock(shaftLeft.east(), defaultState.setValue(DeployerBlock.FACING, Direction.WEST), false);
        scene.world().setBlock(shaftFront.north(), defaultState.setValue(DeployerBlock.FACING, Direction.SOUTH), false);
        scene.world().setBlock(shaftRight.west(), defaultState.setValue(DeployerBlock.FACING, Direction.EAST), false);
        scene.world().showSection(deployerSelection, Direction.DOWN);

        // Set deployer item to nothing
        Class<DeployerBlockEntity> teType = DeployerBlockEntity.class;
        scene.world().modifyBlockEntityNBT(deployerSelection, teType,
                nbt -> nbt.put("HeldItem", ItemStack.EMPTY.saveOptional(scene.world().getHolderLookupProvider())));

        scene.idle(20);

        // Move Deployers and set gearbox to show all shafts
        for (BlockPos pos : deployerSelection) {
            scene.world().moveDeployer(pos, 1, 25);
        }
        scene.idle(26);
        for (BlockPos pos : deployerSelection) {
            scene.world().moveDeployer(pos, -1, 25);
            scene.world().modifyBlockEntityNBT(util.select().position(pos), teType,
                    nbt -> nbt.put("HeldItem", AllBlocks.COGWHEEL.asStack().saveOptional(scene.world().getHolderLookupProvider())));
        }

        scene.world().setBlock(gearboxCenter, gearboxState
                        .setValue(AdvancedGearbox.NORTH, false)
                        .setValue(AdvancedGearbox.EAST, false)
                        .setValue(AdvancedGearbox.SOUTH, false)
                        .setValue(AdvancedGearbox.WEST, false)
                , true);

        scene.idle(44);

    }
}
