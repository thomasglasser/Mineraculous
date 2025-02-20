package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

public record ClientboundSyncArrowPickupStackPayload(int entityId, ItemStack stack) implements ExtendedPacketPayload {
    public static final Type<ClientboundSyncArrowPickupStackPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_sync_arrow_pickup_stack"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncArrowPickupStackPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundSyncArrowPickupStackPayload::entityId,
            ItemStack.OPTIONAL_STREAM_CODEC, ClientboundSyncArrowPickupStackPayload::stack,
            ClientboundSyncArrowPickupStackPayload::new);

    @Override
    public void handle(Player player) {
        Entity entity = player.level().getEntity(entityId);
        if (entity instanceof AbstractArrow arrow) {
            arrow.pickupItemStack = stack.copy();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
