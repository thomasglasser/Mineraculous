package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetShowKamikoMaskPayload(boolean show) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetShowKamikoMaskPayload> TYPE = new Type<>(Mineraculous.modLoc("serverbound_set_show_kamiko_mask"));
    public static final StreamCodec<ByteBuf, ServerboundSetShowKamikoMaskPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ServerboundSetShowKamikoMaskPayload::show,
            ServerboundSetShowKamikoMaskPayload::new);

    // ON SERVER
    @Override
    public void handle(Player player) {
        CompoundTag tag = TommyLibServices.ENTITY.getPersistentData(player);
        tag.putBoolean(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK, show);
        tag.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
        TommyLibServices.ENTITY.setPersistentData(player, tag, true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
