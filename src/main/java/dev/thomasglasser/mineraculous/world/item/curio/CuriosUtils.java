package dev.thomasglasser.mineraculous.world.item.curio;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosUtils {
    public static boolean setStackInFirstValidSlot(LivingEntity entity, String identifier, ItemStack stack) {
        AtomicBoolean set = new AtomicBoolean(false);
        CuriosApi.getCuriosInventory(entity).map(curios -> curios.getCurios().get(identifier).getStacks()).ifPresent(stacks -> {
            if (!set.get()) {
                for (int i = 0; i < stacks.getSlots(); i++) {
                    ItemStack remainder = stacks.insertItem(i, stack, true);
                    if (remainder.isEmpty()) {
                        stacks.insertItem(i, stack, false);
                        set.set(true);
                    }
                }
            }
        });
        return set.get();
    }

    public static boolean setStackInFirstValidSlot(LivingEntity entity, ItemStack stack) {
        AtomicBoolean set = new AtomicBoolean(false);
        CuriosApi.getCuriosInventory(entity).ifPresent(curios -> curios.getCurios().forEach((identifier, handler) -> {
            if (!set.get() && setStackInFirstValidSlot(entity, identifier, stack))
                set.set(true);
        }));
        return set.get();
    }

    public static void setStackInSlot(LivingEntity entity, CuriosData curiosData, ItemStack stack) {
        CuriosApi.getCuriosInventory(entity).ifPresent(curios -> curios.getCurios().get(curiosData.identifier()).getStacks().setStackInSlot(curiosData.slot(), stack));
    }

    public static ItemStack getStackInSlot(LivingEntity entity, CuriosData curiosData) {
        return CuriosApi.getCuriosInventory(entity).map(curios -> curios.getCurios().get(curiosData.identifier()).getStacks().getStackInSlot(curiosData.slot())).orElse(ItemStack.EMPTY);
    }

    public static Map<CuriosData, ItemStack> getAllItems(LivingEntity entity) {
        Map<CuriosData, ItemStack> items = new Object2ObjectOpenHashMap<>();
        CuriosApi.getCuriosInventory(entity).ifPresent(curios -> curios.getCurios().forEach((name, handler) -> {
            for (int i = 0; i < handler.getStacks().getSlots(); i++) {
                ItemStack stack = handler.getStacks().getStackInSlot(i);
                if (!stack.isEmpty()) items.put(new CuriosData(i, name), stack);
            }
        }));
        return items;
    }
}
