package com.bluepaint.advgear;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.TickPriority;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;

public class AdvancedGearbox extends KineticBlock implements IBE<AdvancedGearboxBlockEntity> {

    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;

    static {
        CreateAdvancedGearbox.REGISTRATE.setCreativeTab(CreateAdvancedGearbox.MAIN_TAB);
    }

    // TODO: complete this
    public static <T extends Block> void advgearBlock(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov) {

        class Output {
            public ModelFile modelFile = null;
            public int xRot = 0;
            public int yRot = 0;
        }

        Function<BlockState, Output> function = state -> {
            ArrayList<Direction> usedDirections = new ArrayList<>();
            ArrayList<Direction> unusedDirections = new ArrayList<>();
            getAllDirections(state, usedDirections, unusedDirections);
            StringBuilder qualifiers = new StringBuilder();
            qualifiers.append(usedDirections.size());

            Output output = new Output();

            switch (usedDirections.size()) {
                case 1 -> {
                    output.xRot = usedDirections.getFirst() == Direction.DOWN ? 180 : (usedDirections.getFirst().getNormal().getX() - usedDirections.getFirst().getNormal().getZ()) * 90;
                    output.yRot = usedDirections.getFirst().getAxis() == Direction.Axis.X ? 90 : 0;
                }
                case 2 -> {
                    {
                        // Bit shift right gets rid of positive/negative
                        if (usedDirections.getFirst().ordinal() >> 1 == usedDirections.getLast().ordinal() >> 1) {
                            output.xRot = usedDirections.getFirst().getAxis() != Direction.Axis.Y ? 90 : 0;
                            output.yRot = usedDirections.getFirst().getAxis() == Direction.Axis.X ? 90 : 0;
                            qualifiers.append("_straight");
                            break;
                        }
                    }
                    qualifiers.append("_");
                    {
                        Direction.Axis axis = getAxis(usedDirections.getFirst(), usedDirections.getLast());
                        qualifiers.append(axis.toString());

                        // Mathematical patterns in the clockwise rotation
                        switch (axis) {
                            case X -> {
                                output.xRot = usedDirections.contains(Direction.DOWN) ? 180 : 0;
                                output.yRot = usedDirections.getFirst().getAxisDirection() != usedDirections.getLast().getAxisDirection() ? 180 : 0;
                            }
                            case Y -> {
                                output.xRot = usedDirections.getFirst().getAxisDirection() != usedDirections.getLast().getAxisDirection() ? 180 : 0;
                                output.yRot = usedDirections.contains(Direction.WEST) ? 180 : 0;
                            }
                            case Z -> {
                                output.xRot = usedDirections.contains(Direction.DOWN) ? 180 : 0;
                                output.yRot = usedDirections.contains(Direction.WEST) ? 180 : 0;
                            }
                        }
                    }
                }
                case 3 -> {
                    qualifiers.append("_");

                    Direction center = null;
                    Direction.Axis axis = null;

                    // Get axis by finding the two unused sides that are opposite, if they dont exist, it must be a corner
                    // Center is the opposite of the other side
                    for (Direction unusedDirection : unusedDirections) {
                        if (unusedDirections.contains(unusedDirection.getOpposite())) {
                            axis = unusedDirection.getAxis();
                        } else {
                            center = unusedDirection.getOpposite();
                        }
                    }

                    {
                        // It can only be a corner if no opposites exist in usedDirections
                        if (axis == null) {
                            output.xRot = usedDirections.contains(Direction.DOWN) ? 270 : 0;
                            output.yRot =
                                    (usedDirections.get(1).getAxisDirection() != usedDirections.getLast().getAxisDirection() ? 90 : 0)
                                            + (usedDirections.contains(Direction.NORTH) ? 180 : 0);
                            qualifiers.append("corner");
                            break;
                        }
                    }

                    assert(center != null);

                    qualifiers.append(axis.toString());
                    qualifiers.append("_");

                    qualifiers.append(center.getAxis().toString());

                    if (center.getAxis() == Direction.Axis.X) {
                        output.yRot = center.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 180 : 0;
                    } else {
                        output.xRot = center.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? 180 : 0;
                    }
                }
                case 4 -> {
                    {
                        // Bit shift right gets rid of positive/negative
                        if (unusedDirections.getFirst().ordinal() >> 1 == unusedDirections.getLast().ordinal() >> 1) {
                            output.xRot = unusedDirections.getFirst().getAxis() != Direction.Axis.Y ? 90 : 0;
                            output.yRot = unusedDirections.getFirst().getAxis() == Direction.Axis.X ? 90 : 0;
                            qualifiers.append("_straight");
                            break;
                        }
                    }
                    qualifiers.append("_");
                    {
                        Direction.Axis axis = getAxis(unusedDirections.getFirst(), unusedDirections.getLast());
                        qualifiers.append(axis.toString());

                        // Mathematical patterns in the clockwise rotation
                        switch (axis) {
                            case X -> {
                                output.xRot = unusedDirections.contains(Direction.DOWN) ? 180 : 0;
                                output.yRot = unusedDirections.getFirst().getAxisDirection() != unusedDirections.getLast().getAxisDirection() ? 180 : 0;
                            }
                            case Y -> {
                                output.xRot = unusedDirections.getFirst().getAxisDirection() != unusedDirections.getLast().getAxisDirection() ? 180 : 0;
                                output.yRot = unusedDirections.contains(Direction.WEST) ? 180 : 0;
                            }
                            case Z -> {
                                output.xRot = unusedDirections.contains(Direction.DOWN) ? 180 : 0;
                                output.yRot = unusedDirections.contains(Direction.WEST) ? 180 : 0;
                            }
                        }
                    }
                }
                case 5 -> {
                    output.xRot = unusedDirections.getFirst() == Direction.DOWN ? 180 : (unusedDirections.getFirst().getNormal().getX() - unusedDirections.getFirst().getNormal().getZ()) * 90;
                    output.yRot = unusedDirections.getFirst().getAxis() == Direction.Axis.X ? 90 : 0;
                }
            }
            output.modelFile = AssetLookup.partialBaseModel(ctx, prov, qualifiers.toString());
            return output;
        };

        prov.getVariantBuilder(ctx.getEntry())
                .forAllStatesExcept(state -> {
                    Output output = function.apply(state);
                    return ConfiguredModel.builder()
                            .modelFile(output.modelFile)
                            .uvLock(true)
                            .rotationX(output.xRot)
                            .rotationY(output.yRot)
                            .build();
                    }, BlockStateProperties.WATERLOGGED);
    }

    // Same as Gearbox, except for type and blockstate function
    public static final BlockEntry<AdvancedGearbox> ADVANCED_GEARBOX = CreateAdvancedGearbox.REGISTRATE.block("advanced_gearbox", AdvancedGearbox::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion()
                    .mapColor(MapColor.PODZOL))
            //.transform(CStress.setNoImpact())
            .transform(axeOrPickaxe())
            .blockstate(AdvancedGearbox::advgearBlock)
            .item()
            .transform(customItemModel())
            .register();

    public static final ItemEntry<SequencedAssemblyItem> INCOMPLETE_ADVANCED_GEARBOX = CreateAdvancedGearbox.REGISTRATE.item("incomplete_advanced_gearbox", SequencedAssemblyItem::new)
            .model(AssetLookup.itemModel("incomplete_advanced_gearbox"))
            .register();

    // Advanced Gearbox block entity that extends regular Gearbox block entity to have similar technology
    public static final BlockEntityEntry<AdvancedGearboxBlockEntity> ADVANCED_GEARBOX_BLOCK_ENTITY = CreateAdvancedGearbox.REGISTRATE
            .blockEntity("advanced_gearbox", AdvancedGearboxBlockEntity::new)
            .visual(() -> AdvancedGearboxVisual::new, false)
            .validBlocks(ADVANCED_GEARBOX)
            .renderer(() -> AdvancedGearboxRenderer::new)
            .register();

    /*
    Recipe
     */


    public static void register() {}

    public AdvancedGearbox(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(DOWN, false)
                .setValue(UP, false)
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(EAST, false)
        );
    }

    public static Direction.Axis getAxis(Direction fst, Direction snd) {
        // The first bit represents positive/negative, if removed you can tell if they are either identical or opposite
        int normalFst = fst.ordinal() >> 1;
        int normalSnd = snd.ordinal() >> 1;
        if (normalFst == normalSnd) {
            return fst.getAxis();
        }

        Direction.Axis[] correctedAxis = {
                Direction.Axis.X,
                Direction.Axis.Z,
                Direction.Axis.Y
        };

        return correctedAxis[(normalFst | normalSnd) - 1];
    }

    // Convert Direction to BooleanProperty form
    public static BooleanProperty getDirectionProperty(Direction direction) {
        return switch (direction) {
            case Direction.DOWN -> DOWN;
            case Direction.UP -> UP;
            case Direction.NORTH -> NORTH;
            case Direction.SOUTH -> SOUTH;
            case Direction.WEST -> WEST;
            case Direction.EAST -> EAST;
        };
    }

    // Get all directions that are true
    public static void getAllDirections(BlockState state, ArrayList<Direction> usedDirections, ArrayList<Direction> unusedDirections) {
        for (Direction direction : Direction.values()) {
            if (state.getValue(getDirectionProperty(direction))) {
                usedDirections.add(direction);
                continue;
            }
            unusedDirections.add(direction);
        }
    }

    public void detachKinetics(Level worldIn, BlockPos pos, boolean reAttachNextTick) {
        BlockEntity be = worldIn.getBlockEntity(pos);
        if (!(be instanceof KineticBlockEntity))
            return;
        RotationPropagator.handleRemoved(worldIn, pos, (KineticBlockEntity) be);
        ((KineticBlockEntity) be).removeSource();

        // Re-attach next tick
        if (reAttachNextTick)
            worldIn.scheduleTick(pos, this, 1, TickPriority.EXTREMELY_HIGH);
    }

    public void addParticles(BlockState state, Level level, BlockPos pos, Direction direction) {
        for (int i = 0; i < 30; i++) {
            Vec3 particleVec3 = Vec3.atCenterOf(pos);
            particleVec3.add(Vec3.atLowerCornerOf(direction.getNormal()));
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), particleVec3.x, particleVec3.y, particleVec3.z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
        BlockEntity be = worldIn.getBlockEntity(pos);
        if (!(be instanceof KineticBlockEntity kte))
            return;
        RotationPropagator.handleAdded(worldIn, pos, kte);
    }

    BlockState enableDirection(ItemStack stack, BlockState state, Level level, BlockPos pos, @Nullable Player player, Direction direction) {
        assert(!level.isClientSide);

        // Add Side
        BlockState newState = state.setValue(getDirectionProperty(direction), true);
        detachKinetics(level, pos, true);

        addParticles(state, level, pos, direction);

        // Remove item
        stack.consume(1, player);

        return newState;
    }

    BlockState disableDirection(BlockState state, Level level, BlockPos pos, @Nullable Player player, Direction direction) {
        assert(!level.isClientSide);

        // Remove Side
        BlockState newState = state.setValue(getDirectionProperty(direction), false);
        detachKinetics(level, pos, true);

        ItemStack item = AllBlocks.COGWHEEL.asStack();

        if (player instanceof DeployerFakePlayer deployerFakePlayer) {
            deployerFakePlayer.setItemInHand(InteractionHand.MAIN_HAND, item);
        } else {
            // Spawn Item
            Vec3 vec3 = Vec3.atLowerCornerWithOffset(pos, 0.5, 1.01, 0.5).offsetRandom(level.random, 0.7F);
            ItemEntity itementity = new ItemEntity(level, vec3.x(), vec3.y(), vec3.z(), item);
            itementity.setDefaultPickUpDelay();
            level.addFreshEntity(itementity);
        }

        addParticles(state, level, pos, direction);

        return newState;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(AllBlocks.COGWHEEL.asItem())) {
            BooleanProperty property = getDirectionProperty(hitResult.getDirection());
            if (!state.getValue(property)) {
                if (!level.isClientSide) {
                    level.setBlock(pos, enableDirection(stack, state, level, pos, player, hitResult.getDirection()), Block.UPDATE_CLIENTS);

                    // Play Sound
                    level.playSound(null, pos, SoundType.WOOD.getPlaceSound(), SoundSource.BLOCKS, (SoundType.WOOD.getVolume() + 1.0F) / 2.0F, SoundType.WOOD.getPitch() * 0.8F);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        } else if (stack.is(AllItems.WRENCH.get()) || player instanceof DeployerFakePlayer) {
            BooleanProperty property = getDirectionProperty(hitResult.getDirection());
            if (state.getValue(property)) {
                if (!level.isClientSide) {
                    level.setBlock(pos, disableDirection(state, level, pos, player, hitResult.getDirection()), Block.UPDATE_CLIENTS);

                    // Play Sound
                    IWrenchable.playRemoveSound(level, pos);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useItemOn(stack,state,level,pos,player,hand,hitResult);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState state = super.getStateForPlacement(context);
        Level level = context.getLevel();

        Player player = context.getPlayer();
        if (player == null) return state;
        ItemStack stack = player.getOffhandItem();
        if (stack.is(AllBlocks.COGWHEEL.asItem())) {

            // Search directions for kinetic blocks, place all the cogwheels that it can
            for (Direction direction : Direction.values()) {
                BlockState relativeState = level.getBlockState(pos.relative(direction));
                if (relativeState.getBlock() instanceof IRotate rotate) {
                    if (rotate.hasShaftTowards(level, pos.relative(direction), relativeState, direction.getOpposite())) {
                        state = enableDirection(stack, state, level, pos, player, direction);
                        if (stack.getCount() <= 0) break;
                    }
                }
            }
        }
        return state;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> stack = super.getDrops(state, params);
        ArrayList<Direction> usedDirections = new ArrayList<>();
        ArrayList<Direction> unusedDirections = new ArrayList<>();
        getAllDirections(state, usedDirections, unusedDirections);
        if (!usedDirections.isEmpty()) {
            stack.add(AllBlocks.COGWHEEL.asStack(usedDirections.size()));
        }
        return stack;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DOWN, UP, NORTH, SOUTH, WEST, EAST);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.PUSH_ONLY;
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(getDirectionProperty(face));
    }

    // All cases of getRotationAxis are overridden, so it can just be null to throw in case other uses are necessary
    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return null;
    }

    @Override
    public Class<AdvancedGearboxBlockEntity> getBlockEntityClass() {
        return AdvancedGearboxBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AdvancedGearboxBlockEntity> getBlockEntityType() {
        return ADVANCED_GEARBOX_BLOCK_ENTITY.get();
    }
}
