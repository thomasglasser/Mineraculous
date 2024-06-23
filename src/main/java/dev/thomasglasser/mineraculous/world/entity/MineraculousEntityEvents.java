package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalCuriosInventoryScreen;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ClientboundMiraculousTransformPayload;
import dev.thomasglasser.mineraculous.network.ServerboundRequestInventorySyncPayload;
import dev.thomasglasser.mineraculous.network.ServerboundWakeUpPayload;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.tags.MineraculousBlockTags;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.registration.DeferredItem;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MangroveRootsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class MineraculousEntityEvents
{
	public static final String TAG_WAITTICKS = "WaitTicks";
	public static final String TAG_CATACLYSMED = "Cataclysmed";
	public static final String TAG_HASCATVISION = "HasCatVision";
	public static final String TAG_TAKETICKS = "TakeTicks";
	
	public static final ResourceLocation CAT_VISION_SHADER = ResourceLocation.withDefaultNamespace("shaders/post/creeper.json");

	public static final BiFunction<Holder<MobEffect>, Integer, MobEffectInstance> INFINITE_HIDDEN_EFFECT = (effect, amplifier) -> new MobEffectInstance(effect, -1, amplifier, false, false, false);

	public static final List<Holder<MobEffect>> MIRACULOUS_EFFECTS = List.of(
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

	public static void onLivingTick(EntityTickEvent.Post event)
	{
		CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(event.getEntity());
		int waitTicks = entityData.getInt(MineraculousEntityEvents.TAG_WAITTICKS);
		if (waitTicks > 0)
		{
			entityData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, --waitTicks);
		}
		TommyLibServices.ENTITY.setPersistentData(event.getEntity(), entityData, false);
	}

	public static void onPlayerTick(PlayerTickEvent.Post event)
	{
		Player player = event.getEntity();
		CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(player);

		if (player.level().isClientSide)
		{
			int takeTicks = entityData.getInt(MineraculousEntityEvents.TAG_TAKETICKS);
			if (MineraculousKeyMappings.TAKE_ITEM.isDown() && player.getMainHandItem().isEmpty() && MineraculousClientUtils.getLookEntity() instanceof Player target && (MineraculousServerConfig.enableUniversalStealing || /*TODO: Is akumatized*/ player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).isTransformed()) && (MineraculousServerConfig.enableSleepStealing || !target.isSleeping()))
			{
				entityData.putInt(MineraculousEntityEvents.TAG_TAKETICKS, ++takeTicks);
				if (target.isSleeping() && MineraculousServerConfig.wakeUpChance > 0 && (MineraculousServerConfig.wakeUpChance >= 100 || player.getRandom().nextFloat() < MineraculousServerConfig.wakeUpChance / (20f * 5 * 100)))
				{
					TommyLibServices.NETWORK.sendToServer(new ServerboundWakeUpPayload(target.getUUID(), true));
				}
				if (takeTicks > (20 * MineraculousServerConfig.stealingDuration))
				{
					TommyLibServices.NETWORK.sendToServer(new ServerboundRequestInventorySyncPayload(target.getUUID()));
					ClientUtils.setScreen(new ExternalCuriosInventoryScreen(target));
					entityData.putInt(MineraculousEntityEvents.TAG_TAKETICKS, 0);
				}
				TommyLibServices.ENTITY.setPersistentData(player, entityData, false);
			}
			else if (takeTicks > 0)
			{
				entityData.putInt(MineraculousEntityEvents.TAG_TAKETICKS, 0);
				TommyLibServices.ENTITY.setPersistentData(player, entityData, false);
			}
		}
	}

	public static void onLivingDeath(LivingDeathEvent event)
	{
		LivingEntity entity = event.getEntity();
		MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS.get());
		if (entity instanceof ServerPlayer player)
		{
			miraculousDataSet.keySet().forEach(type ->
			{
				MiraculousData data = miraculousDataSet.get(type);
				if (data.transformed())
					handleTransformation(player, type, data, false);
				KwamiData kwamiData = data.miraculousItem().get(MineraculousDataComponents.KWAMI_DATA.get());
				if (kwamiData != null && player.serverLevel().getEntity(kwamiData.uuid()) instanceof Kwami kwami)
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
				KwamiData kwamiData = miraculousStack.get(MineraculousDataComponents.KWAMI_DATA.get());
				Entity entity = serverLevel.getEntity(kwamiData.uuid());
				if (entity instanceof Kwami kwami)
				{
					if (kwami.isCharged() && miraculousStack.getItem() instanceof MiraculousItem miraculousItem)
					{
						// TODO: Sound
//						level.playSound(null, player.getX(), player.getY(), player.getZ(), transformSound, SoundSource.PLAYERS, 1, 1);

						ArmorData armor = new ArmorData(player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST), player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET));
						entity.setData(MineraculousAttachmentTypes.STORED_ARMOR, armor);
						ArmorSet set = miraculousItem.getArmorSet();
						if (set != null)
						{
							for (EquipmentSlot slot : EquipmentSlot.values())
							{
								DeferredItem<ArmorItem> armorPiece = set.getForSlot(slot);
								if (!(armorPiece == null))
								{
									ItemStack stack = armorPiece.get().getDefaultInstance();
									stack.enchant(serverLevel.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.BINDING_CURSE), 1);
									stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);
									player.setItemSlot(slot, stack);
								}
							}
						}

						miraculousStack.enchant(serverLevel.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.BINDING_CURSE), 1);
						miraculousStack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);

						ItemStack tool = miraculousItem.getTool() == null ? ItemStack.EMPTY : miraculousItem.getTool().getDefaultInstance();
						tool.set(MineraculousDataComponents.KWAMI_DATA.get(), new KwamiData(kwami.getUUID(), kwami.isCharged()));
						miraculousStack.set(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE);
						data = new MiraculousData(true, miraculousStack, data.curiosData(), tool, data.powerLevel(), false, false, data.name());
						player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).put(player, type, data, true);
						CuriosUtils.setStackInSlot(player, data.curiosData(), miraculousStack, true);
						TommyLibServices.NETWORK.sendToAllClients(new ClientboundMiraculousTransformPayload(type, data), serverLevel.getServer());
						int powerLevel = data.powerLevel();
						MIRACULOUS_EFFECTS.forEach(effect -> player.addEffect(INFINITE_HIDDEN_EFFECT.apply(effect, powerLevel)));
						player.addItem(tool);
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
					miraculousStack.remove(MineraculousDataComponents.KWAMI_DATA.get());
					CuriosUtils.setStackInSlot(player, data.curiosData(), miraculousStack, true);
					player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).put(player, type, new MiraculousData(true, miraculousStack, data.curiosData(), data.tool(), data.powerLevel(), false, false, data.name()), true);
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
				ArmorData armor = player.getData(MineraculousAttachmentTypes.STORED_ARMOR);
				for (EquipmentSlot slot : Arrays.stream(EquipmentSlot.values()).filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR).toArray(EquipmentSlot[]::new))
				{
					player.setItemSlot(slot, armor.forSlot(slot));
				}
				miraculousStack.remove(DataComponents.ENCHANTMENTS);
				miraculousStack.remove(MineraculousDataComponents.REMAINING_TICKS.get());
				miraculousStack.remove(MineraculousDataComponents.POWERED.get());
				CuriosUtils.setStackInSlot(player, data.curiosData(), miraculousStack, true);
				// TODO: If item not in inventory, make it disappear when found, in item entity or chest or something
				data.tool().setCount(1);
				data.tool().set(MineraculousDataComponents.RECALLED.get(), true);
				if (data.tool().has(MineraculousDataComponents.KWAMI_DATA.get()))
				{
					MiraculousData finalData = data;
					player.getInventory().clearOrCountMatchingItems(stack -> {
						if (stack.has(MineraculousDataComponents.KWAMI_DATA.get()))
							return stack.get(MineraculousDataComponents.KWAMI_DATA.get()).uuid().equals(finalData.tool().get(MineraculousDataComponents.KWAMI_DATA.get()).uuid());
						return false;
					}, 1, new SimpleContainer(data.tool()));
				}
				data = new MiraculousData(false, miraculousStack, data.curiosData(), ItemStack.EMPTY, data.powerLevel(), false, false, data.name());
				player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).put(player, type, data, true);
				TommyLibServices.NETWORK.sendToAllClients(new ClientboundMiraculousTransformPayload(type, data), serverLevel.getServer());
				MIRACULOUS_EFFECTS.forEach(player::removeEffect);
			}
		}
	}

	public static boolean renounceMiraculous(ItemStack miraculous, Kwami kwami)
	{
		KwamiData kwamiData = miraculous.get(MineraculousDataComponents.KWAMI_DATA.get());
		if (kwamiData != null && kwami.getUUID().equals(kwamiData.uuid()))
		{
			miraculous.set(MineraculousDataComponents.KWAMI_DATA.get(), new KwamiData(kwami.getUUID(), kwami.isCharged()));
			miraculous.set(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE);
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
				KwamiData kwamiData = miraculousData.miraculousItem().get(MineraculousDataComponents.KWAMI_DATA.get());
				if (kwamiData != null)
				{
					kwami.setUUID(kwamiData.uuid());
					kwami.setCharged(kwamiData.charged());
				}
				else
				{
					kwami.setCharged(true);
				}
				kwami.setPos(player.getX() + level.random.nextInt(3), player.getY() + 2, player.getZ() + +level.random.nextInt(3));
				kwami.tame(player);
				level.addFreshEntity(kwami);

				miraculousData.miraculousItem().set(MineraculousDataComponents.KWAMI_DATA.get(), new KwamiData(kwami.getUUID(), kwami.isCharged()));
				CuriosUtils.setStackInSlot(player, miraculousData.curiosData(), miraculousData.miraculousItem(), true);
				player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).put(player, type, new MiraculousData(false, miraculousData.miraculousItem(), miraculousData.curiosData(), miraculousData.tool(), miraculousData.powerLevel(), false, false, miraculousData.name()), true);
			}
			return kwami;
		}
		return null;
	}

	public static InteractionResult testAndApplyCataclysmEffects(LivingEntity entity, Entity target, InteractionHand hand)
	{
		MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS);
		MiraculousData catMiraculousData = miraculousDataSet.get(MiraculousType.CAT);
		if (!entity.level().isClientSide && hand == InteractionHand.MAIN_HAND && catMiraculousData.transformed() && catMiraculousData.mainPowerActive())
		{

			CompoundTag persistentData = TommyLibServices.ENTITY.getPersistentData(target);
			persistentData.putBoolean(TAG_CATACLYSMED, true);
			TommyLibServices.ENTITY.setPersistentData(target, persistentData, true);
			miraculousDataSet.put(entity, MiraculousType.CAT, new MiraculousData(true, catMiraculousData.miraculousItem(), catMiraculousData.curiosData(), catMiraculousData.tool(), catMiraculousData.powerLevel(), true, false, catMiraculousData.name()), true);
			int level = catMiraculousData.powerLevel();
			if (target instanceof LivingEntity livingEntity)
			{
				List<Holder<MobEffect>> CATACLYSM_EFFECTS = List.of(
						MobEffects.POISON,
						MobEffects.WITHER,
						MobEffects.BLINDNESS,
						MobEffects.WEAKNESS,
						MobEffects.MOVEMENT_SLOWDOWN,
						MobEffects.HUNGER,
						MobEffects.CONFUSION,
						MobEffects.DIG_SLOWDOWN
				);
				CATACLYSM_EFFECTS.forEach(effect -> {
					MobEffectInstance instance = INFINITE_HIDDEN_EFFECT.apply(effect, level);
					if (CommonHooks.canMobEffectBeApplied(livingEntity, instance))
						livingEntity.addEffect(instance);
					else
						livingEntity.hurt(entity.damageSources().indirectMagic(entity, entity), 100);
				});
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
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event)
	{
		event.setCancellationResult(testAndApplyCataclysmEffects(event.getEntity(), event.getTarget(), event.getHand()));
	}

	public static void onAttackEntity(AttackEntityEvent event)
	{
		if (!(event.getTarget() instanceof LivingEntity))
			testAndApplyCataclysmEffects(event.getEntity(), event.getTarget(), InteractionHand.MAIN_HAND);
	}

	public static void onLivingAttack(LivingAttackEvent event)
	{
		if (event.getSource().getDirectEntity() instanceof LivingEntity livingEntity)
		{
			testAndApplyCataclysmEffects(livingEntity, event.getEntity(), InteractionHand.MAIN_HAND);
		}
	}

	public static InteractionResult testAndApplyCataclysmToBlocks(LivingEntity entity, BlockPos pos, InteractionHand hand, @Nullable Direction nextPosDirection, int blocksAffected)
	{
		Level level = entity.level();
		MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS);
		MiraculousData catMiraculousData = miraculousDataSet.get(MiraculousType.CAT);
		if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && catMiraculousData.transformed() && catMiraculousData.mainPowerActive())
		{
			BlockState state = level.getBlockState(pos);
			if (state.is(MineraculousBlockTags.CATACLYSM_IMMUNE) || blocksAffected >= Math.max(entity.getData(MineraculousAttachmentTypes.MIRACULOUS).get(MiraculousType.CAT).powerLevel(), 1) * 100)
				return InteractionResult.PASS;
			blocksAffected++;

			int range = 3;
			for (int i = -range; i <= range; i++)
			{
				for (int j = -range; j <= range; j++)
				{
					BlockPos newPos = pos.offset(i, 0, j);
					BlockState newState = level.getBlockState(newPos);
					if (newState.is(BlockTags.LOGS) || newState.is(BlockTags.LEAVES) || newState.getBlock() instanceof MangroveRootsBlock)
					{
						level.setBlock(newPos, MineraculousBlocks.CATACLYSM_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
					}
				}
			}

			if (nextPosDirection == null)
			{
				nextPosDirection = switch (level.random.nextInt(5))
				{
					case 0 -> Direction.NORTH;
					case 1 -> Direction.EAST;
					case 2 -> Direction.SOUTH;
					case 3 -> Direction.WEST;
					default -> Direction.UP;
				};
			}

			if (!level.getBlockState(pos.relative(nextPosDirection)).canBeReplaced())
			{
				testAndApplyCataclysmToBlocks(entity, pos.relative(nextPosDirection), hand, nextPosDirection, blocksAffected);
			}

			RandomSource randomSource = level.random;
			destroyBlocksWithin(randomSource.nextInt(4, 8), level, pos);

			miraculousDataSet.put(entity, MiraculousType.CAT, new MiraculousData(true, catMiraculousData.miraculousItem(), catMiraculousData.curiosData(), catMiraculousData.tool(), catMiraculousData.powerLevel(), true, false, catMiraculousData.name()), true);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	private static void destroyBlocksWithin(int radius, Level level, BlockPos pos)
	{
		int iRange = level.random.nextInt(radius);
		for (int i = -iRange; i <= iRange; i++)
		{
			int jRange = level.random.nextInt(radius);
			for (int j = -jRange; j <= jRange; j++)
			{
				int kRange = level.random.nextInt(radius);
				for (int k = -kRange; k <= kRange; k++)
				{
					BlockPos newPos = pos.offset(i, j, k);
					BlockState newState = level.getBlockState(newPos);
					if (!newState.is(MineraculousBlockTags.CATACLYSM_IMMUNE) && level.random.nextBoolean())
					{
						level.setBlock(newPos, MineraculousBlocks.CATACLYSM_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
					}
				}
			}
		}
	}

	public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event)
	{
		event.setCancellationResult(testAndApplyCataclysmToBlocks(event.getEntity(), event.getHitVec().getBlockPos(), event.getHand(), null, 0));
	}

	public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event)
	{
		InteractionResult result = testAndApplyCataclysmToBlocks(event.getEntity(), event.getPos(), event.getHand(), null, 0);
		if (result.consumesAction())
			event.setUseBlock(TriState.TRUE);
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
		return TommyLibServices.ENTITY.getPersistentData(entity).getBoolean(TAG_CATACLYSMED);
	}

	public static Component formatDisplayName(LivingEntity entity, Component original)
	{
		if (original != null)
		{
			Style style = original.getStyle();
			MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS);
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

	public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
		for (EntityType<? extends LivingEntity> type : MineraculousEntityTypes.getAllAttributes().keySet())
		{
			event.put(type, MineraculousEntityTypes.getAllAttributes().get(type));
		}
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
}
