package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundSetButterflyCaneIsOpenPayload(int slot, boolean bool) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetButterflyCaneIsOpenPayload> TYPE = new Type<>(Mineraculous.modLoc("set_butterfly_cane_is_open"));
    public static final StreamCodec<ByteBuf, ServerboundSetButterflyCaneIsOpenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundSetButterflyCaneIsOpenPayload::slot,
            ByteBufCodecs.BOOL, ServerboundSetButterflyCaneIsOpenPayload::bool,
            ServerboundSetButterflyCaneIsOpenPayload::new);

    @Override
    public void handle(Player player) {
        player.getInventory().getItem(slot).set(MineraculousDataComponents.BUTTERFLY_CANE_IS_OPEN, bool);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
