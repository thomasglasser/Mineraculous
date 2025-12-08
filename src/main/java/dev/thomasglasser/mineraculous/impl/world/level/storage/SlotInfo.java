package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class SlotInfo {
    public static final Codec<SlotInfo> CODEC = Codec.either(Codec.INT, CuriosData.CODEC).xmap(SlotInfo::new, SlotInfo::getSlot);
    public static final StreamCodec<ByteBuf, SlotInfo> STREAM_CODEC = ByteBufCodecs.either(ByteBufCodecs.VAR_INT, CuriosData.STREAM_CODEC).map(SlotInfo::new, SlotInfo::getSlot);

    private final Either<Integer, CuriosData> slot;

    private SlotInfo(Either<Integer, CuriosData> slot) {
        this.slot = slot;
    }

    public SlotInfo(int slot) {
        this(Either.left(slot));
    }

    public SlotInfo(CuriosData curiosData) {
        this(Either.right(curiosData));
    }

    public Either<Integer, CuriosData> getSlot() {
        return slot;
    }
}
