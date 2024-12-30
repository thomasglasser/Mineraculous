package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.tommylib.api.network.ExtendedPacketPayload;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record ServerboundSetButterflyCaneShouldUncoverImpulsePayload(int slot, boolean bool) implements ExtendedPacketPayload {
    public static final Type<ServerboundSetButterflyCaneShouldUncoverImpulsePayload> TYPE = new Type<>(Mineraculous.modLoc("set_butterfly_cane_should_uncover_impulse"));
    public static final StreamCodec<ByteBuf, ServerboundSetButterflyCaneShouldUncoverImpulsePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundSetButterflyCaneShouldUncoverImpulsePayload::slot,
            ByteBufCodecs.BOOL, ServerboundSetButterflyCaneShouldUncoverImpulsePayload::bool,
            ServerboundSetButterflyCaneShouldUncoverImpulsePayload::new);

    @Override
    public void handle(Player player) {
        ItemStack stack = player.getInventory().getItem(slot);
        stack.set(MineraculousDataComponents.BUTTERFLY_CANE_UNCOVER_IMPULSE, bool());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
