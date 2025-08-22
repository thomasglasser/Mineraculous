package dev.thomasglasser.mineraculous.impl.network;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchCatStaffData;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

public record ServerboundCancelCatStaffPerchPayload() implements ExtendedPacketPayload {
    public static final ServerboundCancelCatStaffPerchPayload INSTANCE = new ServerboundCancelCatStaffPerchPayload();

    public static final CustomPacketPayload.Type<ServerboundCancelCatStaffPerchPayload> TYPE = new CustomPacketPayload.Type<>(Mineraculous.modLoc("cancel_cat_staff_perch"));
    public static final StreamCodec<ByteBuf, ServerboundCancelCatStaffPerchPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public void handle(Player player) {
        PerchCatStaffData.remove(player, true);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
