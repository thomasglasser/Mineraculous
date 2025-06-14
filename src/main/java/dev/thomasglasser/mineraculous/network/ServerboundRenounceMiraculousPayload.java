package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MiraculousUtils;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundRenounceMiraculousPayload(InteractionHand hand) implements ExtendedPacketPayload {
    public static final Type<ServerboundRenounceMiraculousPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_renounce_miraculous"));
    public static final StreamCodec<FriendlyByteBuf, ServerboundRenounceMiraculousPayload> CODEC = StreamCodec.composite(
            TommyLibExtraStreamCodecs.forEnum(InteractionHand.class), ServerboundRenounceMiraculousPayload::hand,
            ServerboundRenounceMiraculousPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        ItemStack stack = player.getItemInHand(hand);
        Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        if (miraculous != null) {
            MiraculousUtils.renounce(stack, (ServerLevel) player.level(), player.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).kwamiData());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
