package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.world.level.storage.newPerchingCatStaffData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundPerchVerticalInputPayload(boolean ascend, boolean descend) implements ExtendedPacketPayload {
    public static final Type<ServerboundPerchVerticalInputPayload> TYPE = new Type<>(MineraculousConstants.modLoc("perch_vertical_input"));
    public static final StreamCodec<ByteBuf, ServerboundPerchVerticalInputPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ServerboundPerchVerticalInputPayload::ascend,
            ByteBufCodecs.BOOL, ServerboundPerchVerticalInputPayload::descend,
            ServerboundPerchVerticalInputPayload::new);

    @Override
    public void handle(Player player) {
        newPerchingCatStaffData perchingData = player.getData(MineraculousAttachmentTypes.newPERCHING_CAT_STAFF);
        if (perchingData.isModeActive() && perchingData.state() == newPerchingCatStaffData.PerchingState.STAND) {
            newPerchingCatStaffData.VerticalMovement newMovement = newPerchingCatStaffData.getVerticalMovement(ascend, descend);
            perchingData.withVerticalMovement(newMovement).save(player);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
