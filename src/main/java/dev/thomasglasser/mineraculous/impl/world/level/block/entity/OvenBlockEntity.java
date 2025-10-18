package dev.thomasglasser.mineraculous.impl.world.level.block.entity;

import dev.thomasglasser.mineraculous.api.world.item.crafting.MineraculousRecipeTypes;
import dev.thomasglasser.mineraculous.api.world.level.block.entity.MineraculousBlockEntityTypes;
import dev.thomasglasser.mineraculous.impl.world.inventory.OvenMenu;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class OvenBlockEntity extends AbstractFurnaceBlockEntity {
    public static final int[] SLOTS_INPUT = { 0, 1, 2, 3 };
    public static final int SLOT_FUEL = 4;
    public static final int SLOT_RESULT = 5;
    public static final int SLOT_COUNT = 6;
    private static final int[] SLOTS_FOR_UP = SLOTS_INPUT;
    private static final int[] SLOTS_FOR_DOWN = { SLOT_RESULT, SLOT_FUEL };
    private static final int[] SLOTS_FOR_SIDES = { SLOT_FUEL };
    public static final int BURN_TIME_STANDARD = 400;

    public static Component NAME = Component.translatable("container.mineraculous.oven");

    public OvenBlockEntity(BlockPos pos, BlockState blockState) {
        super(MineraculousBlockEntityTypes.OVEN.get(), pos, blockState, MineraculousRecipeTypes.OVEN.get());
        this.items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    }

    @Override
    protected Component getDefaultName() {
        return NAME;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new OvenMenu(containerId, inventory, this, this.dataAccess);
    }

    public static ItemStack findFirstInput(NonNullList<ItemStack> items) {
        ItemStack input = ItemStack.EMPTY;
        for (int i : SLOTS_INPUT) {
            ItemStack slotItem = items.get(i);
            if (!slotItem.isEmpty()) {
                input = slotItem;
                break;
            }
        }
        return input;
    }

    protected ItemStack findFirstInput() {
        return findFirstInput(items);
    }

    protected int getFilledSlotCount() {
        int count = 0;
        for (int i : SLOTS_INPUT) {
            if (!items.get(i).isEmpty()) {
                count++;
            }
        }
        return count;
    }

    protected int getResultCount(ItemStack result) {
        return result.getCount() * getFilledSlotCount();
    }

    public int getCookingProgress() {
        return cookingProgress;
    }

    public int getCookingTotalTime() {
        return cookingTotalTime;
    }

    public List<ItemStack> getItemsView() {
        return NonNullList.copyOf(items);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, OvenBlockEntity blockEntity) {
        boolean lit = blockEntity.isLit();
        boolean changed = false;
        if (blockEntity.isLit()) {
            blockEntity.litTime--;
        }

        ItemStack input = blockEntity.findFirstInput();
        ItemStack fuel = blockEntity.items.get(SLOT_FUEL);
        boolean hasInput = !input.isEmpty();
        boolean hasFuel = !fuel.isEmpty();
        if (blockEntity.isLit() || hasInput && hasFuel) {
            RecipeHolder<?> recipeholder = hasInput ? blockEntity.quickCheck.getRecipeFor(new SingleRecipeInput(input), level).orElse(null) : null;
            int maxStackSize = blockEntity.getMaxStackSize();
            if (!blockEntity.isLit() && canBurn(level.registryAccess(), recipeholder, maxStackSize, blockEntity)) {
                blockEntity.litTime = blockEntity.getBurnDuration(fuel);
                blockEntity.litDuration = blockEntity.litTime;
                if (blockEntity.isLit()) {
                    changed = true;
                    if (fuel.hasCraftingRemainingItem())
                        blockEntity.items.set(SLOT_FUEL, fuel.getCraftingRemainingItem());
                    else if (hasFuel) {
                        fuel.shrink(1);
                        if (fuel.isEmpty()) {
                            blockEntity.items.set(SLOT_FUEL, fuel.getCraftingRemainingItem());
                        }
                    }
                }
            }

            if (blockEntity.isLit() && canBurn(level.registryAccess(), recipeholder, maxStackSize, blockEntity)) {
                blockEntity.cookingProgress++;
                if (blockEntity.cookingProgress == blockEntity.cookingTotalTime) {
                    blockEntity.cookingProgress = 0;
                    blockEntity.cookingTotalTime = getTotalCookTime(level, blockEntity);
                    if (burn(level.registryAccess(), recipeholder, maxStackSize, blockEntity)) {
                        blockEntity.setRecipeUsed(recipeholder);
                    }

                    changed = true;
                }
            } else {
                blockEntity.cookingProgress = 0;
            }
        } else if (!blockEntity.isLit() && blockEntity.cookingProgress > 0) {
            blockEntity.cookingProgress = Mth.clamp(blockEntity.cookingProgress - 2, 0, blockEntity.cookingTotalTime);
        }

        if (lit != blockEntity.isLit()) {
            changed = true;
            state = state.setValue(AbstractFurnaceBlock.LIT, blockEntity.isLit());
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }

        if (changed) {
            setChanged(level, pos, state);
        }
    }

    protected static int getTotalCookTime(Level level, OvenBlockEntity blockEntity) {
        SingleRecipeInput singlerecipeinput = new SingleRecipeInput(blockEntity.findFirstInput());
        return blockEntity.quickCheck.getRecipeFor(singlerecipeinput, level).map(p_300840_ -> p_300840_.value().getCookingTime()).orElse(BURN_TIME_STANDARD);
    }

    private static boolean canBurn(RegistryAccess registryAccess, @Nullable RecipeHolder<?> recipe, int maxStackSize, OvenBlockEntity blockEntity) {
        ItemStack input = blockEntity.findFirstInput();
        if (!input.isEmpty() && recipe != null) {
            for (int i = 1; i < SLOTS_INPUT.length; i++) {
                ItemStack other = blockEntity.items.get(i);
                if (!other.isEmpty() && !ItemStack.isSameItemSameComponents(input, other)) {
                    return false;
                }
            }
            ItemStack result = ((RecipeHolder<? extends AbstractCookingRecipe>) recipe).value().assemble(new SingleRecipeInput(input), registryAccess);
            if (result.isEmpty()) {
                return false;
            } else {
                ItemStack resultSlotItem = blockEntity.items.get(SLOT_RESULT);
                if (resultSlotItem.isEmpty()) {
                    return true;
                } else if (!ItemStack.isSameItemSameComponents(resultSlotItem, result)) {
                    return false;
                } else {
                    return resultSlotItem.getCount() + blockEntity.getResultCount(result) <= maxStackSize && resultSlotItem.getCount() + blockEntity.getResultCount(result) <= resultSlotItem.getMaxStackSize() || resultSlotItem.getCount() + blockEntity.getResultCount(result) <= result.getMaxStackSize();
                }
            }
        } else {
            return false;
        }
    }

    private static boolean burn(RegistryAccess registryAccess, @Nullable RecipeHolder<?> recipe, int maxStackSize, OvenBlockEntity blockEntity) {
        if (recipe != null && canBurn(registryAccess, recipe, maxStackSize, blockEntity)) {
            ItemStack input = blockEntity.findFirstInput();
            ItemStack result = ((RecipeHolder<? extends AbstractCookingRecipe>) recipe).value().assemble(new SingleRecipeInput(input), registryAccess);
            ItemStack resultSlotItem = blockEntity.items.get(SLOT_RESULT);
            if (resultSlotItem.isEmpty()) {
                blockEntity.items.set(SLOT_RESULT, result.copyWithCount(blockEntity.getResultCount(result)));
            } else if (ItemStack.isSameItemSameComponents(resultSlotItem, result)) {
                resultSlotItem.grow(blockEntity.getResultCount(result));
            }

            if (input.is(Blocks.WET_SPONGE.asItem()) && blockEntity.items.get(SLOT_FUEL).is(Items.BUCKET)) {
                blockEntity.items.set(SLOT_FUEL, new ItemStack(Items.WATER_BUCKET));
            }

            for (int i : SLOTS_INPUT) {
                blockEntity.items.get(i).shrink(1);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        } else {
            return side == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
        }
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return direction != Direction.DOWN || index != SLOT_FUEL || stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET);
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        if (index == SLOT_RESULT) {
            return false;
        } else if (index != SLOT_FUEL) {
            return true;
        } else {
            ItemStack itemstack = this.items.get(SLOT_FUEL);
            return stack.getBurnTime(this.recipeType) > 0 || stack.is(Items.BUCKET) && !itemstack.is(Items.BUCKET);
        }
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        ItemStack itemstack = this.items.get(index);
        boolean flag = !stack.isEmpty() && ItemStack.isSameItemSameComponents(itemstack, stack);
        this.items.set(index, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        if (Arrays.stream(SLOTS_INPUT).anyMatch(i -> i == index) && !flag) {
            this.cookingTotalTime = getTotalCookTime(this.level, this);
            this.cookingProgress = 0;
            this.setChanged();
        }
    }
}
