package dev.thomasglasser.mineraculous.world.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

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

	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event)
	{
		event.setCancellationResult(MineraculousEntityEvents.onEntityInteract(event.getEntity(), event.getTarget(), event.getHand()));
	}

	public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event)
	{
		event.setCancellationResult(MineraculousEntityEvents.onBlockInteract(event.getEntity(), event.getHitVec(), event.getHand()));
	}

	public static void onAttackEntity(AttackEntityEvent event)
	{
		MineraculousEntityEvents.onAttackEntity(event.getEntity(), event.getTarget());
	}

	public static void onLivingAttack(LivingAttackEvent event)
	{
		MineraculousEntityEvents.onLivingAttack(event.getEntity(), event.getSource());
	}

	public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event)
	{
		event.setCancellationResult(MineraculousEntityEvents.onBlockLeftClick(event.getEntity(), event.getPos(), event.getHand()));
	}

	public static void onEffectRemoved(MobEffectEvent.Remove event)
	{
		if (MineraculousEntityEvents.isCataclysmed(event.getEntity()))
			event.setCanceled(true);
	}

	public static void onLivingHeal(LivingHealEvent event)
	{
		if (MineraculousEntityEvents.isCataclysmed(event.getEntity()))
			event.setCanceled(true);
	}

	public static void onLivingTick(LivingEvent.LivingTickEvent event)
	{
		MineraculousEntityEvents.tick(event.getEntity());
	}
}
