package dev.thomasglasser.mineraculous.api.world.entity.curios;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import top.theillusivec4.curios.api.SlotContext;

/**
 * Holds information about the location of something in an entity's Curios inventory
 * 
 * @param identifier The identifier of the slot type
 * @param index      The index of the slot
 */
public record CuriosData(String identifier, int index) {
    public static final Codec<CuriosData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("identifier").forGetter(CuriosData::identifier),
            Codec.INT.fieldOf("index").forGetter(CuriosData::index)).apply(instance, CuriosData::new));
    public static final StreamCodec<ByteBuf, CuriosData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CuriosData::identifier,
            ByteBufCodecs.VAR_INT, CuriosData::index,
            CuriosData::new);

    public CuriosData(SlotContext slotContext) {
        this(slotContext.identifier(), slotContext.index());
    }
}
