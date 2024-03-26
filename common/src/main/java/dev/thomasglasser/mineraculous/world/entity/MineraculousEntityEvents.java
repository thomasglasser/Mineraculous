package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.network.ClientboundMiraculousTransformPacket;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.tags.MineraculousBlockTags;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import dev.thomasglasser.tommylib.api.world.entity.DataHolder;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class MineraculousEntityEvents
{
	public static final String TAG_CATACLYSMED = "Cataclysmed";

	public static final BiFunction<MobEffect, Integer, MobEffectInstance> INFINITE_HIDDEN_EFFECT = (effect, amplifier) -> new MobEffectInstance(effect, -1, amplifier, false, false, false);

	public static final List<MobEffect> MIRACULOUS_EFFECTS = List.of(
			MobEffects.DAMAGE_RESISTANCE,
			MobEffects.DAMAGE_BOOST,
			MobEffects.MOVEMENT_SPEED,
			MobEffects.DIG_SPEED,
			MobEffects.JUMP,
			MobEffects.REGENERATION,
			MobEffects.HEALTH_BOOST,
			MobEffects.SATURATION,
			MobEffects.ABSORPTION
	);

	public static void onDeath(LivingEntity entity)
	{
		MiraculousData data = Services.DATA.getMiraculousData(entity);
		if (entity instanceof ServerPlayer player)
		{
			if (data.transformed())
			{
				handleTransformation(player, data.miraculous(), data.miraculousData(), false);
				if (player.serverLevel().getEntity(data.miraculous().getOrCreateTag().getCompound(MiraculousItem.TAG_KWAMIDATA).getUUID("UUID")) instanceof Kwami kwami)
				{
					renounceMiraculous(data.miraculous(), kwami);
				}
			}
			player.getInventory().items.stream().filter(stack -> stack.getItem() instanceof MiraculousItem).forEach(stack -> renounceMiraculous(stack, null));
		}
	}

	public static void handleTransformation(Player player, ItemStack miraculous, CuriosData curiosData, boolean transform)
	{
		if (player != null)
		{
			ServerLevel serverLevel = (ServerLevel) player.level();
			MiraculousData miraculousData = Services.DATA.getMiraculousData(player);
			if (transform)
			{
				// Transform
				CompoundTag data = miraculous.getOrCreateTag().getCompound(MiraculousItem.TAG_KWAMIDATA);
				Entity entity = serverLevel.getEntity(data.getUUID("UUID"));
				if (entity instanceof Kwami kwami)
				{
					if (kwami.isCharged() && miraculous.getItem() instanceof MiraculousItem miraculousItem)
					{
						// TODO: Sound
//						level.playSound(null, player.getX(), player.getY(), player.getZ(), transformSound, SoundSource.PLAYERS, 1, 1);

						ArmorData armor = new ArmorData(player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST), player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET));
						Services.DATA.setStoredArmor(armor, player);
						ArmorSet set = miraculousItem.getArmorSet();
						if (set != null)
						{
							for (EquipmentSlot slot : EquipmentSlot.values())
							{
								RegistryObject<ArmorItem> armorPiece = set.getForSlot(slot);
								if (!(armorPiece == null))
								{
									ItemStack stack = armorPiece.get().getDefaultInstance();
									stack.enchant(Enchantments.BINDING_CURSE, 1);
									stack.getOrCreateTag().putBoolean("HideFlags", true);
									player.setItemSlot(slot, stack);
								}
							}
						}

						miraculous.enchant(Enchantments.BINDING_CURSE, 1);
						miraculous.getOrCreateTag().putBoolean("HideFlags", true);

						ItemStack tool = miraculousItem.getTool() == null ? ItemStack.EMPTY : miraculousItem.getTool().getDefaultInstance();
						player.addItem(tool);
						Services.DATA.setMiraculousData(new MiraculousData(true, miraculous, curiosData, tool, miraculousData.powerLevel(), false, false), player, true);
						miraculous.getOrCreateTag().putBoolean(MiraculousItem.TAG_POWERED, true);
						Services.CURIOS.setStackInSlot(player, curiosData, miraculous, true);
						TommyLibServices.NETWORK.sendToAllClients(ClientboundMiraculousTransformPacket.class, ClientboundMiraculousTransformPacket.write(miraculous, curiosData, transform, Services.DATA.getMiraculousData(player).tool()), serverLevel.getServer());
						MIRACULOUS_EFFECTS.forEach(effect -> player.addEffect(INFINITE_HIDDEN_EFFECT.apply(effect, miraculousData.powerLevel())));
						if (miraculous.is(MineraculousItems.CAT_MIRACULOUS.get()))
							player.addEffect(INFINITE_HIDDEN_EFFECT.apply(MobEffects.NIGHT_VISION, 1));
						kwami.discard();
						// TODO: Advancement trigger with miraculous context
					}
					else
					{
						// TODO: Hungry sound
//						kwami.playSound(kwami.getHungrySound());
					}
				}
				else
				{
					miraculous.getOrCreateTag().remove(MiraculousItem.TAG_KWAMIDATA);
					Services.CURIOS.setStackInSlot(player, curiosData, miraculous, true);
				}
			}
			else
			{
				// De-transform
				Kwami kwami = summonKwami(player.level(), miraculous, curiosData, player);
				if (kwami != null)
				{
					kwami.setCharged(false);
				}
				else
				{
					Mineraculous.LOGGER.error("Kwami could not be created for player " + player.getName().getString());
					return;
				}
				ArmorData armor = Services.DATA.getStoredArmor(player);
				for (EquipmentSlot slot : Arrays.stream(EquipmentSlot.values()).filter(slot -> slot.getType() == EquipmentSlot.Type.ARMOR).toArray(EquipmentSlot[]::new))
				{
					player.setItemSlot(slot, armor.forSlot(slot));
				}
				miraculous.removeTagKey("Enchantments");
				miraculous.getOrCreateTag().putBoolean(MiraculousItem.TAG_POWERED, false);
				miraculous.getOrCreateTag().remove(MiraculousItem.TAG_REMAININGTICKS);
				Services.CURIOS.setStackInSlot(player, curiosData, miraculous, true);
				miraculousData.tool().getOrCreateTag().putBoolean(MiraculousItem.TAG_RECALLED, true);
				Services.DATA.setMiraculousData(new MiraculousData(false, miraculous, curiosData, ItemStack.EMPTY, miraculousData.powerLevel(), false, false), player, true);
				TommyLibServices.NETWORK.sendToAllClients(ClientboundMiraculousTransformPacket.class, ClientboundMiraculousTransformPacket.write(miraculous, curiosData, transform, Services.DATA.getMiraculousData(player).tool()), serverLevel.getServer());
				MIRACULOUS_EFFECTS.forEach(player::removeEffect);
				if (miraculous.is(MineraculousItems.CAT_MIRACULOUS.get()))
					player.removeEffect(MobEffects.NIGHT_VISION);
			}
		}
	}

	public static boolean renounceMiraculous(ItemStack miraculous, Kwami kwami)
	{
		CompoundTag tag = miraculous.getOrCreateTag();
		CompoundTag data = tag.getCompound(MiraculousItem.TAG_KWAMIDATA);
		if (data.hasUUID("UUID") && kwami.getUUID().equals(data.getUUID("UUID")))
		{
			data.putBoolean(Kwami.TAG_HASVOICE, kwami.hasVoice());
			data.putBoolean(Kwami.TAG_CHARGED, kwami.isCharged());
			tag.put(MiraculousItem.TAG_KWAMIDATA, data);
			tag.putBoolean(MiraculousItem.TAG_POWERED, true);
			kwami.discard();
			// TODO: Play kwami hiding sound
			return true;
		}
		return false;
	}

	public static Kwami summonKwami(Level level, ItemStack itemStack, CuriosData curiosData, Player player)
	{
		Kwami kwami = ((MiraculousItem)itemStack.getItem()).getKwamiType().create(level);
		if (kwami != null)
		{
			CompoundTag data = itemStack.getOrCreateTag().getCompound(MiraculousItem.TAG_KWAMIDATA);
			if (data.contains("UUID"))
				kwami.setUUID(data.getUUID("UUID"));
			if (data.contains(Kwami.TAG_HASVOICE))
			{
				kwami.setHasVoice(data.getBoolean(Kwami.TAG_HASVOICE));
			}
			else
			{
				kwami.setHasVoice(true);
			}
			if (data.contains(Kwami.TAG_CHARGED))
			{
				kwami.setCharged(data.getBoolean(Kwami.TAG_CHARGED));
			}
			else
			{
				kwami.setCharged(true);
			}
			kwami.setPos(player.getX() + level.random.nextInt(3), player.getY() + 2, player.getZ() +  + level.random.nextInt(3));
			kwami.tame(player);
			level.addFreshEntity(kwami);

			data.putUUID("UUID", kwami.getUUID());
			data.putBoolean(Kwami.TAG_HASVOICE, kwami.hasVoice());
			data.putBoolean(Kwami.TAG_CHARGED, kwami.isCharged());
			itemStack.getOrCreateTag().put(MiraculousItem.TAG_KWAMIDATA, data);
			Services.CURIOS.setStackInSlot(player, curiosData, itemStack, true);
		}
		return kwami;
	}

	public static InteractionResult testAndApplyCataclysmEffects(LivingEntity entity, Entity target, InteractionHand hand)
	{
		MiraculousData miraculousData = Services.DATA.getMiraculousData(entity);
		if (!entity.level().isClientSide && hand == InteractionHand.MAIN_HAND && miraculousData.transformed() && miraculousData.miraculous().is(MineraculousItems.CAT_MIRACULOUS.get()) && miraculousData.powerActive())
		{
			int level = miraculousData.powerLevel();
			if (target instanceof LivingEntity livingEntity)
			{
				List<MobEffect> CATACLYSM_EFFECTS = List.of(
						MobEffects.WITHER,
						MobEffects.BLINDNESS,
						MobEffects.WEAKNESS,
						MobEffects.MOVEMENT_SLOWDOWN,
						MobEffects.HUNGER,
						MobEffects.CONFUSION,
						MobEffects.DIG_SLOWDOWN
				);
				CATACLYSM_EFFECTS.forEach(effect -> livingEntity.addEffect(INFINITE_HIDDEN_EFFECT.apply(effect, level)));
			}
			else
			{
				target.hurt(entity.damageSources().indirectMagic(entity, entity), 1024);
			}
			((DataHolder)(target)).getPersistentData().putBoolean(TAG_CATACLYSMED, true);
			Services.DATA.setMiraculousData(new MiraculousData(true, miraculousData.miraculous(), miraculousData.miraculousData(), miraculousData.tool(), miraculousData.powerLevel(), true, false), entity, true);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public static InteractionResult onEntityInteract(LivingEntity entity, Entity target, InteractionHand hand)
	{
		return testAndApplyCataclysmEffects(entity, target, hand);
	}

	public static InteractionResult onAttackEntity(Player player, Entity entity)
	{
		if (!(entity instanceof LivingEntity))
			return testAndApplyCataclysmEffects(player, entity, InteractionHand.MAIN_HAND);
		return InteractionResult.PASS;
	}

	public static void onLivingAttack(LivingEntity target, DamageSource source)
	{
		if (source.getDirectEntity() instanceof LivingEntity livingEntity)
		{
			testAndApplyCataclysmEffects(livingEntity, target, InteractionHand.MAIN_HAND);
		}
	}

	public static InteractionResult testAndApplyCataclysmToBlocks(LivingEntity entity, BlockPos pos, InteractionHand hand)
	{
		Level level = entity.level();
		MiraculousData miraculousData = Services.DATA.getMiraculousData(entity);
		if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && miraculousData.transformed() && miraculousData.miraculous().is(MineraculousItems.CAT_MIRACULOUS.get()) && miraculousData.powerActive())
		{
			if (level.getBlockState(pos).is(MineraculousBlockTags.CATACLYSM_IMMUNE))
				return InteractionResult.PASS;

			// TODO: Cataclysmize block and nearby blocks
			level.destroyBlock(pos, false, entity);
			Services.DATA.setMiraculousData(new MiraculousData(true, miraculousData.miraculous(), miraculousData.miraculousData(), miraculousData.tool(), miraculousData.powerLevel(), true, false), entity, true);

			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public static InteractionResult onBlockInteract(LivingEntity entity, BlockHitResult hitResult, InteractionHand hand)
	{
		return testAndApplyCataclysmToBlocks(entity, hitResult.getBlockPos(), hand);
	}

	public static InteractionResult onBlockLeftClick(Player player, BlockPos pos, InteractionHand hand)
	{
		return testAndApplyCataclysmToBlocks(player, pos, hand);
	}

	public static ItemStack convertToMiraculousDust(ItemStack stack)
	{
		if (!stack.is(MineraculousItemTags.CATACLYSM_IMMUNE))
		{
			return MineraculousItems.CATACLYSM_DUST.get().getDefaultInstance();
		}
		return stack;
	}

	public static boolean isCataclysmed(Entity entity)
	{
		return ((DataHolder)entity).getPersistentData().getBoolean(TAG_CATACLYSMED);
	}
}
