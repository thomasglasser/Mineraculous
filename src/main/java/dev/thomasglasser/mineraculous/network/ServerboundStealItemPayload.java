package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalInventoryScreen;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public record ServerboundStealItemPayload(UUID target, int slot) implements ExtendedPacketPayload {
    public static final Type<ServerboundStealItemPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_steal_item"));
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
            if (EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
                player.displayClientMessage(Component.translatable(ExternalInventoryScreen.ITEM_BOUND_KEY), true);
            } else {
                target.getInventory().removeItem(stack);
                if (player.getMainHandItem().isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                } else {
                    player.drop(stack, true, true);
                }
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
