package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.gui.screens.inventory.ExternalInventoryScreen;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.world.item.armor.KamikotizationArmorItem;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public record ServerboundStealItemPayload(UUID target, int slot) implements ExtendedPacketPayload {
    public static final Type<ServerboundStealItemPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_steal_item"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStealItemPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundStealItemPayload::target,
            ByteBufCodecs.INT, ServerboundStealItemPayload::slot,
            ServerboundStealItemPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(this.target);
        if (target != null) {
            ItemStack stack = target.inventoryMenu.slots.get(this.slot).getItem();
            if (stack.getItem() instanceof KamikotizationArmorItem) {
                if (handleKamikotizationArmor(player, target, stack)) {
                    return;
                }
            }
            if (EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE) || stack.has(MineraculousDataComponents.KAMIKOTIZING)) {
                player.displayClientMessage(ExternalInventoryScreen.ITEM_BOUND_KEY, true);
            } else {
                target.getInventory().removeItem(stack);
                giveOrDropItem(player, stack.copyAndClear());
            }
        }
    }

    private static boolean handleKamikotizationArmor(Player player, Player target, ItemStack stack) {
        return target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION)
                .filter(data -> data.kamikotizedSlot().isPresent() && target.getItemBySlot(data.kamikotizedSlot().get()) == stack)
                .flatMap(data -> target.getData(MineraculousAttachmentTypes.STORED_ARMOR)
                        .map(armorData -> {
                            ItemStack original = armorData.removeFrom(data.kamikotizedSlot().get(), target);
                            giveOrDropItem(player, original);
                            data.clearKamikotizedSlot().save(target);
                            return true;
                        }))
                .orElse(false);
    }

    public static void giveOrDropItem(Player player, ItemStack stack) {
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        } else {
            player.drop(stack, true, true);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
