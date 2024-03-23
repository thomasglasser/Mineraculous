package dev.thomasglasser.mineraculous.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public class MineraculousNeoForgeEntityEvents
{
	public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
		for (EntityType<? extends LivingEntity> type : MineraculousEntityTypes.getAllAttributes().keySet())
		{
			event.put(type, MineraculousEntityTypes.getAllAttributes().get(type));
		}
	}
}
