package dev.thomasglasser.mineraculous.impl.world.item;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class MineraculousItemEvents {
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item.TooltipContext context = event.getContext();
        List<Component> tooltip = event.getToolTip();
        Consumer<Component> adder = tooltip::add;
        TooltipFlag flag = event.getFlags();

        stack.addToTooltip(MineraculousDataComponents.ACTIVE, context, adder, flag);
    }
}
