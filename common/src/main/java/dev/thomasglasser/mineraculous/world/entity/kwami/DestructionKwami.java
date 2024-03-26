package dev.thomasglasser.mineraculous.world.entity.kwami;

import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DestructionKwami extends Kwami
{
	public DestructionKwami(EntityType<? extends DestructionKwami> entityType, Level level)
	{
		super(entityType, level);
	}

	@Override
	public boolean isTreat(ItemStack stack)
	{
		return super.isTreat(stack) || stack.is(MineraculousItemTags.DESTRUCTION_KWAMI_TREATS);
	}

	@Override
	public SoundEvent getHungrySound()
	{
		// TODO: Destruction hungry sound
		return null;
	}
}
