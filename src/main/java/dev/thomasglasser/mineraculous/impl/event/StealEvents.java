package dev.thomasglasser.mineraculous.impl.event;

import dev.thomasglasser.mineraculous.api.client.gui.screens.inventory.ExternalInventoryScreen;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.event.StealEvent;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.network.ServerboundStealItemPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundWakeUpPayload;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.impl.world.item.armor.KamikotizationArmorItem;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class StealEvents {
    public static void onPreStartSteal(StealEvent.Start.Pre event) {
        Player player = event.getEntity();
        Player target = event.getTarget();
        event.setCanceled(!((MineraculousServerConfig.get().enableUniversalStealing.get() || player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || player.getData(MineraculousAttachmentTypes.MIRACULOUSES.get()).isTransformed()) && (MineraculousServerConfig.get().enableSleepStealing.get() || !target.isSleeping())));
    }

    public static void onTickStartSteal(StealEvent.Start.Tick event) {
        Player player = event.getEntity();
        Player target = event.getTarget();
        if (target.isSleeping() && MineraculousServerConfig.get().wakeUpChance.get() > 0 && (MineraculousServerConfig.get().wakeUpChance.get() >= 100 || player.getRandom().nextFloat() < MineraculousServerConfig.get().wakeUpChance.get() / (SharedConstants.TICKS_PER_SECOND * MineraculousServerConfig.get().stealingDuration.getAsInt() * 100F))) {
            TommyLibServices.NETWORK.sendToServer(new ServerboundWakeUpPayload(target.getUUID(), Optional.of(MineraculousClientUtils.STEALING_WARNING)));
            event.setCanceled(true);
        }
    }

    public static void onFinishSteal(StealEvent.Finish event) {
        Player player = event.getEntity();
        Player target = event.getTarget();
        ItemStack stack = event.getStack();
        Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        if (miraculous != null && stack.getItem() instanceof MiraculousItem && stack.has(MineraculousDataComponents.POWERED)) {
            target.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).detransform(target, (ServerLevel) player.level(), miraculous, stack, true);
        }
        if (stack.getItem() instanceof KamikotizationArmorItem) {
            if (handleKamikotizationArmor(player, target, stack)) {
                event.setCanceled(true);
            }
        }
        if (EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) || stack.has(MineraculousDataComponents.KAMIKOTIZING)) {
            player.displayClientMessage(ExternalInventoryScreen.ITEM_BOUND_KEY, true);
            event.setCanceled(true);
        }
    }

    private static boolean handleKamikotizationArmor(Player player, Player target, ItemStack stack) {
        return target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION)
                .filter(data -> data.kamikotizedSlot().isPresent() && target.getItemBySlot(data.kamikotizedSlot().get()) == stack)
                .flatMap(data -> target.getData(MineraculousAttachmentTypes.STORED_ARMOR)
                        .map(armorData -> {
                            ItemStack original = armorData.removeFrom(data.kamikotizedSlot().get(), target);
                            ServerboundStealItemPayload.giveOrDropItem(player, original);
                            data.clearKamikotizedSlot().save(target);
                            return true;
                        }))
                .orElse(false);
    }
}
