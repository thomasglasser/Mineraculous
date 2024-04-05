package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.network.ClientboundLivingEntityCataclysmedPacket;
import dev.thomasglasser.mineraculous.network.ClientboundMiraculousTransformPacket;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.tags.MineraculousBlockTags;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import dev.thomasglasser.tommylib.api.world.entity.DataHolder;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class MineraculousEntityEvents
{
	public static final String TAG_CATACLYSMED = "Cataclysmed";
	public static final String TAG_HASCATVISION = "HasCatVision";

	public static final ResourceLocation CAT_VISION_SHADER = new ResourceLocation("shaders/post/creeper.json");

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
		MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(entity);
		if (entity instanceof ServerPlayer player)
		{
			miraculousDataSet.keySet().forEach(type ->
			{
				MiraculousData data = miraculousDataSet.get(type);
				if (data.transformed())
					handleTransformation(player, type, data, false);
				if (player.serverLevel().getEntity(data.miraculousItem().getOrCreateTag().getCompound(MiraculousItem.TAG_KWAMIDATA).getUUID("UUID")) instanceof Kwami kwami)
				{
					renounceMiraculous(data.miraculousItem(), kwami);
				}
			});
		}
	}

	public static void handleTransformation(Player player, MiraculousType type, MiraculousData data, boolean transform)
	{
		if (player != null)
		{
			ServerLevel serverLevel = (ServerLevel) player.level();
			ItemStack miraculousStack = data.miraculousItem();
			if (transform)
			{
				// Transform
				CompoundTag kwamiData = miraculousStack.getOrCreateTag().getCompound(MiraculousItem.TAG_KWAMIDATA);
				Entity entity = serverLevel.getEntity(kwamiData.getUUID("UUID"));
				if (entity instanceof Kwami kwami)
				{
					if (kwami.isCharged() && miraculousStack.getItem() instanceof MiraculousItem miraculousItem)
					{
						// TODO: Sound
//						level.playSound(null, player.getX(), player.getY(), player.getZ(), transformSound, SoundSource.PLAYERS, 1, 1);

						ArmorData armor = new ArmorData(player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST), player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET));
						Services.DATA.setStoredArmor(player, armor);
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

						miraculousStack.enchant(Enchantments.BINDING_CURSE, 1);
						miraculousStack.getOrCreateTag().putBoolean("HideFlags", true);

						ItemStack tool = miraculousItem.getTool() == null ? ItemStack.EMPTY : miraculousItem.getTool().getDefaultInstance();
						player.addItem(tool);
						miraculousStack.getOrCreateTag().putBoolean(MiraculousItem.TAG_POWERED, true);
						data = new MiraculousData(true, miraculousStack, data.curiosData(), tool, data.powerLevel(), false, false, data.name());
						Services.DATA.getMiraculousDataSet(player).put(player, type, data, true);
						Services.CURIOS.setStackInSlot(player, data.curiosData(), miraculousStack, true);
						TommyLibServices.NETWORK.sendToAllClients(ClientboundMiraculousTransformPacket.ID, ClientboundMiraculousTransformPacket::new, ClientboundMiraculousTransformPacket.write(type, data), serverLevel.getServer());
						int powerLevel = data.powerLevel();
						MIRACULOUS_EFFECTS.forEach(effect -> player.addEffect(INFINITE_HIDDEN_EFFECT.apply(effect, powerLevel)));
						kwami.discard();
						// TODO: Advancement trigger with miraculous item context
					}
					else
					{
						// TODO: Hungry sound
//						kwami.playSound(kwami.getHungrySound());
					}
				}
				else
				{
					miraculousStack.getOrCreateTag().remove(MiraculousItem.TAG_KWAMIDATA);
					Services.CURIOS.setStackInSlot(player, data.curiosData(), miraculousStack, true);
					Services.DATA.getMiraculousDataSet(player).put(player, type, new MiraculousData(true, miraculousStack, data.curiosData(), data.tool(), data.powerLevel(), false, false, data.name()), true);
				}
			}
			else
			{
				// De-transform
				Kwami kwami = summonKwami(player.level(), type, data, player);
				if (kwami != null)
				{
					kwami.setCharged(false);
				}
				else
				{
					Mineraculous.LOGGER.error("Kwami could not be created for player " + player.getName().plainCopy().getString());
					return;
				}
				ArmorData armor = Services.DATA.getStoredArmor(player);
				for (EquipmentSlot slot : Arrays.stream(EquipmentSlot.values()).filter(slot -> slot.getType() == EquipmentSlot.Type.ARMOR).toArray(EquipmentSlot[]::new))
				{
					player.setItemSlot(slot, armor.forSlot(slot));
				}
				miraculousStack.removeTagKey("Enchantments");
				miraculousStack.getOrCreateTag().putBoolean(MiraculousItem.TAG_POWERED, false);
				miraculousStack.getOrCreateTag().remove(MiraculousItem.TAG_REMAININGTICKS);
				Services.CURIOS.setStackInSlot(player, data.curiosData(), miraculousStack, true);
				data.tool().getOrCreateTag().putBoolean(MiraculousItem.TAG_RECALLED, true);
				data = new MiraculousData(false, miraculousStack, data.curiosData(), data.tool(), data.powerLevel(), false, false, data.name());
				Services.DATA.getMiraculousDataSet(player).put(player, type, data, true);
				TommyLibServices.NETWORK.sendToAllClients(ClientboundMiraculousTransformPacket.ID, ClientboundMiraculousTransformPacket::new, ClientboundMiraculousTransformPacket.write(type, data), serverLevel.getServer());
				MIRACULOUS_EFFECTS.forEach(player::removeEffect);
			}
		}
	}

	public static boolean renounceMiraculous(ItemStack miraculous, Kwami kwami)
	{
		CompoundTag tag = miraculous.getOrCreateTag();
		CompoundTag data = tag.getCompound(MiraculousItem.TAG_KWAMIDATA);
		if (data.hasUUID("UUID") && kwami.getUUID().equals(data.getUUID("UUID")))
		{
			data.putBoolean(Kwami.TAG_CHARGED, kwami.isCharged());
			tag.put(MiraculousItem.TAG_KWAMIDATA, data);
			tag.putBoolean(MiraculousItem.TAG_POWERED, true);
			kwami.discard();
			// TODO: Play kwami hiding sound
			return true;
		}
		return false;
	}

	public static Kwami summonKwami(Level level, MiraculousType type, MiraculousData miraculousData, Player player)
	{
		if (miraculousData.miraculousItem().getItem() instanceof MiraculousItem miraculousItem)
		{
			Kwami kwami = miraculousItem.getKwamiType().create(level);
			if (kwami != null)
			{
				CompoundTag data = miraculousData.miraculousItem().getOrCreateTag().getCompound(MiraculousItem.TAG_KWAMIDATA);
				if (data.contains("UUID")) kwami.setUUID(data.getUUID("UUID"));
				if (data.contains(Kwami.TAG_CHARGED))
				{
					kwami.setCharged(data.getBoolean(Kwami.TAG_CHARGED));
				}
				else
				{
					kwami.setCharged(true);
				}
				kwami.setPos(player.getX() + level.random.nextInt(3), player.getY() + 2, player.getZ() + +level.random.nextInt(3));
				kwami.tame(player);
				level.addFreshEntity(kwami);

				data.putUUID("UUID", kwami.getUUID());
				data.putBoolean(Kwami.TAG_CHARGED, kwami.isCharged());
				miraculousData.miraculousItem().getOrCreateTag().put(MiraculousItem.TAG_KWAMIDATA, data);
				Services.CURIOS.setStackInSlot(player, miraculousData.curiosData(), miraculousData.miraculousItem(), true);
				Services.DATA.getMiraculousDataSet(player).put(player, type, new MiraculousData(false, miraculousData.miraculousItem(), miraculousData.curiosData(), miraculousData.tool(), miraculousData.powerLevel(), false, false, miraculousData.name()), true);
			}
			return kwami;
		}
		return null;
	}

	public static InteractionResult testAndApplyCataclysmEffects(LivingEntity entity, Entity target, InteractionHand hand)
	{
		MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(entity);
		MiraculousData catMiraculousData = miraculousDataSet.get(MiraculousType.CAT);
		if (!entity.level().isClientSide && hand == InteractionHand.MAIN_HAND && catMiraculousData.transformed() && catMiraculousData.mainPowerActive())
		{
			int level = catMiraculousData.powerLevel();
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
				TommyLibServices.NETWORK.sendToAllClients(ClientboundLivingEntityCataclysmedPacket.ID, ClientboundLivingEntityCataclysmedPacket::new, ClientboundLivingEntityCataclysmedPacket.write(livingEntity), entity.level().getServer());
			}
			else if (target instanceof VehicleEntity vehicle)
			{
				target.discard();
				Block.popResource(target.level(), vehicle.blockPosition(), MineraculousItems.CATACLYSM_DUST.get().getDefaultInstance());
			}
			else
			{
				target.hurt(entity.damageSources().indirectMagic(entity, entity), 1024);
			}
			((DataHolder)(target)).getPersistentData().putBoolean(TAG_CATACLYSMED, true);
			miraculousDataSet.put(entity, MiraculousType.CAT, new MiraculousData(true, catMiraculousData.miraculousItem(), catMiraculousData.curiosData(), catMiraculousData.tool(), catMiraculousData.powerLevel(), true, false, catMiraculousData.name()), true);
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
		MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(entity);
		MiraculousData catMiraculousData = miraculousDataSet.get(MiraculousType.CAT);
		if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && catMiraculousData.transformed() && catMiraculousData.mainPowerActive())
		{
			BlockState state = level.getBlockState(pos);
			if (state.is(MineraculousBlockTags.CATACLYSM_IMMUNE))
				return InteractionResult.PASS;

			// TODO: Cataclysmize block and nearby blocks
			level.destroyBlock(pos, false, entity);
			ServerLevel serverLevel = (ServerLevel) level;
			Block.getDrops(state, serverLevel, pos, level.getBlockEntity(pos) == null ? null : level.getBlockEntity(pos), entity, ItemStack.EMPTY).stream().map(MineraculousEntityEvents::convertToCataclysmDust).forEach(stack -> Block.popResource(level, pos, stack));
			state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, true);
			miraculousDataSet.put(entity, MiraculousType.CAT, new MiraculousData(true, catMiraculousData.miraculousItem(), catMiraculousData.curiosData(), catMiraculousData.tool(), catMiraculousData.powerLevel(), true, false, catMiraculousData.name()), true);
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

	public static ItemStack convertToCataclysmDust(ItemStack stack)
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

	public static Component formatDisplayName(LivingEntity entity, Component original)
	{
		if (original != null)
		{
			Style style = original.getStyle();
			MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(entity);
			List<MiraculousType> transformed = miraculousDataSet.getTransformed();
			if (transformed.size() > 1)
			{
				// TODO: Support for name and color based on combinations of miraculous types, like Monarch being purple
			}
			else
			{
				MiraculousData data = miraculousDataSet.get(transformed.get(0));
				if (data.miraculousItem().getItem() instanceof MiraculousItem miraculousItem)
				{
					Style newStyle = style.withColor(miraculousItem.getPowerColor());
					if (!data.name().isEmpty())
						return Component.literal(data.name()).setStyle(newStyle);
					return original.copy().setStyle(newStyle.withObfuscated(true));
				}
			}
		}
		return original;
	}
}
