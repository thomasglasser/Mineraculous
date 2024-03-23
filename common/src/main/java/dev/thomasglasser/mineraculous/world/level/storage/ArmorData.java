package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public record ArmorData(ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots)
{
	public static final Codec<ArmorData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ItemStack.CODEC.fieldOf("helmet").forGetter(ArmorData::helmet),
			ItemStack.CODEC.fieldOf("chestplate").forGetter(ArmorData::chestplate),
			ItemStack.CODEC.fieldOf("leggings").forGetter(ArmorData::leggings),
			ItemStack.CODEC.fieldOf("boots").forGetter(ArmorData::boots))
			.apply(instance, ArmorData::new));

	public ArmorData()
	{
		this(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
	}

	public ItemStack forSlot(EquipmentSlot slot)
	{
		switch (slot)
		{
			case HEAD -> { return helmet; }
			case CHEST -> { return chestplate; }
			case LEGS -> { return leggings; }
			case FEET -> { return boots; }
			default -> { return ItemStack.EMPTY; }
		}
	}
}
