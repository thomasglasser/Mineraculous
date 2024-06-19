package dev.thomasglasser.mineraculous.world.entity.kwami;

import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Plagg extends Kwami
{
	public Plagg(EntityType<? extends Plagg> entityType, Level level)
	{
		super(entityType, level);
	}

	@Override
	public boolean isFood(ItemStack stack)
	{
		return stack.is(MineraculousItemTags.PLAGG_FOODS);
	}

	@Override
	public boolean isTreat(ItemStack stack)
	{
		return stack.is(MineraculousItemTags.PLAGG_TREATS);
	}

	@Override
	public SoundEvent getHungrySound()
	{
		// TODO: Plagg hungry sound
		return null;
	}
}
