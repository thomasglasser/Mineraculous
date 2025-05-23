package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuOption;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface RadialMenuProvider<T extends RadialMenuOption> {
    default boolean canOpenMenu(ItemStack stack, InteractionHand hand) {
        return true;
    }

    int getColor(ItemStack stack, InteractionHand hand);

    List<T> getOptions(ItemStack stack, InteractionHand hand);

    default boolean handleSecondaryKeyBehavior(ItemStack stack, InteractionHand hand) {
        return false;
    }

    Supplier<DataComponentType<T>> getComponentType(ItemStack stack, InteractionHand hand);

    default void setOption(ItemStack stack, InteractionHand hand, int index) {
        stack.set(getComponentType(stack, hand).get(), getOptions(stack, hand).get(index));
    }
}
