package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import net.minecraft.world.item.ItemStack;

public record MiraculousData(boolean transformed, ItemStack miraculous, CuriosData miraculousData, ItemStack tool, int powerLevel)
{
	public static final Codec<MiraculousData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.fieldOf("transformed").forGetter(MiraculousData::transformed),
			ItemStack.CODEC.fieldOf("miraculous").forGetter(MiraculousData::miraculous),
			CuriosData.CODEC.fieldOf("miraculous_data").forGetter(MiraculousData::miraculousData),
			ItemStack.CODEC.fieldOf("tool").forGetter(MiraculousData::tool),
			Codec.INT.fieldOf("power_level").forGetter(MiraculousData::powerLevel)
	).apply(instance, MiraculousData::new));

	public MiraculousData()
	{
		this(false, ItemStack.EMPTY, new CuriosData(), ItemStack.EMPTY, 0);
	}
}
