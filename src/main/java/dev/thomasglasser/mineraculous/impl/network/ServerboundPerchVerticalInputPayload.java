package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchingCatStaffData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundPerchVerticalInputPayload(PerchingCatStaffData.VerticalMovement suggestedMovement) implements ExtendedPacketPayload {
    public static final Type<ServerboundPerchVerticalInputPayload> TYPE = new Type<>(MineraculousConstants.modLoc("perch_vertical_input"));
    public static final StreamCodec<ByteBuf, ServerboundPerchVerticalInputPayload> CODEC = StreamCodec.composite(
            PerchingCatStaffData.VerticalMovement.STREAM_CODEC, ServerboundPerchVerticalInputPayload::suggestedMovement,
            ServerboundPerchVerticalInputPayload::new);

    @Override
    public void handle(Player player) {
        PerchingCatStaffData perchingData = player.getData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF);
        if (perchingData.isModeActive() && perchingData.state() == PerchingCatStaffData.PerchingState.STAND) {
            perchingData.withVerticalMovement(suggestedMovement).save(player);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
