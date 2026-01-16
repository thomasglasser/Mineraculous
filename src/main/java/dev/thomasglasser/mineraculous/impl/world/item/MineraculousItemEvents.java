package dev.thomasglasser.mineraculous.impl.world.item;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.event.ItemBreakEvent;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.SlotInfo;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
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
        SlotInfo kamikotizing = stack.get(MineraculousDataComponents.KAMIKOTIZING);
        if (kamikotizing != null) {
            Player player = event.getPlayer();
            kamikotizing.slot().ifLeft(i -> {
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

    public static void onPreItemBreak(ItemBreakEvent.Pre event) {
        ItemStack stack = event.getStack();
        LivingEntity breaker = event.getBreaker();
        if (stack.has(MineraculousDataComponents.KAMIKOTIZING) || breaker != null && stack.has(MineraculousDataComponents.KAMIKOTIZATION) && stack.getOrDefault(MineraculousDataComponents.OWNER, Util.NIL_UUID).equals(breaker.getUUID())) {
            if (breaker instanceof Player player) {
                player.displayClientMessage(MineraculousItemUtils.KAMIKOTIZED_ITEM_UNBREAKABLE_KEY, true);
            }
            event.setCanceled(true);
        }
    }

    public static void onDetermineItemBreakDamage(ItemBreakEvent.DetermineDamage event) {
        LivingEntity breaker = event.getBreaker();
        int damage = event.getDamage();
        if (breaker != null) {
            MiraculousesData miraculousesData = breaker.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            if (miraculousesData.isTransformed()) {
                damage += 100 * miraculousesData.getMaxTransformedPowerLevel();
            } else {
                Optional<KamikotizationData> kamikotizationData = breaker.getData(MineraculousAttachmentTypes.KAMIKOTIZATION);
                if (kamikotizationData.isPresent()) {
                    damage += 100 * kamikotizationData.get().kamikoData().powerLevel();
                }
            }
        }
        event.setDamage(damage);
    }
}
