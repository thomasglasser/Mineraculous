package dev.thomasglasser.mineraculous.world.entity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.advancements.critereon.KamikotizationUsePowerTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.MiraculousUsePowerTrigger;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.network.ClientboundRefreshVipDataPayload;
import dev.thomasglasser.mineraculous.network.ClientboundRequestSyncKamikotizationLookPayload;
import dev.thomasglasser.mineraculous.network.ClientboundRequestSyncMiraculousLookPayload;
import dev.thomasglasser.mineraculous.network.ClientboundRequestSyncSuitLookPayload;
import dev.thomasglasser.mineraculous.network.ClientboundSyncKamikotizationLookPayload;
import dev.thomasglasser.mineraculous.network.ClientboundSyncMiraculousLookPayload;
import dev.thomasglasser.mineraculous.network.ClientboundSyncSuitLookPayload;
import dev.thomasglasser.mineraculous.network.ServerboundPutKamikotizationToolInHandPayload;
import dev.thomasglasser.mineraculous.network.ServerboundRequestInventorySyncPayload;
import dev.thomasglasser.mineraculous.network.ServerboundRequestMiraculousDataSetSyncPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSendEmptyLeftClickPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetKamikotizationPowerActivatedPayload;
import dev.thomasglasser.mineraculous.network.ServerboundTryBreakItemPayload;
import dev.thomasglasser.mineraculous.network.ServerboundWakeUpPayload;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.ability.ApplyEffectsWhileTransformedAbility;
import dev.thomasglasser.mineraculous.world.entity.ability.NightVisionAbility;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.npc.MineraculousVillagerTrades;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.ChargeOverrideDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedLookDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.LuckyCharmIdDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousRecoveryDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousRecoveryEntityData;
import dev.thomasglasser.mineraculous.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.mineraculous.world.level.storage.ToolIdDataHolder;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

public class MineraculousEntityEvents {
    public static final String TAG_WAIT_TICKS = "WaitTicks";
    public static final String TAG_HAS_NIGHT_VISION = "HasNightVision";
    public static final String TAG_TAKE_TICKS = "TakeTicks";
    public static final String TAG_CATACLYSMED = "Cataclysmed";
    public static final String TAG_SHOW_KAMIKO_MASK = "ShowKamikoMask";
    public static final String TAG_CAMERA_CONTROL_INTERRUPTED = "CameraControlInterrupted";
    public static final String TAG_YOYO_BOUND_POS = "YoyoBoundPos";

    public static final String ITEM_BROKEN_KEY = "mineraculous.item_broken";

    public static final BiFunction<Holder<MobEffect>, Integer, MobEffectInstance> INFINITE_HIDDEN_EFFECT = (effect, amplifier) -> new MobEffectInstance(effect, -1, amplifier, false, false, false);

    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(entity);
        int waitTicks = entityData.getInt(MineraculousEntityEvents.TAG_WAIT_TICKS);
        if (waitTicks > 0) {
            entityData.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, --waitTicks);
        }
        if (entity.level() instanceof ServerLevel serverLevel) {
            if (entityData.contains(TAG_YOYO_BOUND_POS)) {
                entity.resetFallDistance();
                CompoundTag pos = entityData.getCompound(TAG_YOYO_BOUND_POS);
                entity.teleportTo(pos.getDouble("X"), pos.getDouble("Y"), pos.getDouble("Z"));
            }
            MiraculousRecoveryEntityData miraculousRecoveryEntityData = ((MiraculousRecoveryDataHolder) serverLevel.getServer().overworld()).mineraculous$getMiraculousRecoveryEntityData();
            if (miraculousRecoveryEntityData.isBeingTracked(entity.getUUID())) {
                List<UUID> alreadyRelated = miraculousRecoveryEntityData.getRelatedEntities(entity.getUUID());
                List<LivingEntity> related = entity.level().getEntities(EntityTypeTest.forClass(LivingEntity.class), entity.getBoundingBox().inflate(16), livingEntity -> !alreadyRelated.contains(livingEntity.getUUID()) && (livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS).isTransformed() || livingEntity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()));
                for (LivingEntity livingEntity : related) {
                    if (livingEntity.getUUID() != entity.getUUID()) {
                        miraculousRecoveryEntityData.putRelatedEntity(entity.getUUID(), livingEntity.getUUID());
                    }
                }
            }
            if (entity instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getItem();
                ItemStack recovered = ((MiraculousRecoveryDataHolder) serverLevel.getServer().overworld()).mineraculous$getMiraculousRecoveryItemData().checkRecovered(stack);
                if (recovered != null) {
                    itemEntity.setItem(recovered);
                    stack.setCount(0);
                }
            }
        }
        TommyLibServices.ENTITY.setPersistentData(entity, entityData, false);
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(player);

        Level level = player.level();
        if (level.isClientSide && entityData.getInt(TAG_WAIT_TICKS) == 0 && ClientUtils.getMainClientPlayer() == player) {
            int takeTicks = entityData.getInt(MineraculousEntityEvents.TAG_TAKE_TICKS);
            if (MineraculousKeyMappings.TAKE_BREAK_ITEM.get().isDown()) {
                ItemStack mainHandItem = player.getMainHandItem();
                if (mainHandItem.isEmpty()) {
                    if (MineraculousClientUtils.getLookEntity() instanceof Player target && (MineraculousServerConfig.get().enableUniversalStealing.get() || player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).isTransformed()) && (MineraculousServerConfig.get().enableSleepStealing.get() || !target.isSleeping())) {
                        entityData.putInt(MineraculousEntityEvents.TAG_TAKE_TICKS, ++takeTicks);
                        if (target.isSleeping() && MineraculousServerConfig.get().wakeUpChance.get() > 0 && (MineraculousServerConfig.get().wakeUpChance.get() >= 100 || player.getRandom().nextFloat() < MineraculousServerConfig.get().wakeUpChance.get() / (20f * 5 * 100))) {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundWakeUpPayload(target.getUUID(), true));
                        }
                        if (takeTicks > (20 * MineraculousServerConfig.get().stealingDuration.get())) {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundRequestInventorySyncPayload(target.getUUID()));
                            MineraculousClientUtils.openExternalCuriosInventoryScreen(target, player);
                            entityData.putInt(MineraculousEntityEvents.TAG_TAKE_TICKS, 0);
                        }
                        TommyLibServices.ENTITY.setPersistentData(player, entityData, false);
                    }
                } else {
                    TommyLibServices.NETWORK.sendToServer(ServerboundTryBreakItemPayload.INSTANCE);
                    entityData.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, 10);
                    TommyLibServices.ENTITY.setPersistentData(player, entityData, false);
                }
            } else if (takeTicks > 0) {
                entityData.putInt(MineraculousEntityEvents.TAG_TAKE_TICKS, 0);
                TommyLibServices.ENTITY.setPersistentData(player, entityData, false);
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            player.getInventory().clearOrCountMatchingItems(itemStack -> {
                ItemStack recovered = ((MiraculousRecoveryDataHolder) serverPlayer.serverLevel().getServer().overworld()).mineraculous$getMiraculousRecoveryItemData().checkRecovered(itemStack);
                if (recovered != null) {
                    if (player.getInventory().contains(itemStack))
                        player.getInventory().setItem(player.getInventory().findSlotMatchingItem(itemStack), recovered);
                    else
                        player.addItem(recovered);
                    itemStack.setCount(0);
                }
                if (itemStack.has(MineraculousDataComponents.KWAMI_DATA)) {
                    if (itemStack.has(MineraculousDataComponents.TOOL_ID)) {
                        int currentId = ((ToolIdDataHolder) serverPlayer.serverLevel().getServer().overworld()).mineraculous$getToolIdData().getToolId(itemStack.get(MineraculousDataComponents.KWAMI_DATA));
                        Integer stackId = itemStack.get(MineraculousDataComponents.TOOL_ID);
                        return stackId != currentId;
                    }
                    if (itemStack.has(MineraculousDataComponents.LUCKY_CHARM)) {
                        int currentId = ((LuckyCharmIdDataHolder) serverPlayer.serverLevel().getServer().overworld()).mineraculous$getLuckyCharmIdData().getLuckyCharmId(itemStack.get(MineraculousDataComponents.KWAMI_DATA).uuid());
                        Integer stackId = itemStack.get(MineraculousDataComponents.LUCKY_CHARM).id();
                        return stackId != currentId;
                    }
                } else if (itemStack.has(MineraculousDataComponents.KAMIKOTIZATION)) {
                    ResolvableProfile resolvableProfile = itemStack.get(DataComponents.PROFILE);
                    if (resolvableProfile != null) {
                        Player target = serverPlayer.level().getPlayerByUUID(resolvableProfile.id().orElse(resolvableProfile.gameProfile().getId()));
                        return target == null || target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isEmpty();
                    }
                    if (itemStack.has(MineraculousDataComponents.LUCKY_CHARM)) {
                        int currentId = ((LuckyCharmIdDataHolder) serverPlayer.serverLevel().getServer().overworld()).mineraculous$getLuckyCharmIdData().getLuckyCharmId(serverPlayer.getUUID());
                        Integer stackId = itemStack.get(MineraculousDataComponents.LUCKY_CHARM).id();
                        return stackId != currentId;
                    }
                }
                return false;
            }, Integer.MAX_VALUE, new SimpleContainer());
            CuriosUtils.getAllItems(player).forEach(((curiosData, itemStack) -> {
                ItemStack recovered = ((MiraculousRecoveryDataHolder) serverPlayer.serverLevel().getServer().overworld()).mineraculous$getMiraculousRecoveryItemData().checkRecovered(itemStack);
                if (recovered != null) {
                    player.addItem(recovered);
                    itemStack.setCount(0);
                }
                if (itemStack.has(MineraculousDataComponents.KWAMI_DATA)) {
                    if (itemStack.has(MineraculousDataComponents.TOOL_ID)) {
                        int currentId = ((ToolIdDataHolder) serverPlayer.serverLevel().getServer().overworld()).mineraculous$getToolIdData().getToolId(itemStack.get(MineraculousDataComponents.KWAMI_DATA));
                        Integer stackId = itemStack.get(MineraculousDataComponents.TOOL_ID);
                        if (stackId != null && stackId != currentId)
                            itemStack.shrink(1);
                    }
                    if (itemStack.has(MineraculousDataComponents.LUCKY_CHARM)) {
                        int currentId = ((LuckyCharmIdDataHolder) serverPlayer.serverLevel().getServer().overworld()).mineraculous$getLuckyCharmIdData().getLuckyCharmId(itemStack.get(MineraculousDataComponents.KWAMI_DATA).uuid());
                        Integer stackId = itemStack.get(MineraculousDataComponents.LUCKY_CHARM).id();
                        if (stackId != currentId)
                            itemStack.shrink(1);
                    }
                } else if (itemStack.has(MineraculousDataComponents.KAMIKOTIZATION)) {
                    ResolvableProfile resolvableProfile = itemStack.get(DataComponents.PROFILE);
                    if (resolvableProfile != null) {
                        Player target = serverPlayer.level().getPlayerByUUID(resolvableProfile.id().orElse(resolvableProfile.gameProfile().getId()));
                        if (target == null || target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isEmpty())
                            itemStack.shrink(1);
                    }
                    if (itemStack.has(MineraculousDataComponents.LUCKY_CHARM)) {
                        int currentId = ((LuckyCharmIdDataHolder) serverPlayer.serverLevel().getServer().overworld()).mineraculous$getLuckyCharmIdData().getLuckyCharmId(serverPlayer.getUUID());
                        Integer stackId = itemStack.get(MineraculousDataComponents.LUCKY_CHARM).id();
                        if (stackId != currentId)
                            itemStack.shrink(1);
                    }
                }
            }));

            if (serverPlayer.tickCount == 15) {
                FlattenedLookDataHolder flattenedLookDataHolder = (FlattenedLookDataHolder) player.getServer().overworld();
                flattenedLookDataHolder.mineraculous$getSuitLookData().forEach((uuid, dataSet) -> {
                    dataSet.forEach((key, data) -> TommyLibServices.NETWORK.sendToClient(new ClientboundSyncSuitLookPayload(uuid, key, data, false), serverPlayer));
                });
                flattenedLookDataHolder.mineraculous$getMiraculousLookData().forEach((uuid, dataSet) -> {
                    dataSet.forEach((key, data) -> TommyLibServices.NETWORK.sendToClient(new ClientboundSyncMiraculousLookPayload(uuid, key, data, false), serverPlayer));
                });
                flattenedLookDataHolder.mineraculous$getKamikotizationLookData().forEach((uuid, data) -> {
                    TommyLibServices.NETWORK.sendToClient(new ClientboundSyncKamikotizationLookPayload(uuid, data), serverPlayer);
                });
                for (ServerPlayer other : serverPlayer.serverLevel().players()) {
                    MiraculousDataSet miraculousDataSet = other.getData(MineraculousAttachmentTypes.MIRACULOUS);
                    for (ResourceKey<Miraculous> miraculous : miraculousDataSet.keySet()) {
                        Map<String, FlattenedSuitLookData> commonSuitLooks = ((FlattenedLookDataHolder) serverPlayer.serverLevel().getServer().overworld()).mineraculous$getCommonSuitLookData().get(miraculous);
                        String look = miraculousDataSet.get(miraculous).suitLook();
                        if (commonSuitLooks.containsKey(look)) {
                            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncSuitLookPayload(other.getUUID(), miraculous, commonSuitLooks.get(look), true), serverPlayer.getServer());
                        }
                        Map<String, FlattenedMiraculousLookData> commonMiraculousLooks = ((FlattenedLookDataHolder) serverPlayer.serverLevel().getServer().overworld()).mineraculous$getCommonMiraculousLookData().get(miraculous);
                        look = miraculousDataSet.get(miraculous).miraculousLook();
                        if (commonMiraculousLooks.containsKey(look)) {
                            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncMiraculousLookPayload(other.getUUID(), miraculous, commonMiraculousLooks.get(look), true), serverPlayer.getServer());
                        }
                    }
                }
            }
        }

        Optional<KamikotizationData> optionalData = player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION);
        if (optionalData.isPresent()) {
            KamikotizationData kamikotizationData = optionalData.get();
            ResourceKey<Kamikotization> kamikotizationKey = kamikotizationData.kamikotization();
            Kamikotization kamikotization = level.holderOrThrow(kamikotizationKey).value();
            Optional<Integer> transformationFrames = kamikotizationData.transformationFrames().isPresent() ? kamikotizationData.transformationFrames().get().left() : Optional.empty();
            Optional<Integer> detransformationFrames = kamikotizationData.transformationFrames().isPresent() ? kamikotizationData.transformationFrames().get().right() : Optional.empty();
            if (player instanceof ServerPlayer serverPlayer) {
                if (transformationFrames.isPresent() && transformationFrames.get() >= 1) {
                    if (player.tickCount % 2 == 0) {
                        if (transformationFrames.get() <= 1) {
                            kamikotizationData.clearTransformationFrames().save(player, true);
                            ArmorData armor = new ArmorData(player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST), player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET));
                            player.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.of(armor));
                            for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                                ItemStack stack = Kamikotization.createItemStack(MineraculousArmors.KAMIKOTIZATION.getForSlot(slot).get(), kamikotizationKey);
                                stack.enchant(level.holderOrThrow(Enchantments.BINDING_CURSE), 1);
                                stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);
                                player.setItemSlot(slot, stack);
                            }
                            player.refreshDisplayName();
                        } else {
                            kamikotizationData.withTransformationFrames(transformationFrames.get() - 1).save(player, true);
                        }
                    }
                    serverPlayer.serverLevel().sendParticles(MineraculousParticleTypes.KAMIKOTIZATION.get(), player.getX(), player.getY() + 2 - (11 - transformationFrames.get()) / 5.0, player.getZ(), 100, Math.random() / 3.0, Math.random() / 3.0, Math.random() / 3.0, 0);
                } else if (detransformationFrames.isPresent() && detransformationFrames.get() >= 1) {
                    if (player.tickCount % 2 == 0) {
                        if (detransformationFrames.get() <= 1) {
                            kamikotizationData.withDetransformationFrames(-1);
                            KamikotizationData.remove(player, true);
                            player.getData(MineraculousAttachmentTypes.STORED_ARMOR).ifPresent(data -> {
                                for (EquipmentSlot slot : Arrays.stream(EquipmentSlot.values()).filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR).toArray(EquipmentSlot[]::new)) {
                                    player.setItemSlot(slot, data.forSlot(slot));
                                }
                            });
                            player.refreshDisplayName();
                        } else {
                            kamikotizationData.withDetransformationFrames(detransformationFrames.get() - 1).save(player, true);
                        }
                    }
                    serverPlayer.serverLevel().sendParticles(MineraculousParticleTypes.KAMIKOTIZATION.get(), player.getX(), player.getY() + 2 - (11 - detransformationFrames.get()) / 5.0, player.getZ(), 100, Math.random() / 3.0, Math.random() / 3.0, Math.random() / 3.0, 0);
                }
            } else if (ClientUtils.getMainClientPlayer() == player) {
                CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(player);
                int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAIT_TICKS);
                if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen() && !MineraculousClientUtils.isCameraEntityOther()) {
                    if (MineraculousKeyMappings.ACTIVATE_POWER.get().isDown() && !kamikotizationData.mainPowerActive() && level.holderOrThrow(kamikotizationKey).value().powerSource().right().isPresent()) {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundSetKamikotizationPowerActivatedPayload(kamikotizationKey));
                        playerData.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, 10);
                    } else if (MineraculousKeyMappings.OPEN_TOOL_WHEEL.get().isDown() && player.getMainHandItem().isEmpty()) {
                        TommyLibServices.NETWORK.sendToServer(ServerboundPutKamikotizationToolInHandPayload.INSTANCE);
                        playerData.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, 10);
                    }
                }
                TommyLibServices.ENTITY.setPersistentData(player, playerData, false);
            }
            if (level instanceof ServerLevel serverLevel && transformationFrames.isEmpty() && detransformationFrames.isEmpty()) {
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                AbilityData abilityData = new AbilityData(0, Either.right(kamikotizationKey));
                kamikotization.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                    if (ability.canActivate(abilityData, serverLevel, player.blockPosition(), player) && ability.perform(abilityData, serverLevel, player.blockPosition(), player, Ability.Context.PASSIVE) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (!player.getMainHandItem().isEmpty()) {
                    kamikotization.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                        if (ability.canActivate(abilityData, serverLevel, player.blockPosition(), player) && ability.perform(abilityData, serverLevel, player.blockPosition(), player, Ability.Context.from(player.getMainHandItem())) && ability.overrideActive())
                            overrideActive.set(true);
                    });
                    if (kamikotizationData.mainPowerActive()) {
                        if (!overrideActive.get()) {
                            boolean usedPower = kamikotization.powerSource().right().get().value().perform(abilityData, serverLevel, player.blockPosition(), player, Ability.Context.from(player.getMainHandItem()));
                            if (usedPower) {
                                if (player instanceof ServerPlayer serverPlayer) {
                                    kamikotizationData.withMainPowerActive(false).save(event.getEntity(), true);
                                    MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(serverPlayer, kamikotizationKey, KamikotizationUsePowerTrigger.Context.ITEM);
                                }
                            }
                        } else
                            kamikotizationData.withMainPowerActive(false).save(event.getEntity(), true);
                    }
                }
                if (kamikotizationData.mainPowerActive()) {
                    if (overrideActive.get()) {
                        kamikotizationData.withMainPowerActive(false).save(player, true);
                    } else {
                        boolean usedPower = kamikotization.powerSource().right().get().value().perform(abilityData, serverLevel, player.blockPosition(), player, Ability.Context.PASSIVE);
                        if (usedPower) {
                            kamikotizationData.withMainPowerActive(false).save(player, true);
                            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                                MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(serverPlayer, kamikotizationKey, KamikotizationUsePowerTrigger.Context.EMPTY);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void onServerPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        for (ServerPlayer serverPlayer : ((ServerLevel) player.level()).getPlayers(serverPlayer -> true)) {
            TommyLibServices.NETWORK.sendToAllClients(ClientboundRefreshVipDataPayload.INSTANCE, serverPlayer.getServer());
        }
    }

    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        Level level = event.getLevel();
        Entity entity = event.getEntity();
        if (!level.isClientSide) {
            entity.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO).id().ifPresent(id -> {
                Entity yoyo = event.getLevel().getEntity(id);
                if (yoyo != null)
                    yoyo.discard();
                ThrownLadybugYoyoData.remove(entity, true);
            });
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS.get());
        if (entity instanceof ServerPlayer player) {
            miraculousDataSet.keySet().forEach(miraculous -> {
                MiraculousData data = miraculousDataSet.get(miraculous);
                if (data.transformed())
                    handleMiraculousTransformation(player, miraculous, data, false, true, false);
                renounceMiraculous(data.miraculousItem(), player.serverLevel());
            });
            player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> handleKamikotizationTransformation(player, data, false, true, player.position().add(0, 1, 0)));
        }
        if (entity.hasEffect(MineraculousMobEffects.CATACLYSMED) && TommyLibServices.ENTITY.getPersistentData(entity).hasUUID(TAG_CATACLYSMED)) {
            entity.setLastHurtByPlayer(entity.level().getPlayerByUUID(TommyLibServices.ENTITY.getPersistentData(entity).getUUID(TAG_CATACLYSMED)));
        }
    }

    public static void handleMiraculousTransformation(ServerPlayer player, ResourceKey<Miraculous> miraculous, MiraculousData data, boolean transform, boolean instant, boolean removed) {
        if (player != null) {
            player.removeEntitiesOnShoulder();
            ServerLevel serverLevel = player.serverLevel();
            ItemStack miraculousStack = data.miraculousItem();
            int transformationFrames = instant ? 0 : player.serverLevel().holderOrThrow(miraculous).value().transformationFrames();
            if (transform) {
                // Transform
                if (player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || player.getData(MineraculousAttachmentTypes.MIRACULOUS).isTransformed()) {
                    return;
                }
                KwamiData kwamiData = miraculousStack.get(MineraculousDataComponents.KWAMI_DATA.get());
                Entity entity = serverLevel.getEntity(kwamiData.uuid());
                if (entity instanceof Kwami kwami) {
                    if (kwami.isCharged()) {
                        ArmorData armor = new ArmorData(player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST), player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET));
                        player.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.of(armor));
                        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                            ItemStack stack = Miraculous.createItemStack(MineraculousArmors.MIRACULOUS.getForSlot(slot).get(), miraculous);
                            stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);
                            if (transformationFrames > 0)
                                stack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, transformationFrames);
                            player.setItemSlot(slot, stack);
                        }

                        int newToolId = ((ToolIdDataHolder) serverLevel.getServer().overworld()).mineraculous$getToolIdData().incrementToolId(kwamiData);
                        data = data.transform(true, miraculousStack, newToolId);

                        miraculousStack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);
                        miraculousStack.set(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE);
                        if (transformationFrames > 0)
                            miraculousStack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, transformationFrames);
                        else {
                            ItemStack tool = data.createTool(player);
                            if (!tool.isEmpty()) {
                                if (serverLevel.holderOrThrow(miraculous).value().toolSlot().isPresent()) {
                                    boolean added = CuriosUtils.setStackInFirstValidSlot(player, serverLevel.holderOrThrow(miraculous).value().toolSlot().get(), tool);
                                    if (!added) {
                                        player.addItem(tool);
                                    }
                                } else {
                                    player.addItem(tool);
                                }
                            }
                        }

                        player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).put(player, miraculous, data, true);
                        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), serverLevel.holderOrThrow(miraculous).value().transformSound(), SoundSource.PLAYERS, 1, 1);
                        if (data.curiosData() != CuriosData.EMPTY)
                            CuriosUtils.setStackInSlot(player, data.curiosData(), miraculousStack);
                        if (data.name().isEmpty())
                            player.displayClientMessage(Component.translatable(MiraculousData.NAME_NOT_SET, Component.translatable(Miraculous.toLanguageKey(miraculous)), miraculous.location().getPath()), true);
                        int powerLevel = data.powerLevel();
                        serverLevel.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((effect, startLevel) -> player.addEffect(INFINITE_HIDDEN_EFFECT.apply(serverLevel.holderOrThrow(effect), startLevel + (powerLevel / 10))));
                        player.getAttributes().addTransientAttributeModifiers(getMiraculousAttributes(serverLevel, powerLevel));
                        kwami.discard();
                        MiraculousData finalData = data;
                        serverLevel.holderOrThrow(miraculous).value().activeAbility().ifPresent(ability -> ability.value().transform(new AbilityData(finalData.powerLevel(), Either.left(miraculous)), serverLevel, player.blockPosition(), player));
                        serverLevel.holderOrThrow(miraculous).value().passiveAbilities().forEach(ability -> ability.value().transform(new AbilityData(finalData.powerLevel(), Either.left(miraculous)), serverLevel, player.blockPosition(), player));
                    } else {
                        kwami.playHurtSound(serverLevel.damageSources().starve());
                    }
                } else {
                    miraculousStack.remove(MineraculousDataComponents.KWAMI_DATA.get());
                    CuriosUtils.setStackInSlot(player, data.curiosData(), miraculousStack);
                }
            } else {
                // De-transform
                Kwami kwami = summonKwami(serverLevel, miraculous, data, player);
                if (kwami != null) {
                    kwami.setCharged(false);
                } else {
                    Mineraculous.LOGGER.error("Kwami could not be created for player " + player.getName().plainCopy().getString());
                    return;
                }

                if (transformationFrames > 0)
                    miraculousStack.set(MineraculousDataComponents.DETRANSFORMATION_FRAMES, transformationFrames);
                else {
                    miraculousStack.remove(DataComponents.ENCHANTMENTS);
                    player.getData(MineraculousAttachmentTypes.STORED_ARMOR).ifPresent(armorData -> {
                        for (EquipmentSlot slot : Arrays.stream(EquipmentSlot.values()).filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR).toArray(EquipmentSlot[]::new)) {
                            player.setItemSlot(slot, armorData.forSlot(slot));
                        }
                    });
                }
                miraculousStack.remove(MineraculousDataComponents.REMAINING_TICKS.get());
                miraculousStack.remove(MineraculousDataComponents.POWERED.get());
                if (data.curiosData() != CuriosData.EMPTY)
                    CuriosUtils.setStackInSlot(player, data.curiosData(), miraculousStack);
                if (removed) {
                    renounceMiraculous(miraculousStack, serverLevel);
                    ((ChargeOverrideDataHolder) serverLevel.getServer().overworld()).mineraculous$getChargeOverrideData().put(kwami.getUUID(), false);
                }
                data = data.transform(false, miraculousStack, ((ToolIdDataHolder) serverLevel.getServer().overworld()).mineraculous$getToolIdData().getToolId(kwami.getUUID()));
                if (removed)
                    data = data.unEquip();
                player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).put(player, miraculous, data, true);
                player.getInventory().clearOrCountMatchingItems(itemStack -> itemStack.has(MineraculousDataComponents.TOOL_ID) && itemStack.has(MineraculousDataComponents.KWAMI_DATA) && itemStack.get(MineraculousDataComponents.KWAMI_DATA).uuid().equals(kwami.getUUID()), Integer.MAX_VALUE, new SimpleContainer());
                CuriosUtils.getAllItems(player).forEach(((curiosData, itemStack) -> {
                    if (itemStack.has(MineraculousDataComponents.TOOL_ID) && itemStack.has(MineraculousDataComponents.KWAMI_DATA) && itemStack.get(MineraculousDataComponents.KWAMI_DATA).uuid().equals(kwami.getUUID())) {
                        itemStack.shrink(1);
                    }
                }));
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), serverLevel.holderOrThrow(miraculous).value().detransformSound(), SoundSource.PLAYERS, 1, 1);
                serverLevel.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).keySet().stream().map(serverLevel::holderOrThrow).forEach(player::removeEffect);
                player.getAttributes().removeAttributeModifiers(getMiraculousAttributes(serverLevel, data.powerLevel()));
                MiraculousData finalData = data;
                serverLevel.holderOrThrow(miraculous).value().activeAbility().ifPresent(ability -> ability.value().detransform(new AbilityData(finalData.powerLevel(), Either.left(miraculous)), serverLevel, player.blockPosition(), player));
                serverLevel.holderOrThrow(miraculous).value().passiveAbilities().forEach(ability -> ability.value().detransform(new AbilityData(finalData.powerLevel(), Either.left(miraculous)), serverLevel, player.blockPosition(), player));
            }
            player.refreshDisplayName();
        }
    }

    private static Multimap<Holder<Attribute>, AttributeModifier> getMiraculousAttributes(ServerLevel serverLevel, int powerLevel) {
        Multimap<Holder<Attribute>, AttributeModifier> attributeModifiers = HashMultimap.create();
        Registry<Attribute> attributes = serverLevel.registryAccess().registryOrThrow(Registries.ATTRIBUTE);
        attributes.getDataMap(MineraculousDataMaps.MIRACULOUS_ATTRIBUTES).forEach((attribute, settings) -> attributeModifiers.put(attributes.getHolderOrThrow(attribute), new AttributeModifier(Mineraculous.modLoc("miraculous_buff"), (settings.amount() * (powerLevel / 10.0)), settings.operation())));
        return attributeModifiers;
    }

    public static void renounceMiraculous(ItemStack miraculous, ServerLevel serverLevel) {
        miraculous.set(MineraculousDataComponents.POWERED, Unit.INSTANCE);
        miraculous.remove(MineraculousDataComponents.REMAINING_TICKS);
        KwamiData kwamiData = miraculous.get(MineraculousDataComponents.KWAMI_DATA.get());
        if (kwamiData != null) {
            if (serverLevel.getEntity(kwamiData.uuid()) instanceof Kwami kwami) {
                miraculous.set(MineraculousDataComponents.KWAMI_DATA.get(), new KwamiData(kwami.getUUID(), kwami.isCharged()));
                kwami.discard();
            }
        }
    }

    public static Kwami summonKwami(ServerLevel level, ResourceKey<Miraculous> miraculous, MiraculousData miraculousData, Player player) {
        if (miraculousData.miraculousItem().has(MineraculousDataComponents.MIRACULOUS)) {
            Kwami kwami = MineraculousEntityTypes.KWAMI.get().create(level);
            if (kwami != null) {
                kwami.setMiraculous(miraculous);
                KwamiData kwamiData = miraculousData.miraculousItem().get(MineraculousDataComponents.KWAMI_DATA.get());
                if (kwamiData != null) {
                    kwami.setUUID(kwamiData.uuid());
                    kwami.setCharged(kwamiData.charged());
                } else {
                    kwami.setCharged(true);
                }
                Direction direction = player.getDirection().getOpposite();
                int xOffset = switch (direction) {
                    case WEST -> 1;
                    case EAST -> -1;
                    default -> 0;
                };
                int zOffset = switch (direction) {
                    case NORTH -> 1;
                    case SOUTH -> -1;
                    default -> 0;
                };
                kwami.teleportTo(level, player.getX() + xOffset, player.getY() + 1, player.getZ() + zOffset, Set.of(), direction.toYRot(), 0.0F);
                kwami.tame(player);
                level.addFreshEntity(kwami);
                kwami.playSound(MineraculousSoundEvents.KWAMI_SUMMON.get());

                miraculousData.miraculousItem().set(MineraculousDataComponents.KWAMI_DATA.get(), new KwamiData(kwami.getUUID(), kwami.isCharged()));
                if (miraculousData.curiosData() != CuriosData.EMPTY)
                    CuriosUtils.setStackInSlot(player, miraculousData.curiosData(), miraculousData.miraculousItem());
                player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).put(player, miraculous, miraculousData.withItem(miraculousData.miraculousItem()), true);
                return kwami;
            }
        }
        return null;
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.level() instanceof ServerLevel level) {
            Entity target = event.getTarget();
            boolean blocked = target instanceof LivingEntity livingEntity && livingEntity.isBlocking();
            LivingEntity livingTarget;
            if (target instanceof LivingEntity livingEntity)
                livingTarget = livingEntity;
            else {
                livingTarget = null;
            }
            player.getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
                Miraculous miraculous = level.holderOrThrow(key).value();
                MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                AbilityData abilityData = new AbilityData(data.powerLevel(), Either.left(key));
                miraculous.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                    if (ability.canActivate(abilityData, level, event.getPos(), player) && ability.perform(abilityData, level, event.getPos(), player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target)) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.mainPowerActive()) {
                    if (overrideActive.get()) {
                        player.getData(MineraculousAttachmentTypes.MIRACULOUS).put(player, key, data.withPowerStatus(false, false), true);
                    } else {
                        boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(abilityData, level, event.getPos(), player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target));
                        if (usedPower) {
                            player.getData(MineraculousAttachmentTypes.MIRACULOUS).put(player, key, data.withUsedPower(), true);
                            MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger((ServerPlayer) player, key, target instanceof LivingEntity ? MiraculousUsePowerTrigger.Context.LIVING_ENTITY : MiraculousUsePowerTrigger.Context.ENTITY);
                        }
                    }
                }
            });
            player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                ResourceKey<Kamikotization> key = data.kamikotization();
                Kamikotization kamikotization = level.holderOrThrow(key).value();
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                AbilityData abilityData = new AbilityData(0, Either.right(key));
                kamikotization.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                    if (ability.canActivate(abilityData, level, player.blockPosition(), player) && ability.perform(abilityData, level, player.blockPosition(), player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target)) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.mainPowerActive()) {
                    if (!overrideActive.get()) {
                        boolean usedPower = kamikotization.powerSource().right().isPresent() && kamikotization.powerSource().right().get().value().perform(abilityData, level, player.blockPosition(), player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target));
                        if (usedPower) {
                            data.withMainPowerActive(false).save(player, true);
                            if (player instanceof ServerPlayer serverPlayer) {
                                MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(serverPlayer, key, target instanceof LivingEntity ? KamikotizationUsePowerTrigger.Context.LIVING_ENTITY : KamikotizationUsePowerTrigger.Context.ENTITY);
                            }
                        }
                    } else
                        data.withMainPowerActive(false).save(player, true);
                }
            });
        }
    }

    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level() instanceof ServerLevel level) {
            Entity target = event.getTarget();
            boolean blocked = target instanceof LivingEntity livingEntity && livingEntity.isBlocking();
            LivingEntity livingTarget;
            if (target instanceof LivingEntity livingEntity)
                livingTarget = livingEntity;
            else {
                livingTarget = null;
            }
            player.getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
                Miraculous miraculous = level.holderOrThrow(key).value();
                MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                AbilityData abilityData = new AbilityData(data.powerLevel(), Either.left(key));
                miraculous.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                    if (ability.canActivate(abilityData, level, player.blockPosition(), player) && ability.perform(abilityData, level, player.blockPosition(), player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target)) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.mainPowerActive()) {
                    if (overrideActive.get()) {
                        player.getData(MineraculousAttachmentTypes.MIRACULOUS).put(player, key, data.withPowerStatus(false, false), true);
                    } else {
                        boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(abilityData, level, player.blockPosition(), player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target));
                        if (usedPower) {
                            player.getData(MineraculousAttachmentTypes.MIRACULOUS).put(player, key, data.withUsedPower(), true);
                            MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger((ServerPlayer) player, key, target instanceof LivingEntity ? MiraculousUsePowerTrigger.Context.LIVING_ENTITY : MiraculousUsePowerTrigger.Context.ENTITY);
                        }
                    }
                }
            });
            player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                ResourceKey<Kamikotization> key = data.kamikotization();
                Kamikotization kamikotization = level.holderOrThrow(key).value();
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                AbilityData abilityData = new AbilityData(0, Either.right(key));
                kamikotization.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                    if (ability.canActivate(abilityData, level, player.blockPosition(), player) && ability.perform(abilityData, level, player.blockPosition(), player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target)) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.mainPowerActive()) {
                    if (!overrideActive.get()) {
                        boolean usedPower = kamikotization.powerSource().right().isPresent() && kamikotization.powerSource().right().get().value().perform(abilityData, level, player.blockPosition(), player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target));
                        if (usedPower) {
                            data.withMainPowerActive(false).save(player, true);
                            MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger((ServerPlayer) player, key, target instanceof LivingEntity ? KamikotizationUsePowerTrigger.Context.LIVING_ENTITY : KamikotizationUsePowerTrigger.Context.ENTITY);
                        }
                    } else
                        data.withMainPowerActive(false).save(player, true);
                }
            });
        }
    }

    public static void onLivingAttack(LivingDamageEvent.Post event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            Entity attacker = event.getSource().getDirectEntity();
            LivingEntity victim = event.getEntity();
            boolean blocked = victim.isBlocking();
            if (attacker instanceof LivingEntity livingEntity) {
                livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
                    Miraculous miraculous = level.holderOrThrow(key).value();
                    MiraculousData data = livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
                    AtomicBoolean overrideActive = new AtomicBoolean(false);
                    AbilityData abilityData = new AbilityData(data.powerLevel(), Either.left(key));
                    miraculous.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                        if (ability.canActivate(abilityData, level, livingEntity.blockPosition(), livingEntity) && ability.perform(abilityData, level, livingEntity.blockPosition(), livingEntity, blocked ? Ability.Context.from(victim.getUseItem(), victim) : Ability.Context.from(victim)) && ability.overrideActive())
                            overrideActive.set(true);
                    });
                    if (data.mainPowerActive()) {
                        if (overrideActive.get()) {
                            event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, data.withPowerStatus(false, false), true);
                        } else {
                            boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(abilityData, level, livingEntity.blockPosition(), livingEntity, blocked ? Ability.Context.from(victim.getUseItem(), victim) : Ability.Context.from(victim));
                            if (usedPower) {
                                event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, data.withUsedPower(), true);
                                if (event.getEntity() instanceof ServerPlayer player) {
                                    MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(player, key, MiraculousUsePowerTrigger.Context.LIVING_ENTITY);
                                }
                            }
                        }
                    }
                });
                livingEntity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                    ResourceKey<Kamikotization> key = data.kamikotization();
                    Kamikotization kamikotization = level.holderOrThrow(key).value();
                    AtomicBoolean overrideActive = new AtomicBoolean(false);
                    AbilityData abilityData = new AbilityData(0, Either.right(key));
                    kamikotization.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                        if (ability.canActivate(abilityData, level, livingEntity.blockPosition(), livingEntity) && ability.perform(abilityData, level, livingEntity.blockPosition(), livingEntity, blocked ? Ability.Context.from(victim.getUseItem(), victim) : Ability.Context.from(victim)) && ability.overrideActive())
                            overrideActive.set(true);
                    });
                    if (data.mainPowerActive()) {
                        if (!overrideActive.get()) {
                            boolean usedPower = kamikotization.powerSource().right().isPresent() && kamikotization.powerSource().right().get().value().perform(abilityData, level, livingEntity.blockPosition(), livingEntity, blocked ? Ability.Context.from(victim.getUseItem(), victim) : Ability.Context.from(victim));
                            if (usedPower) {
                                data.withMainPowerActive(false).save(livingEntity, true);
                                if (event.getEntity() instanceof ServerPlayer player) {
                                    MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(player, key, KamikotizationUsePowerTrigger.Context.LIVING_ENTITY);
                                }
                            }
                        } else
                            data.withMainPowerActive(false).save(livingEntity, true);
                    }
                });
            }
            CompoundTag data = TommyLibServices.ENTITY.getPersistentData(victim);
            if (data.getBoolean(TAG_SHOW_KAMIKO_MASK)) {
                data.putBoolean(TAG_CAMERA_CONTROL_INTERRUPTED, true);
                TommyLibServices.ENTITY.setPersistentData(victim, data, true);
            }
        }
    }

    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel() instanceof ServerLevel level) {
            Player player = event.getEntity();
            player.getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
                Miraculous miraculous = level.holderOrThrow(key).value();
                MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                AbilityData abilityData = new AbilityData(data.powerLevel(), Either.left(key));
                miraculous.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                    if (ability.canActivate(abilityData, level, event.getPos(), player) && ability.perform(abilityData, level, event.getPos(), player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos())) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.mainPowerActive()) {
                    if (overrideActive.get()) {
                        player.getData(MineraculousAttachmentTypes.MIRACULOUS).put(player, key, data.withPowerStatus(false, false), true);
                    } else {
                        boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(abilityData, level, event.getPos(), player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos()));
                        if (usedPower) {
                            player.getData(MineraculousAttachmentTypes.MIRACULOUS).put(player, key, data.withUsedPower(), true);
                            MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger((ServerPlayer) player, key, MiraculousUsePowerTrigger.Context.BLOCK);
                        }
                    }
                }
            });
            player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                ResourceKey<Kamikotization> key = data.kamikotization();
                Kamikotization kamikotization = level.holderOrThrow(key).value();
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                AbilityData abilityData = new AbilityData(0, Either.right(key));
                kamikotization.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                    if (ability.canActivate(abilityData, level, event.getPos(), player) && ability.perform(abilityData, level, event.getPos(), player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos())) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.mainPowerActive()) {
                    if (!overrideActive.get()) {
                        boolean usedPower = kamikotization.powerSource().right().isPresent() && kamikotization.powerSource().right().get().value().perform(abilityData, level, event.getPos(), player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos()));
                        if (usedPower) {
                            data.withMainPowerActive(false).save(player, true);
                            MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger((ServerPlayer) player, key, KamikotizationUsePowerTrigger.Context.BLOCK);
                        }
                    } else
                        data.withMainPowerActive(false).save(player, true);
                }
            });
        }
    }

    public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel() instanceof ServerLevel level) {
            Player player = event.getEntity();
            player.getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
                Miraculous miraculous = level.holderOrThrow(key).value();
                MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                AbilityData abilityData = new AbilityData(data.powerLevel(), Either.left(key));
                miraculous.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                    if (ability.canActivate(abilityData, level, event.getPos(), player) && ability.perform(abilityData, level, event.getPos(), player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos())) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.mainPowerActive()) {
                    if (overrideActive.get()) {
                        player.getData(MineraculousAttachmentTypes.MIRACULOUS).put(player, key, data.withPowerStatus(false, false), true);
                    } else {
                        boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(abilityData, level, event.getPos(), player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos()));
                        if (usedPower) {
                            player.getData(MineraculousAttachmentTypes.MIRACULOUS).put(player, key, data.withUsedPower(), true);
                            MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger((ServerPlayer) player, key, MiraculousUsePowerTrigger.Context.BLOCK);
                        }
                    }
                }
            });
            player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                ResourceKey<Kamikotization> key = data.kamikotization();
                Kamikotization kamikotization = level.holderOrThrow(key).value();
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                AbilityData abilityData = new AbilityData(0, Either.right(key));
                kamikotization.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                    if (ability.canActivate(abilityData, level, event.getPos(), player) && ability.perform(abilityData, level, event.getPos(), player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos())) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.mainPowerActive()) {
                    if (!overrideActive.get()) {
                        boolean usedPower = kamikotization.powerSource().right().isPresent() && kamikotization.powerSource().right().get().value().perform(abilityData, level, event.getPos(), player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos()));
                        if (usedPower) {
                            data.withMainPowerActive(false).save(player, true);
                            MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger((ServerPlayer) player, key, KamikotizationUsePowerTrigger.Context.BLOCK);
                        }
                    } else
                        data.withMainPowerActive(false).save(player, true);
                }
            });
        }
    }

    public static void onEmptyLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        TommyLibServices.NETWORK.sendToServer(new ServerboundSendEmptyLeftClickPayload(event.getEntity().getId()));
    }

    public static ItemStack convertToCataclysmDust(ItemStack stack) {
        if (!stack.is(MineraculousItemTags.CATACLYSM_IMMUNE)) {
            return MineraculousItems.CATACLYSM_DUST.get().getDefaultInstance();
        }
        return stack;
    }

    public static boolean isCataclysmed(LivingEntity entity) {
        return entity.hasEffect(MineraculousMobEffects.CATACLYSMED);
    }

    public static Component formatDisplayName(Entity entity, Component original) {
        if (original != null) {
            Style style = original.getStyle();
            MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS);
            List<ResourceKey<Miraculous>> transformed = miraculousDataSet.getTransformed();
            if (!transformed.isEmpty()) {
                MiraculousData data = miraculousDataSet.get(transformed.getFirst());
                if (data.miraculousItem().has(MineraculousDataComponents.MIRACULOUS)) {
                    Style newStyle = style.withColor(entity.level().holderOrThrow(data.miraculousItem().get(MineraculousDataComponents.MIRACULOUS)).value().color());
                    if (!data.name().isEmpty())
                        return Component.literal(data.name()).setStyle(newStyle);
                    return Entity.removeAction(original.copy().setStyle(newStyle.withObfuscated(true).withHoverEvent(null)));
                }
            } else if (entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
                KamikotizationData data = entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).get();
                Style newStyle = style.withColor(data.kamikoData().nameColor());
                return Entity.removeAction(Component.literal(data.name()).setStyle(newStyle.withHoverEvent(null)));
            }
        }
        return original;
    }

    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        for (EntityType<? extends LivingEntity> type : MineraculousEntityTypes.getAllAttributes().keySet()) {
            event.put(type, MineraculousEntityTypes.getAllAttributes().get(type));
        }
    }

    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (MineraculousEntityEvents.isCataclysmed(event.getEntity()) && event.getEffect() == MineraculousMobEffects.CATACLYSMED)
            event.setCanceled(true);
    }

    public static void onLivingHeal(LivingHealEvent event) {
        if (MineraculousEntityEvents.isCataclysmed(event.getEntity()))
            event.setCanceled(true);
    }

    public static void onRegisterVillagerTrades(VillagerTradesEvent event) {
        MineraculousVillagerTrades.TRADES.forEach((profession, trades) -> {
            if (event.getType() == profession.get())
                event.getTrades().putAll(trades);
        });
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) {
            TommyLibServices.NETWORK.sendToServer(new ServerboundRequestMiraculousDataSetSyncPayload(event.getEntity().getId()));
        } else {
            if (event.getEntity() instanceof ServerPlayer player) {
                MiraculousDataSet miraculousDataSet = player.getData(MineraculousAttachmentTypes.MIRACULOUS);
                miraculousDataSet.getTransformedHolders(event.getLevel().registryAccess()).forEach(miraculous -> {
                    NightVisionAbility nightVisionAbility = Ability.getFirstMatching(ability -> ability instanceof NightVisionAbility, miraculous.value(), miraculousDataSet.get(miraculous.getKey()).mainPowerActive()) instanceof NightVisionAbility n ? n : null;
                    if (nightVisionAbility != null)
                        nightVisionAbility.resetNightVision(player);
                });
                if (player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
                    KamikotizationData kamikotizationData = player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).get();
                    NightVisionAbility nightVisionAbility = Ability.getFirstMatching(ability -> ability instanceof NightVisionAbility, player.level().holderOrThrow(kamikotizationData.kamikotization()).value(), kamikotizationData.mainPowerActive()) instanceof NightVisionAbility n ? n : null;
                    if (nightVisionAbility != null)
                        nightVisionAbility.resetNightVision(player);
                }

                for (ResourceKey<Miraculous> miraculous : miraculousDataSet.keySet()) {
                    Map<String, FlattenedSuitLookData> commonSuitLooks = ((FlattenedLookDataHolder) event.getLevel().getServer().overworld()).mineraculous$getCommonSuitLookData().get(miraculous);
                    String look = miraculousDataSet.get(miraculous).suitLook();
                    if (!look.isEmpty()) {
                        if (commonSuitLooks.containsKey(look)) {
                            miraculousDataSet.put(player, miraculous, miraculousDataSet.get(miraculous).withSuitLook(look), true);
                            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncSuitLookPayload(player.getUUID(), miraculous, commonSuitLooks.get(look), true), player.getServer());
                        } else
                            TommyLibServices.NETWORK.sendToClient(new ClientboundRequestSyncSuitLookPayload(miraculous, look), player);
                    }
                    Map<String, FlattenedMiraculousLookData> commonMiraculousLooks = ((FlattenedLookDataHolder) event.getLevel().getServer().overworld()).mineraculous$getCommonMiraculousLookData().get(miraculous);
                    look = miraculousDataSet.get(miraculous).miraculousLook();
                    if (!look.isEmpty()) {
                        if (commonMiraculousLooks.containsKey(look)) {
                            miraculousDataSet.put(player, miraculous, miraculousDataSet.get(miraculous).withMiraculousLook(look), true);
                            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncMiraculousLookPayload(player.getUUID(), miraculous, commonMiraculousLooks.get(look), true), player.getServer());
                        } else
                            TommyLibServices.NETWORK.sendToClient(new ClientboundRequestSyncMiraculousLookPayload(miraculous, look), player);
                    }
                }

                if (player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
                    TommyLibServices.NETWORK.sendToClient(new ClientboundRequestSyncKamikotizationLookPayload(player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).get().kamikotization()), player);
                }
            }
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer serverPlayer = (ServerPlayer) event.getEntity();
        FlattenedLookDataHolder flattenedLookDataHolder = (FlattenedLookDataHolder) serverPlayer.getServer().overworld();
        flattenedLookDataHolder.mineraculous$getSuitLookData().remove(serverPlayer.getUUID());
        flattenedLookDataHolder.mineraculous$getMiraculousLookData().remove(serverPlayer.getUUID());
        flattenedLookDataHolder.mineraculous$getKamikotizationLookData().remove(serverPlayer.getUUID());
        serverPlayer.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO).id().ifPresent(id -> {
            Entity yoyo = serverPlayer.level().getEntity(id);
            if (yoyo != null)
                yoyo.discard();
            ThrownLadybugYoyoData.remove(serverPlayer, true);
        });
    }

    public static void handleKamikotizationTransformation(ServerPlayer player, KamikotizationData data, boolean transform, boolean instant, Vec3 kamikoSpawnPos) {
        if (player != null) {
            ServerLevel serverLevel = player.serverLevel();
            ItemStack originalStack = data.slotInfo().left().isPresent() ? player.getInventory().getItem(data.slotInfo().left().get()) : CuriosUtils.getStackInSlot(player, data.slotInfo().right().get());
            Kamikotization kamikotization = serverLevel.holderOrThrow(data.kamikotization()).value();
            ItemStack kamikotizationStack = originalStack.copy();
            int transformationFrames = 10;
            if (transform) {
                // Transform
                if (player.getData(MineraculousAttachmentTypes.MIRACULOUS).isTransformed()) {
                    return;
                }

                if (kamikotization.powerSource().left().isPresent())
                    kamikotizationStack = kamikotization.powerSource().left().get().copyWithCount(Math.min(kamikotizationStack.getCount(), kamikotization.powerSource().left().get().getMaxStackSize()));

                data = data.withStackCount(kamikotizationStack.getCount());

                kamikotizationStack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);
                kamikotizationStack.set(MineraculousDataComponents.KAMIKO_DATA.get(), data.kamikoData());
                kamikotizationStack.set(MineraculousDataComponents.KAMIKOTIZATION, data.kamikotization());
                kamikotizationStack.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));

                ((MiraculousRecoveryDataHolder) serverLevel.getServer().overworld()).mineraculous$getMiraculousRecoveryItemData().putKamikotized(player.getUUID(), originalStack);

                if (!instant) {
                    data = data.withTransformationFrames(transformationFrames);
                } else {
                    data = data.withTransformationFrames(-1);
                    ArmorData armor = new ArmorData(player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST), player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET));
                    player.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.of(armor));
                    for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                        ItemStack stack = Kamikotization.createItemStack(MineraculousArmors.KAMIKOTIZATION.getForSlot(slot).get(), data.kamikotization());
                        stack.enchant(serverLevel.holderOrThrow(Enchantments.BINDING_CURSE), 1);
                        stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);
                        player.setItemSlot(slot, stack);
                    }
                    for (int i = 0; i <= 10; i++) {
                        player.serverLevel().sendParticles(MineraculousParticleTypes.KAMIKOTIZATION.get(), player.getX(), player.getY() + 2 - (11 - transformationFrames) / 5.0, player.getZ(), 100, Math.random() / 3.0, Math.random() / 3.0, Math.random() / 3.0, 0);
                    }
                }

                data.save(player, true);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), MineraculousSoundEvents.KAMIKOTIZATION_TRANSFORM, SoundSource.PLAYERS, 1, 1);
                if (data.slotInfo().left().isPresent()) {
                    player.getInventory().setItem(data.slotInfo().left().get(), kamikotizationStack);
                } else {
                    CuriosUtils.setStackInSlot(player, data.slotInfo().right().get(), kamikotizationStack);
                }
                serverLevel.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).forEach((effect, startLevel) -> player.addEffect(INFINITE_HIDDEN_EFFECT.apply(serverLevel.holderOrThrow(effect), startLevel)));
                KamikotizationData finalData = data;
                kamikotization.powerSource().right().ifPresent(ability -> ability.value().transform(new AbilityData(0, Either.right(finalData.kamikotization())), serverLevel, player.blockPosition(), player));
                kamikotization.passiveAbilities().forEach(ability -> ability.value().transform(new AbilityData(0, Either.right(finalData.kamikotization())), serverLevel, player.blockPosition(), player));
                ((MiraculousRecoveryDataHolder) serverLevel.getServer().overworld()).mineraculous$getMiraculousRecoveryEntityData().startTracking(player.getUUID());
            } else {
                // De-transform
                Kamiko kamiko = summonKamiko(player.level(), data, kamikoSpawnPos);
                if (kamiko != null) {
                    kamiko.setOwnerUUID(data.kamikoData().owner());
                } else {
                    Mineraculous.LOGGER.error("Kamiko could not be created for player " + player.getName().plainCopy().getString());
                    return;
                }
                kamikotizationStack.remove(DataComponents.ENCHANTMENTS);
                kamikotizationStack.remove(MineraculousDataComponents.KAMIKOTIZATION.get());
                kamikotizationStack.remove(DataComponents.PROFILE);

                if (!instant) {
                    data = data.withDetransformationFrames(transformationFrames);
                } else {
                    data = data.withDetransformationFrames(-1);
                    KamikotizationData.remove(player, true);
                    player.getData(MineraculousAttachmentTypes.STORED_ARMOR).ifPresent(armorData -> {
                        for (EquipmentSlot slot : Arrays.stream(EquipmentSlot.values()).filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR).toArray(EquipmentSlot[]::new)) {
                            player.setItemSlot(slot, armorData.forSlot(slot));
                        }
                    });
                }

                if (data.slotInfo().left().isPresent()) {
                    player.getInventory().setItem(data.slotInfo().left().get(), kamikotizationStack);
                } else {
                    CuriosUtils.setStackInSlot(player, data.slotInfo().right().get(), kamikotizationStack);
                }
                if (instant)
                    KamikotizationData.remove(player, true);
                else
                    data.save(player, true);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), MineraculousSoundEvents.KAMIKOTIZATION_DETRANSFORM, SoundSource.PLAYERS, 1, 1);
                serverLevel.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS).keySet().stream().map(serverLevel::holderOrThrow).forEach(player::removeEffect);
                KamikotizationData finalData1 = data;
                kamikotization.powerSource().right().ifPresent(ability -> ability.value().detransform(new AbilityData(0, Either.right(finalData1.kamikotization())), serverLevel, player.blockPosition(), player));
                kamikotization.passiveAbilities().forEach(ability -> ability.value().detransform(new AbilityData(0, Either.right(finalData1.kamikotization())), serverLevel, player.blockPosition(), player));
                CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(player);
                entityData.putBoolean(TAG_SHOW_KAMIKO_MASK, false);
                TommyLibServices.ENTITY.setPersistentData(player, entityData, true);
            }
            player.refreshDisplayName();
        }
    }

    public static Kamiko summonKamiko(Level level, KamikotizationData data, Vec3 kamikoSpawnPos) {
        Kamiko kamiko = MineraculousEntityTypes.KAMIKO.get().create(level);
        if (kamiko != null) {
            KamikoData kamikoData = data.kamikoData();
            if (kamikoData != null) {
                kamiko.setUUID(kamikoData.uuid());
                kamiko.setOwnerUUID(kamikoData.owner());
            }
            kamiko.setPos(kamikoSpawnPos);
            level.addFreshEntity(kamiko);
        }
        return kamiko;
    }

    public static void onPlayerBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack mainHandItem = event.getEntity().getMainHandItem();
        if (mainHandItem.is(MineraculousItems.LADYBUG_YOYO.get()) && mainHandItem.has(MineraculousDataComponents.ACTIVE)) {
            event.setCanceled(true);
        }
    }

    public static void onMobEffectRemoved(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        Map<ResourceKey<MobEffect>, Integer> effectsMap = entity.level().registryAccess().registryOrThrow(Registries.MOB_EFFECT).getDataMap(MineraculousDataMaps.MIRACULOUS_EFFECTS);
        Holder<MobEffect> effect = event.getEffect();
        ResourceKey<MobEffect> effectKey = effect.getKey();
        MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS);
        if (miraculousDataSet.isTransformed()) {
            if (effectsMap.containsKey(effectKey))
                event.setCanceled(true);
            else {
                for (ResourceKey<Miraculous> miraculous : miraculousDataSet.getTransformed()) {
                    if (Ability.hasMatching(a -> a instanceof ApplyEffectsWhileTransformedAbility effectsAbility && effectsAbility.effects().contains(effect), entity.level().holderOrThrow(miraculous).value(), true))
                        event.setCanceled(true);
                }
            }
        }
        if (entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
            KamikotizationData data = entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).get();
            if (effectsMap.containsKey(effectKey))
                event.setCanceled(true);
            else {
                if (Ability.hasMatching(a -> a instanceof ApplyEffectsWhileTransformedAbility effectsAbility && effectsAbility.effects().contains(effect), entity.level().holderOrThrow(data.kamikotization()).value(), true))
                    event.setCanceled(true);
            }
        }
    }

    public static void onBlockDrops(BlockDropsEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            MiraculousRecoveryDataHolder recoveryDataHolder = (MiraculousRecoveryDataHolder) serverLevel.getServer().overworld();
            UUID recoverer = recoveryDataHolder.mineraculous$getMiraculousRecoveryBlockData().getRecoverer(event.getPos());
            if (recoverer != null) {
                for (ItemEntity item : event.getDrops()) {
                    UUID id = UUID.randomUUID();
                    recoveryDataHolder.mineraculous$getMiraculousRecoveryItemData().putRemovable(recoverer, id);
                    item.getItem().set(MineraculousDataComponents.RECOVERABLE_ITEM_ID, id);
                }
            }
        }
    }

    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().level() instanceof ServerLevel serverLevel) {
            MiraculousRecoveryDataHolder recoveryDataHolder = (MiraculousRecoveryDataHolder) serverLevel.getServer().overworld();
            UUID recoverer = recoveryDataHolder.mineraculous$getMiraculousRecoveryEntityData().getRecoverer(event.getEntity(), serverLevel);
            if (recoverer != null) {
                for (ItemEntity item : event.getDrops()) {
                    UUID id = UUID.randomUUID();
                    recoveryDataHolder.mineraculous$getMiraculousRecoveryItemData().putRemovable(recoverer, id);
                    item.getItem().set(MineraculousDataComponents.RECOVERABLE_ITEM_ID, id);
                }
            }
        }
    }

    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            int i = entity.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO).safeFallTicks();
            if (i > 0) {
                event.setDamageMultiplier(0);
            }
        }
    }
}
