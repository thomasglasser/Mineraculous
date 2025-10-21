package dev.thomasglasser.mineraculous.impl.world.item;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.impl.world.item.component.Kamikotizing;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
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

    public static void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        Kamikotizing kamikotizing = stack.get(MineraculousDataComponents.KAMIKOTIZING);
        if (kamikotizing != null) {
            Player player = event.getPlayer();
            kamikotizing.slotInfo().getSlot().ifLeft(i -> {
                ItemStack current = player.getInventory().getItem(i);
                if (!current.isEmpty())
                    player.addItem(current);
                player.getInventory().setItem(i, stack);
            }).ifRight(curiosData -> {
                ItemStack current = CuriosUtils.getStackInSlot(player, curiosData);
                if (!current.isEmpty())
                    player.addItem(current);
                CuriosUtils.setStackInSlot(player, curiosData, stack);
            });
            event.setCanceled(true);
        }
    }
}
