package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public class ServerboundToggleNightVisionPayload implements ExtendedPacketPayload {
    public static final ServerboundToggleNightVisionPayload INSTANCE = new ServerboundToggleNightVisionPayload();
    public static final Type<ServerboundToggleNightVisionPayload> TYPE = new Type<>(MineraculousConstants.modLoc("serverbound_toggle_night_vision"));
    public static final StreamCodec<ByteBuf, ServerboundToggleNightVisionPayload> CODEC = StreamCodec.unit(INSTANCE);

    private ServerboundToggleNightVisionPayload() {}

    @Override
    public void handle(Player player) {
        player.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withToggleNightVision().save(player, true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
