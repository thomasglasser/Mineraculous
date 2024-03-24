package dev.thomasglasser.mineraculous.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class MineraculousNeoForgeEntityEvents
{
	public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
		for (EntityType<? extends LivingEntity> type : MineraculousEntityTypes.getAllAttributes().keySet())
		{
			event.put(type, MineraculousEntityTypes.getAllAttributes().get(type));
		}
	}

	public static void onLivingDeath(LivingDeathEvent event)
	{
		MineraculousEntityEvents.onDeath(event.getEntity());
	}
}
