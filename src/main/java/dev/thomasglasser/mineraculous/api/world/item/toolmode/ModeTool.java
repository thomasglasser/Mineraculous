package dev.thomasglasser.mineraculous.api.world.item.toolmode;

import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.impl.network.ServerboundEquipHeldToolPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ModeTool {
    static @Nullable ToolMode getToolMode(ItemStack stack) {
        return stack.get(MineraculousDataComponents.TOOL_MODE);
    }

    ImmutableSet<ToolMode> getToolModes(ItemStack stack, InteractionHand hand, Player holder);

    default ImmutableSet<ToolMode> getEnabledToolModes(ItemStack stack, InteractionHand hand, Player holder) {
        ImmutableSet.Builder<ToolMode> builder = ImmutableSet.builder();
        for (ToolMode mode : getToolModes(stack, hand, holder)) {
            if (isEnabled(mode, stack, hand, holder)) {
                builder.add(mode);
            }
        }
        return builder.build();
    }

    default boolean isEnabled(ToolMode mode, ItemStack stack, InteractionHand hand, Player holder) {
        return true;
    }

    default void setToolMode(ItemStack stack, InteractionHand hand, Player holder, @Nullable ToolMode mode) {
        onModeChanged(stack, hand, holder, getToolMode(stack), mode);
        stack.set(MineraculousDataComponents.TOOL_MODE, mode);
    }

    void onModeChanged(ItemStack stack, InteractionHand hand, Player holder, @Nullable ToolMode oldMode, @Nullable ToolMode newMode);

    default boolean canChangeToolMode(ItemStack stack, InteractionHand hand, Player holder) {
        return true;
    }

    default void onFailedToolModeChange(ItemStack stack, InteractionHand hand, Player holder) {
        TommyLibServices.NETWORK.sendToServer(new ServerboundEquipHeldToolPayload(hand));
    }
}
