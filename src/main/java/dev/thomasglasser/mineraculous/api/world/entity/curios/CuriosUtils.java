package dev.thomasglasser.mineraculous.api.world.entity.curios;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CuriosUtils {
    /**
     * Puts the provided {@link ItemStack} in the provided {@link LivingEntity}'s first available index of the provided Curios slot.
     *
     * @param entity     The entity to equip the stack for
     * @param identifier The Curios slot type to put the stack in
     * @param stack      The stack to put in the entity's first available index of the Curios slot
     * @return Whether the stack was successfully inserted
     */
    public static boolean setStackInFirstValidSlot(LivingEntity entity, String identifier, ItemStack stack) {
        return CuriosApi.getCuriosInventory(entity).map(curios -> {
            IDynamicStackHandler stacks = curios.getCurios().get(identifier).getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                ItemStack remainder = stacks.insertItem(i, stack, true);
                if (remainder.isEmpty()) {
                    stacks.insertItem(i, stack, false);
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }

    /**
     * Puts the provided {@link ItemStack} in the first available valid Curios slot of the provided {@link LivingEntity}.
     *
     * @param entity The entity to equip the stack for
     * @param stack  The stack to put in the entity's first available valid Curios slot
     * @return Whether the stack was successfully inserted
     */
    public static boolean setStackInFirstValidSlot(LivingEntity entity, ItemStack stack) {
        Set<String> identifiers = CuriosApi.getCuriosInventory(entity).map(handler -> handler.getCurios().keySet()).orElseGet(ImmutableSet::of);
        for (String identifier : identifiers) {
            if (setStackInFirstValidSlot(entity, identifier, stack)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Puts the provided {@link ItemStack} in the provided Curios slot of the provided {@link LivingEntity}.
     * 
     * @param entity     The entity to equip the stack for
     * @param curiosData The Curios slot to put the stack in
     * @param stack      The stack to put in the entity's Curios slot
     */
    public static void setStackInSlot(LivingEntity entity, CuriosData curiosData, ItemStack stack) {
        CuriosApi.getCuriosInventory(entity).ifPresent(curios -> curios.getCurios().get(curiosData.identifier()).getStacks().setStackInSlot(curiosData.index(), stack));
    }

    /**
     * Gets the {@link ItemStack} in the provided Curios slot of the provided {@link LivingEntity}.
     * 
     * @param entity     The entity to get the stack for
     * @param curiosData The Curios slot to get the stack from
     * @return The stack in the slot, or an empty stack if the inventory is not present
     */
    public static ItemStack getStackInSlot(LivingEntity entity, CuriosData curiosData) {
        return CuriosApi.getCuriosInventory(entity).map(curios -> curios.getCurios().get(curiosData.identifier()).getStacks().getStackInSlot(curiosData.index())).orElse(ItemStack.EMPTY);
    }

    /**
     * Creates a map of Curios slots to their {@link ItemStack}s for the provided {@link LivingEntity}.
     * 
     * @param entity The entity to make the map for
     * @return A map of Curios slots to their stacks
     */
    public static Map<CuriosData, ItemStack> getAllItems(LivingEntity entity) {
        Map<CuriosData, ItemStack> items = new Object2ObjectOpenHashMap<>();
        CuriosApi.getCuriosInventory(entity).ifPresent(curios -> curios.getCurios().forEach((name, handler) -> {
            IDynamicStackHandler stacks = handler.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                ItemStack stack = stacks.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    items.put(new CuriosData(name, i), stack);
                }
            }
        }));
        return items;
    }

    /**
     * Checks if the provided {@link ItemStack} is equipped in the provided {@link LivingEntity}'s Curios inventory.
     *
     * @param entity The entity to check for the stack in
     * @param stack  The stack to check for
     * @return Whether the stack is equipped
     */
    public static boolean isEquipped(LivingEntity entity, ItemStack stack) {
        return CuriosApi.getCuriosInventory(entity).map(curios -> curios.isEquipped(s -> s == stack)).orElse(false);
    }
}
