package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ClientboundSyncCurioPayload(int entity, CuriosData curiosData, ItemStack stack) implements ExtendedPacketPayload {

    public static final Type<ClientboundSyncCurioPayload> TYPE = new Type<>(Mineraculous.modLoc("clientbound_sync_curio"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncCurioPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundSyncCurioPayload::entity,
            CuriosData.STREAM_CODEC, ClientboundSyncCurioPayload::curiosData,
            ItemStack.OPTIONAL_STREAM_CODEC, ClientboundSyncCurioPayload::stack,
            ClientboundSyncCurioPayload::new);

    // ON CLIENT
    @Override
    public void handle(Player player) {
        Entity found = player.level().getEntity(entity);
        if (found instanceof LivingEntity livingEntity)
            CuriosUtils.setStackInSlot(livingEntity, curiosData, stack, false);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
