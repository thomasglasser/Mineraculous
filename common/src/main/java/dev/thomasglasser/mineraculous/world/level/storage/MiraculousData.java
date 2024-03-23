package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

public record MiraculousData(boolean transformed, ItemStack tool)
{
	public static final Codec<MiraculousData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.fieldOf("transformed").forGetter(MiraculousData::transformed),
			ItemStack.CODEC.fieldOf("tool").forGetter(MiraculousData::tool))
			.apply(instance, MiraculousData::new));

	public MiraculousData()
	{
		this(false, ItemStack.EMPTY);
	}
}
