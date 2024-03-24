package dev.thomasglasser.mineraculous.world.item.curio;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record CuriosData(int slot, String category, String name)
{
	public static final Codec<CuriosData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("slot").forGetter(CuriosData::slot),
			Codec.STRING.fieldOf("category").forGetter(CuriosData::category),
			Codec.STRING.fieldOf("name").forGetter(CuriosData::name)
	).apply(instance, CuriosData::new));

	public CuriosData()
	{
		this(0, "", "");
	}
}
