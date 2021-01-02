package com.miskatonicmysteries.common.block;

import com.miskatonicmysteries.common.block.blockentity.OctagramBlockEntity;
import com.miskatonicmysteries.common.feature.Affiliated;
import com.miskatonicmysteries.common.feature.recipe.rite.Rite;
import com.miskatonicmysteries.common.lib.ModObjects;
import com.miskatonicmysteries.common.lib.ModRecipes;
import com.miskatonicmysteries.common.lib.util.InventoryUtil;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class OctagramBlock extends HorizontalFacingBlock implements BlockEntityProvider, Affiliated {
    public static List<OctagramBlock> OCTAGRAMS = new ArrayList<>();
    public static final VoxelShape SHAPE = createCuboidShape(0, 0, 0, 16, 1, 16);
    private final Identifier affiliation;

    public OctagramBlock(Identifier affiliation) {
        super(Settings.of(Material.CARPET).nonOpaque().noCollision());
        OCTAGRAMS.add(this);
        this.affiliation = affiliation;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        //todo make rites executable
        if (world.getBlockEntity(pos) instanceof OctagramBlockEntity) {
            OctagramBlockEntity octagram = (OctagramBlockEntity) world.getBlockEntity(pos);
            if (octagram.currentRite != null) {
                return ActionResult.PASS;
            }
            Rite rite = ModRecipes.getRite(octagram);
            if (rite != null) {
                octagram.currentRite = rite;
                rite.onStart(octagram);
                octagram.markDirty();
                return ActionResult.SUCCESS;
            }
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1;
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            for (int i = 0; i < 8; i++) {
                BlockPos partPos = BlockOuterOctagram.getOffsetToCenterPos(i);
                world.setBlockState(pos.add(-partPos.getX(), 0, -partPos.getZ()), Blocks.AIR.getDefaultState());
                if (!world.isClient) {
                    ((ServerWorld) world).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, getDefaultState()), partPos.getX(), partPos.getY(), partPos.getZ(), 6, 0.0D, 0.0D, 0.0D, 0.05D);
                }
            }
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof OctagramBlockEntity) {
                ItemScatterer.spawn(world, pos, ((OctagramBlockEntity) blockEntity).getItems());
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new OctagramBlockEntity();
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        for (int i = 0; i < 8; i++) {
            BlockPos partPos = BlockOuterOctagram.getOffsetToCenterPos(i);
            world.setBlockState(pos.add(-partPos.getX(), 0, -partPos.getZ()), ModObjects.OCTAGRAM_SIDES.getDefaultState().with(BlockOuterOctagram.NUMBER, i));
        }
        super.onPlaced(world, pos, state, placer, itemStack);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        if (isPlacementValid(ctx.getWorld(), ctx.getBlockPos(), ctx)) {
            for (int i = 0; i < 8; i++) {
                if (!isPlacementValid(ctx.getWorld(), ctx.getBlockPos().add(BlockOuterOctagram.getOffsetToCenterPos(i)), ctx))
                    return null;
            }
            return this.getDefaultState().with(FACING, ctx.getPlayerFacing());
        }
        return null;
    }

    protected boolean isPlacementValid(World world, BlockPos pos, ItemPlacementContext ctx) {
        return (world.getBlockState(pos).isAir() || world.getBlockState(pos).canReplace(ctx)) && world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), Direction.UP);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.appendProperties(builder);
    }

    @Override
    public Identifier getAffiliation() {
        return affiliation;
    }

    @Override
    public boolean isSupernatural() {
        return true;
    }

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.DESTROY;
    }

    public static class BlockOuterOctagram extends Block {
        public static IntProperty NUMBER = IntProperty.of("number", 0, 7);

        public BlockOuterOctagram() {
            super(Settings.of(Material.CARPET).nonOpaque());
            setDefaultState(getDefaultState().with(NUMBER, 0));
        }

        @Override
        public BlockRenderType getRenderType(BlockState state) {
            return BlockRenderType.INVISIBLE;
        }

        @Override
        public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
            OctagramBlockEntity octagram = getOctagram(world, pos, state);
            if (octagram == null) {
                world.breakBlock(pos, false);
                return ActionResult.FAIL;
            } else {
                ItemStack stack = player.getStackInHand(hand);
                if (!stack.isEmpty() && octagram.isValid(state.get(NUMBER), stack) && octagram.getStack(state.get(NUMBER)).isEmpty()) {
                    octagram.setStack(state.get(NUMBER), stack);
                    octagram.markDirty();
                    return ActionResult.CONSUME;
                } else if (stack.isEmpty() && !octagram.getItems().isEmpty()) {
                    InventoryUtil.giveItem(world, player, octagram.removeStack(state.get(NUMBER)));
                    octagram.markDirty();
                    return ActionResult.SUCCESS;
                }
            }
            return super.onUse(state, world, pos, player, hand, hit);
        }


        @Override
        public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {

        }

        @Override
        public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
            if (!state.isOf(newState.getBlock())) {
                world.breakBlock(pos.add(getOffsetToCenterPos(state.get(NUMBER))), false);
            }
        }

        @Override
        public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
            if (!world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), Direction.UP)) {
                world.breakBlock(pos.add(getOffsetToCenterPos(state.get(NUMBER))), false);
            }
            super.neighborUpdate(state, world, pos, block, fromPos, notify);
        }

        public static OctagramBlockEntity getOctagram(World world, BlockPos pos, BlockState state) {
            BlockEntity blockEntity = world.getBlockEntity(pos.add(getOffsetToCenterPos(state.get(NUMBER))));
            return blockEntity instanceof OctagramBlockEntity ? (OctagramBlockEntity) blockEntity : null;
        }

        public static BlockPos getOffsetToCenterPos(int index) {
            switch (index) {
                default:
                case 0:
                    return new BlockPos(0, 0, -1);
                case 1:
                    return new BlockPos(-1, 0, -1);
                case 2:
                    return new BlockPos(-1, 0, 0);
                case 3:
                    return new BlockPos(-1, 0, 1);
                case 4:
                    return new BlockPos(0, 0, 1);
                case 5:
                    return new BlockPos(1, 0, 1);
                case 6:
                    return new BlockPos(1, 0, 0);
                case 7:
                    return new BlockPos(1, 0, -1);
            }
        }

        @Override
        public PistonBehavior getPistonBehavior(BlockState state) {
            return PistonBehavior.DESTROY;
        }

        @Override
        protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
            builder.add(NUMBER);
            super.appendProperties(builder);
        }

        @Override
        public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
            return SHAPE;
        }
    }
}