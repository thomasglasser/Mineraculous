package dev.thomasglasser.mineraculous.world.item.curio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CuriosData(int slot, String category, String name)
{
	public static final Codec<CuriosData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("slot").forGetter(CuriosData::slot),
			Codec.STRING.fieldOf("category").forGetter(CuriosData::category),
			Codec.STRING.fieldOf("name").forGetter(CuriosData::name)
	).apply(instance, CuriosData::new));

	public static final StreamCodec<ByteBuf, CuriosData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, CuriosData::slot,
			ByteBufCodecs.STRING_UTF8, CuriosData::category,
			ByteBufCodecs.STRING_UTF8, CuriosData::name,
			CuriosData::new
	);

	public CuriosData()
	{
		this(0, "", "");
	}
}
