package dev.thomasglasser.mineraculous.impl.world.item.component;

import dev.thomasglasser.mineraculous.impl.world.level.storage.SlotInfo;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record Kamikotizing(SlotInfo slotInfo) {
    public static StreamCodec<ByteBuf, Kamikotizing> STREAM_CODEC = SlotInfo.STREAM_CODEC.map(Kamikotizing::new, Kamikotizing::slotInfo);
}
