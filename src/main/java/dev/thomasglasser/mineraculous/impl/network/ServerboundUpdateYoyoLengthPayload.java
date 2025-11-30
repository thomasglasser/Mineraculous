package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundUpdateYoyoLengthPayload(boolean increase) implements ExtendedPacketPayload {
    public static final Type<ServerboundUpdateYoyoLengthPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_update_yoyo_length"));
    public static final StreamCodec<ByteBuf, ServerboundUpdateYoyoLengthPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ServerboundUpdateYoyoLengthPayload::increase,
            ServerboundUpdateYoyoLengthPayload::new);

    @Override
    public void handle(Player player) {
        ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
        ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(player.level());
        if (thrownYoyo != null && thrownYoyo.canHandleInput()) {
            if (increase) {
                thrownYoyo.setMaxRopeLength(thrownYoyo.getMaxRopeLength() + 0.3f);
            } else {
                thrownYoyo.setMaxRopeLength(thrownYoyo.getMaxRopeLength() - 0.3f);
            }
        } else {
            player.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).ifPresent(leashingData -> leashingData.withMaxRopeLength(leashingData.maxRopeLength() + (increase ? 0.5f : -0.5f)).save(player));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
