package dev.thomasglasser.mineraculous.api.world.item;

import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuOption;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface RadialMenuProvider<T extends RadialMenuOption> {
    default boolean canOpenMenu(ItemStack stack, InteractionHand hand, Player holder) {
        return true;
    }

    int getColor(ItemStack stack, InteractionHand hand, Player holder);

    List<T> getOptions(ItemStack stack, InteractionHand hand, Player holder);

    default boolean handleSecondaryKeyBehavior(ItemStack stack, InteractionHand hand, Player holder) {
        return false;
    }

    Supplier<DataComponentType<T>> getComponentType(ItemStack stack, InteractionHand hand, Player holder);

    default T setOption(ItemStack stack, InteractionHand hand, int index, Player holder) {
        T value = getOptions(stack, hand, holder).get(index);
        stack.set(getComponentType(stack, hand, holder).get(), value);
        return value;
    }
}
