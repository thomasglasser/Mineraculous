package dev.thomasglasser.mineraculous.world.item.curio;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

public class CuriosUtils {
    public static boolean setStackInFirstValidSlot(LivingEntity entity, String identifier, ItemStack stack) {
        IDynamicStackHandler stacks = CuriosApi.getCuriosInventory(entity).orElseThrow().getCurios().get(identifier).getStacks();
        for (int i = 0; i < stacks.getSlots(); i++) {
            ItemStack remainder = stacks.insertItem(i, stack, true);
            if (remainder.isEmpty()) {
                stacks.insertItem(i, stack, false);
                return true;
            }
        }
        return false;
    }

    public static boolean setStackInFirstValidSlot(LivingEntity entity, ItemStack stack) {
        AtomicBoolean set = new AtomicBoolean(false);
        CuriosApi.getCuriosInventory(entity).orElseThrow().getCurios().forEach((identifier, handler) -> {
            if (setStackInFirstValidSlot(entity, identifier, stack))
                set.set(true);
        });
        return set.get();
    }

    public static void setStackInSlot(LivingEntity entity, CuriosData curiosData, ItemStack stack) {
        CuriosApi.getCuriosInventory(entity).orElseThrow().getCurios().get(curiosData.identifier()).getStacks().setStackInSlot(curiosData.slot(), stack);
    }

    public static ItemStack getStackInSlot(LivingEntity entity, CuriosData curiosData) {
        return CuriosApi.getCuriosInventory(entity).orElseThrow().getCurios().get(curiosData.identifier()).getStacks().getStackInSlot(curiosData.slot());
    }

    public static Map<CuriosData, ItemStack> getAllItems(LivingEntity entity) {
        Map<CuriosData, ItemStack> items = new HashMap<>();
        ICuriosItemHandler curios = CuriosApi.getCuriosInventory(entity).orElseThrow();
        curios.getCurios().forEach((name, handler) -> {
            for (int i = 0; i < handler.getStacks().getSlots(); i++) {
                ItemStack stack = handler.getStacks().getStackInSlot(i);
                if (!stack.isEmpty()) items.put(new CuriosData(i, name), stack);
            }
        });
        return items;
    }
}
