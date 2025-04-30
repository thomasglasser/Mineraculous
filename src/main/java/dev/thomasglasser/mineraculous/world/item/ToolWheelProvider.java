package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuOption;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface ToolWheelProvider<T extends RadialMenuOption> {
    boolean canOpenToolWheel(ItemStack stack);

    int getColor(ItemStack stack, InteractionHand hand);

    T[] getOptions(ItemStack stack, InteractionHand hand);

    Holder<DataComponentType<?>> getComponentType();

    void handleSecondaryToolKeyBehavior(ItemStack stack, InteractionHand hand);
}
