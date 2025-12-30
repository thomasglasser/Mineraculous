package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.event.StealEvent;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

public record ServerboundStealItemPayload(UUID target, int slot) implements ExtendedPacketPayload {
    public static final Type<ServerboundStealItemPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_steal_item"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStealItemPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(UUID::fromString, UUID::toString), ServerboundStealItemPayload::target,
            ByteBufCodecs.VAR_INT, ServerboundStealItemPayload::slot,
            ServerboundStealItemPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        Player target = player.level().getPlayerByUUID(this.target);
        if (target != null) {
            ItemStack stack = target.inventoryMenu.slots.get(this.slot).getItem();
            if (NeoForge.EVENT_BUS.post(new StealEvent.Finish(player, target, stack)).isCanceled())
                return;
            target.getInventory().removeItem(stack);
            giveOrDropItem(player, stack.copyAndClear());
        }
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
