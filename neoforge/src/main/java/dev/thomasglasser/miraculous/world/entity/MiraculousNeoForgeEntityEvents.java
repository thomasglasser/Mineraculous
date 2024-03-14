package dev.thomasglasser.miraculous.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public class MiraculousNeoForgeEntityEvents
{
	public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
		for (EntityType<? extends LivingEntity> type : MiraculousEntityTypes.getAllAttributes().keySet())
		{
			event.put(type, MiraculousEntityTypes.getAllAttributes().get(type));
		}
	}
}
