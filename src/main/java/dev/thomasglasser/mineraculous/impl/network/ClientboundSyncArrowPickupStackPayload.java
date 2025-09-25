package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

public record ClientboundSyncArrowPickupStackPayload(int entityId, ItemStack stack) implements ExtendedPacketPayload {
    public static final Type<ClientboundSyncArrowPickupStackPayload> TYPE = new Type<>(MineraculousConstants.modLoc("clientbound_sync_arrow_pickup_stack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncArrowPickupStackPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundSyncArrowPickupStackPayload::entityId,
            ItemStack.OPTIONAL_STREAM_CODEC, ClientboundSyncArrowPickupStackPayload::stack,
            ClientboundSyncArrowPickupStackPayload::new);

    @Override
    public void handle(Player player) {
        if (player.level().getEntity(entityId) instanceof AbstractArrow arrow) {
            arrow.pickupItemStack = stack;
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
