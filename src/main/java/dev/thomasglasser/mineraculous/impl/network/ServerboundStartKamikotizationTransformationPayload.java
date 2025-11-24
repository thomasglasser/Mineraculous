package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.SlotInfo;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundStartKamikotizationTransformationPayload(KamikotizationData data, SlotInfo slotInfo) implements ExtendedPacketPayload {
    public static final Type<ServerboundStartKamikotizationTransformationPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_start_kamikotization_transformation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStartKamikotizationTransformationPayload> CODEC = StreamCodec.composite(
            KamikotizationData.STREAM_CODEC, ServerboundStartKamikotizationTransformationPayload::data,
            SlotInfo.STREAM_CODEC, ServerboundStartKamikotizationTransformationPayload::slotInfo,
            ServerboundStartKamikotizationTransformationPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ItemStack stack = slotInfo.getSlot().map(slot -> player.getInventory().getItem(slot), curiosData -> CuriosUtils.getStackInSlot(player, curiosData));
        ItemStack result = data.transform(player, (ServerLevel) player.level(), stack);
        slotInfo.getSlot().ifLeft(slot -> player.getInventory().setItem(slot, result)).ifRight(curiosData -> CuriosUtils.setStackInSlot(player, curiosData, result));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
