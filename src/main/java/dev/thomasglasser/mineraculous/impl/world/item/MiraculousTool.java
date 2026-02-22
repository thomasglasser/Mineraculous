package dev.thomasglasser.mineraculous.impl.world.item;

import com.google.common.collect.ImmutableList;
import dev.thomasglasser.mineraculous.impl.network.ServerboundEquipToolPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface MiraculousTool<T extends MiraculousTool.ToolMode> {
    List<T> getToolModes(ItemStack stack, InteractionHand hand, Player holder);

    default List<T> getEnabledToolModes(ItemStack stack, InteractionHand hand, Player holder) {
        ImmutableList.Builder<T> list = new ImmutableList.Builder<>();
        for (T mode : getToolModes(stack, hand, holder)) {
            if (mode.isEnabled(stack, holder))
                list.add(mode);
        }
        return list.build();
    }

    default boolean canOpenToolModeMenu(ItemStack stack, Player holder) {
        return true;
    }

    Supplier<DataComponentType<T>> getToolModeComponentType(ItemStack stack, InteractionHand hand, Player holder);

    default void setToolMode(ItemStack stack, InteractionHand hand, Player holder, T mode) {
        onModeChanged(stack, hand, holder, getToolMode(stack, hand, holder), mode);
        stack.set(getToolModeComponentType(stack, hand, holder).get(), mode);
    }

    default T getToolMode(ItemStack stack, InteractionHand hand, Player holder) {
        return stack.get(getToolModeComponentType(stack, hand, holder));
    }

    void onModeChanged(ItemStack stack, InteractionHand hand, Player holder, T oldMode, T newMode);

    default void onModeMenuFailedToOpen(InteractionHand hand) {
        TommyLibServices.NETWORK.sendToServer(new ServerboundEquipToolPayload(hand));
    }

    interface ToolMode extends StringRepresentable {
        Component displayName();

        boolean isEnabled(ItemStack stack, Player holder);

        ResourceLocation getIcon();
    }
}
