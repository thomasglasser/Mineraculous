package dev.thomasglasser.mineraculous.world.entity;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.advancements.critereon.KamikotizationUsePowerTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.MiraculousUsePowerTrigger;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalCuriosInventoryScreen;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.network.ClientboundRequestSyncKamikotizationLookPayload;
import dev.thomasglasser.mineraculous.network.ClientboundRequestSyncMiraculousLookPayload;
import dev.thomasglasser.mineraculous.network.ClientboundRequestSyncSuitLookPayload;
import dev.thomasglasser.mineraculous.network.ClientboundSyncKamikotizationLookPayload;
import dev.thomasglasser.mineraculous.network.ClientboundSyncMiraculousLookPayload;
import dev.thomasglasser.mineraculous.network.ClientboundSyncSuitLookPayload;
import dev.thomasglasser.mineraculous.network.ServerboundRequestInventorySyncPayload;
import dev.thomasglasser.mineraculous.network.ServerboundRequestMiraculousDataSetSyncPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetKamikotizationPowerActivatedPayload;
import dev.thomasglasser.mineraculous.network.ServerboundStealCuriosPayload;
import dev.thomasglasser.mineraculous.network.ServerboundStealItemPayload;
import dev.thomasglasser.mineraculous.network.ServerboundTryBreakItemPayload;
import dev.thomasglasser.mineraculous.network.ServerboundWakeUpPayload;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.NightVisionAbility;
import dev.thomasglasser.mineraculous.world.entity.npc.MineraculousVillagerTrades;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedLookDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.mineraculous.world.level.storage.ToolIdDataHolder;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import top.theillusivec4.curios.common.inventory.CurioSlot;

public class MineraculousEntityEvents {
    public static final String TAG_WAITTICKS = "WaitTicks";
    public static final String TAG_HASNIGHTVISION = "HasNightVision";
    public static final String TAG_TAKETICKS = "TakeTicks";
    public static final String TAG_CATACLYSMED = "Cataclysmed";
    public static final String TAG_SHOW_KAMIKO_MASK = "ShowKamikoMask";
    public static final String TAG_CAMERA_CONTROL_INTERRUPTED = "CameraControlInterrupted";

    public static final String ITEM_BROKEN_KEY = "mineraculous.item_broken";

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
            MobEffects.ABSORPTION);

    public static void onLivingTick(EntityTickEvent.Post event) {
        CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(event.getEntity());
        int waitTicks = entityData.getInt(MineraculousEntityEvents.TAG_WAITTICKS);
        if (waitTicks > 0) {
            entityData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, --waitTicks);
        }
        TommyLibServices.ENTITY.setPersistentData(event.getEntity(), entityData, false);
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(player);

        if (player.level().isClientSide && entityData.getInt(TAG_WAITTICKS) == 0 && ClientUtils.getMainClientPlayer() == player) {
            //jump input key trigger started
            if (Minecraft.getInstance().player != null) {
                MineraculousClientUtils.jump = Minecraft.getInstance().player.input.jumping;
                if (MineraculousClientUtils.jumpX != MineraculousClientUtils.jump) {
                    MineraculousClientUtils.jumpKeyStartPressing = true;
                } else {
                    MineraculousClientUtils.jumpKeyStartPressing = false;
                }
                MineraculousClientUtils.jumpX = MineraculousClientUtils.jump;
            }

            int takeTicks = entityData.getInt(MineraculousEntityEvents.TAG_TAKETICKS);
            if (MineraculousKeyMappings.TAKE_BREAK_ITEM.get().isDown()) {
                ItemStack mainHandItem = player.getMainHandItem();
                if (mainHandItem.isEmpty()) {
                    if (MineraculousClientUtils.getLookEntity() instanceof Player target && (MineraculousServerConfig.INSTANCE.enableUniversalStealing.get() || player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).isTransformed()) && (MineraculousServerConfig.INSTANCE.enableSleepStealing.get() || !target.isSleeping())) {
                        entityData.putInt(MineraculousEntityEvents.TAG_TAKETICKS, ++takeTicks);
                        if (target.isSleeping() && MineraculousServerConfig.INSTANCE.wakeUpChance.get() > 0 && (MineraculousServerConfig.INSTANCE.wakeUpChance.get() >= 100 || player.getRandom().nextFloat() < MineraculousServerConfig.INSTANCE.wakeUpChance.get() / (20f * 5 * 100))) {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundWakeUpPayload(target.getUUID(), true));
                        }
                        if (takeTicks > (20 * MineraculousServerConfig.INSTANCE.stealingDuration.get())) {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundRequestInventorySyncPayload(target.getUUID()));
                            ClientUtils.setScreen(new ExternalCuriosInventoryScreen(target, true, ((slot, target1, menu) -> {
                                if (slot instanceof CurioSlot curioSlot)
                                    TommyLibServices.NETWORK.sendToServer(new ServerboundStealCuriosPayload(target1.getUUID(), new CuriosData(curioSlot.getSlotIndex(), curioSlot.getIdentifier())));
                                else
                                    TommyLibServices.NETWORK.sendToServer(new ServerboundStealItemPayload(target1.getUUID(), menu.slots.indexOf(slot)));
                            }), exit -> {
                                TommyLibServices.NETWORK.sendToServer(new ServerboundRequestInventorySyncPayload(target.getUUID()));
                                TommyLibServices.NETWORK.sendToServer(new ServerboundRequestInventorySyncPayload(player.getUUID()));
                            }));
                            entityData.putInt(MineraculousEntityEvents.TAG_TAKETICKS, 0);
                        }
                        TommyLibServices.ENTITY.setPersistentData(player, entityData, false);
                    }
                } else {
                    TommyLibServices.NETWORK.sendToServer(ServerboundTryBreakItemPayload.INSTANCE);
                    entityData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
                    TommyLibServices.ENTITY.setPersistentData(player, entityData, false);
                }
            } else if (takeTicks > 0) {
                entityData.putInt(MineraculousEntityEvents.TAG_TAKETICKS, 0);
                TommyLibServices.ENTITY.setPersistentData(player, entityData, false);
            }
        }

        if (player instanceof ServerPlayer serverPlayer) {
            player.getInventory().clearOrCountMatchingItems(itemStack -> {
                if (itemStack.has(MineraculousDataComponents.KWAMI_DATA) && itemStack.has(MineraculousDataComponents.TOOL_ID)) {
                    int currentId = ((ToolIdDataHolder) serverPlayer.serverLevel().getServer().overworld()).mineraculous$getToolIdData().getToolId(itemStack.get(MineraculousDataComponents.KWAMI_DATA));
                    Integer stackId = itemStack.get(MineraculousDataComponents.TOOL_ID);
                    return stackId != null && stackId != currentId;
                }
                return false;
            }, Integer.MAX_VALUE, new SimpleContainer());
            CuriosUtils.getAllItems(player).forEach(((curiosData, itemStack) -> {
                if (itemStack.has(MineraculousDataComponents.KWAMI_DATA) && itemStack.has(MineraculousDataComponents.TOOL_ID)) {
                    int currentId = ((ToolIdDataHolder) serverPlayer.serverLevel().getServer().overworld()).mineraculous$getToolIdData().getToolId(itemStack.get(MineraculousDataComponents.KWAMI_DATA));
                    Integer stackId = itemStack.get(MineraculousDataComponents.TOOL_ID);
                    if (stackId != null && stackId != currentId) {
                        itemStack.shrink(1);
                    }
                }
            }));

            if (serverPlayer.tickCount == 15) {
                FlattenedLookDataHolder flattenedLookDataHolder = (FlattenedLookDataHolder) player.getServer().overworld();
                flattenedLookDataHolder.mineraculous$getSuitLookData().forEach((uuid, dataSet) -> {
                    dataSet.forEach(data -> TommyLibServices.NETWORK.sendToClient(new ClientboundSyncSuitLookPayload(uuid, data, false), serverPlayer));
                });
                flattenedLookDataHolder.mineraculous$getMiraculousLookData().forEach((uuid, dataSet) -> {
                    dataSet.forEach(data -> TommyLibServices.NETWORK.sendToClient(new ClientboundSyncMiraculousLookPayload(uuid, data, false), serverPlayer));
                });
                flattenedLookDataHolder.mineraculous$getKamikotizationLookData().forEach((uuid, data) -> {
                    TommyLibServices.NETWORK.sendToClient(new ClientboundSyncKamikotizationLookPayload(uuid, data), serverPlayer);
                });
            }
        }

        Optional<KamikotizationData> optionalData = player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION);
        if (optionalData.isPresent()) {
            KamikotizationData kamikotizationData = optionalData.get();
            ResourceKey<Kamikotization> kamikotization = kamikotizationData.kamikotization();
            Integer transformationFrames = kamikotizationData.kamikotizedStack().get(MineraculousDataComponents.TRANSFORMATION_FRAMES);
            Integer detransformationFrames = kamikotizationData.kamikotizedStack().get(MineraculousDataComponents.DETRANSFORMATION_FRAMES);
            if (player instanceof ServerPlayer serverPlayer) {
                boolean useHead = detransformationFrames == null;
                if (useHead)
                    detransformationFrames = player.getItemBySlot(EquipmentSlot.HEAD).get(MineraculousDataComponents.DETRANSFORMATION_FRAMES);
                if (transformationFrames != null && transformationFrames >= 1) {
                    if (player.tickCount % 2 == 0) {
                        if (transformationFrames <= 1) {
                            kamikotizationData.kamikotizedStack().remove(MineraculousDataComponents.TRANSFORMATION_FRAMES);
                            kamikotizationData.save(player, true);
                            ArmorData armor = new ArmorData(player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST), player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET));
                            player.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.of(armor));
                            for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                                ItemStack stack = Kamikotization.createItemStack(MineraculousArmors.KAMIKOTIZATION.getForSlot(slot).get(), kamikotization);
                                stack.enchant(player.level().holderOrThrow(Enchantments.BINDING_CURSE), 1);
                                stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);
                                player.setItemSlot(slot, stack);
                            }
                        } else {
                            kamikotizationData.kamikotizedStack().set(MineraculousDataComponents.TRANSFORMATION_FRAMES, transformationFrames - 1);
                            kamikotizationData.save(player, true);
                        }
                    }
                    serverPlayer.serverLevel().sendParticles(MineraculousParticleTypes.KAMIKOTIZATION.get(), player.getX(), player.getY() + 2 - (11 - transformationFrames) / 5.0, player.getZ(), 100, Math.random() / 3.0, Math.random() / 3.0, Math.random() / 3.0, 0);
                } else if (detransformationFrames != null && detransformationFrames >= 1) {
                    if (player.tickCount % 2 == 0) {
                        if (detransformationFrames <= 1) {
                            if (useHead)
                                player.getItemBySlot(EquipmentSlot.HEAD).remove(MineraculousDataComponents.DETRANSFORMATION_FRAMES);
                            else
                                kamikotizationData.kamikotizedStack().remove(MineraculousDataComponents.DETRANSFORMATION_FRAMES);
                            KamikotizationData.remove(player, true);
                            player.getData(MineraculousAttachmentTypes.STORED_ARMOR).ifPresent(data -> {
                                for (EquipmentSlot slot : Arrays.stream(EquipmentSlot.values()).filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR).toArray(EquipmentSlot[]::new)) {
                                    player.setItemSlot(slot, data.forSlot(slot));
                                }
                            });
                        } else {
                            if (useHead)
                                player.getItemBySlot(EquipmentSlot.HEAD).set(MineraculousDataComponents.DETRANSFORMATION_FRAMES, detransformationFrames - 1);
                            else {
                                kamikotizationData.kamikotizedStack().set(MineraculousDataComponents.DETRANSFORMATION_FRAMES, detransformationFrames - 1);
                                kamikotizationData.save(player, true);
                            }
                        }
                    }
                    serverPlayer.serverLevel().sendParticles(MineraculousParticleTypes.KAMIKOTIZATION.get(), player.getX(), player.getY() + 2 - (11 - detransformationFrames) / 5.0, player.getZ(), 100, Math.random() / 3.0, Math.random() / 3.0, Math.random() / 3.0, 0);
                }
            } else if (ClientUtils.getMainClientPlayer() == player) {
                CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(player);
                int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAITTICKS);
                if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen() && !MineraculousClientUtils.isCameraEntityOther()) {
                    if (MineraculousKeyMappings.ACTIVATE_POWER.get().isDown() && !kamikotizationData.mainPowerActive() && player.level().holderOrThrow(kamikotization).value().activeAbility().isPresent()) {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundSetKamikotizationPowerActivatedPayload(kamikotization));
                        playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
                    } else if (MineraculousKeyMappings.OPEN_TOOL_WHEEL.get().isDown() && player.getMainHandItem().isEmpty()) {
//                            TommyLibServices.NETWORK.sendToServer(new ServerboundPutToolInHandPayload(miraculous));
//                            playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
                    }
                }
                TommyLibServices.ENTITY.setPersistentData(player, playerData, false);
            }
            if (transformationFrames == null && detransformationFrames == null) {
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                player.level().holderOrThrow(kamikotization).value().passiveAbilities().forEach(ability -> {
                    if (ability.value().perform(new AbilityData(0, Either.right(kamikotization)), player.level(), player.blockPosition(), player, Ability.Context.PASSIVE) && ability.value().overrideActive())
                        overrideActive.set(true);
                });
                if (!player.getMainHandItem().isEmpty()) {
                    player.level().holderOrThrow(kamikotization).value().passiveAbilities().forEach(ability -> {
                        if (ability.value().perform(new AbilityData(0, Either.right(kamikotization)), player.level(), player.blockPosition(), player, Ability.Context.from(player.getMainHandItem())) && ability.value().overrideActive())
                            overrideActive.set(true);
                    });
                    if (kamikotizationData.mainPowerActive()) {
                        if (!overrideActive.get()) {
                            boolean usedPower = player.level().holderOrThrow(kamikotization).value().activeAbility().get().value().perform(new AbilityData(0, Either.right(kamikotization)), player.level(), player.blockPosition(), player, Ability.Context.from(player.getMainHandItem()));
                            if (usedPower) {
                                if (player instanceof ServerPlayer serverPlayer) {
                                    kamikotizationData.withMainPowerActive(false).save(event.getEntity(), !event.getEntity().level().isClientSide);
                                    MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(serverPlayer, kamikotization, KamikotizationUsePowerTrigger.Context.ITEM);
                                }
                            }
                        } else
                            kamikotizationData.withMainPowerActive(false).save(event.getEntity(), !event.getEntity().level().isClientSide);
                    }
                }
                if (kamikotizationData.mainPowerActive()) {
                    if (overrideActive.get()) {
                        kamikotizationData.withMainPowerActive(false).save(player, !player.level().isClientSide);
                    } else {
                        player.level().holderOrThrow(kamikotization).value().activeAbility().get().value().perform(new AbilityData(0, Either.right(kamikotization)), player.level(), player.blockPosition(), player, Ability.Context.PASSIVE);
                    }
                }
            }
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS.get());
        if (entity instanceof ServerPlayer player) {
            miraculousDataSet.keySet().forEach(miraculous -> {
                MiraculousData data = miraculousDataSet.get(miraculous);
                if (data.transformed())
                    handleMiraculousTransformation(player, miraculous, data, false, true);
                renounceMiraculous(data.miraculousItem(), player.serverLevel());
            });
            player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> handleKamikotizationTransformation(player, data, false, true, false, player.position().add(0, 1, 0)));
        }
        if (entity.hasEffect(MineraculousMobEffects.CATACLYSMED) && TommyLibServices.ENTITY.getPersistentData(entity).hasUUID(TAG_CATACLYSMED)) {
            entity.setLastHurtByPlayer(entity.level().getPlayerByUUID(TommyLibServices.ENTITY.getPersistentData(entity).getUUID(TAG_CATACLYSMED)));
        }
    }

    public static void handleMiraculousTransformation(ServerPlayer player, ResourceKey<Miraculous> miraculous, MiraculousData data, boolean transform, boolean instant) {
        if (player != null) {
            ServerLevel serverLevel = player.serverLevel();
            ItemStack miraculousStack = data.miraculousItem();
            int transformationFrames = instant ? 0 : player.serverLevel().holderOrThrow(miraculous).value().transformationFrames();
            if (transform) {
                // Transform
                if (player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
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
                            stack.enchant(serverLevel.holderOrThrow(Enchantments.BINDING_CURSE), 1);
                            stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);
                            if (transformationFrames > 0)
                                stack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, transformationFrames);
                            player.setItemSlot(slot, stack);
                        }

                        miraculousStack.enchant(serverLevel.holderOrThrow(Enchantments.BINDING_CURSE), 1);
                        miraculousStack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);
                        miraculousStack.set(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE);
                        if (transformationFrames > 0)
                            miraculousStack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, transformationFrames);
                        else {
                            ItemStack tool = data.createTool(serverLevel);
                            if (!tool.isEmpty()) {
                                if (serverLevel.holderOrThrow(miraculous).value().toolSlot().isPresent()) {
                                    boolean added = CuriosUtils.setStackInFirstValidSlot(player, serverLevel.holderOrThrow(miraculous).value().toolSlot().get(), tool, true);
                                    if (!added) {
                                        player.addItem(tool);
                                    }
                                } else {
                                    player.addItem(tool);
                                }
                            }
                        }

                        data = data.transform(true, miraculousStack, ((ToolIdDataHolder) serverLevel.getServer().overworld()).mineraculous$getToolIdData().getToolId(kwamiData));
                        player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).put(player, miraculous, data, true);
                        serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), serverLevel.holderOrThrow(miraculous).value().transformSound(), SoundSource.PLAYERS, 1, 1);
                        CuriosUtils.setStackInSlot(player, data.curiosData(), miraculousStack, true);
                        if (data.name().isEmpty())
                            player.displayClientMessage(Component.translatable(MiraculousData.NAME_NOT_SET, Component.translatable(Miraculous.toLanguageKey(miraculous)), miraculous.location().getPath()), true);
                        int powerLevel = data.powerLevel();
                        MIRACULOUS_EFFECTS.forEach(effect -> player.addEffect(INFINITE_HIDDEN_EFFECT.apply(effect, powerLevel / 10)));
                        kwami.discard();
                        MiraculousData finalData = data;
                        serverLevel.holderOrThrow(miraculous).value().activeAbility().ifPresent(ability -> ability.value().transform(new AbilityData(finalData.powerLevel(), Either.left(miraculous)), serverLevel, player.blockPosition(), player));
                        serverLevel.holderOrThrow(miraculous).value().passiveAbilities().forEach(ability -> ability.value().transform(new AbilityData(finalData.powerLevel(), Either.left(miraculous)), serverLevel, player.blockPosition(), player));
                    } else {
                        kwami.playHurtSound(serverLevel.damageSources().starve());
                    }
                } else {
                    miraculousStack.remove(MineraculousDataComponents.KWAMI_DATA.get());
                    CuriosUtils.setStackInSlot(player, data.curiosData(), miraculousStack, true);
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
                CuriosUtils.setStackInSlot(player, data.curiosData(), miraculousStack, true);
                int newToolId = ((ToolIdDataHolder) serverLevel.getServer().overworld()).mineraculous$getToolIdData().incrementToolId(new KwamiData(kwami.getUUID(), kwami.isCharged()));
                data = data.transform(false, miraculousStack, newToolId);
                player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).put(player, miraculous, data, true);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), serverLevel.holderOrThrow(miraculous).value().detransformSound(), SoundSource.PLAYERS, 1, 1);
                MIRACULOUS_EFFECTS.forEach(player::removeEffect);
                MiraculousData finalData = data;
                serverLevel.holderOrThrow(miraculous).value().activeAbility().ifPresent(ability -> ability.value().detransform(new AbilityData(finalData.powerLevel(), Either.left(miraculous)), serverLevel, player.blockPosition(), player));
                serverLevel.holderOrThrow(miraculous).value().passiveAbilities().forEach(ability -> ability.value().detransform(new AbilityData(finalData.powerLevel(), Either.left(miraculous)), serverLevel, player.blockPosition(), player));
            }
        }
    }

    public static void renounceMiraculous(ItemStack miraculous, ServerLevel serverLevel) {
        miraculous.set(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE);
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

                miraculousData.miraculousItem().set(MineraculousDataComponents.KWAMI_DATA.get(), new KwamiData(kwami.getUUID(), kwami.isCharged()));
                CuriosUtils.setStackInSlot(player, miraculousData.curiosData(), miraculousData.miraculousItem(), true);
                player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).put(player, miraculous, miraculousData.withItem(miraculousData.miraculousItem()), true);
                return kwami;
            }
        }
        return null;
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
            Miraculous miraculous = event.getEntity().level().holderOrThrow(key).value();
            MiraculousData data = event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
            AtomicBoolean overrideActive = new AtomicBoolean(false);
            miraculous.passiveAbilities().forEach(ability -> {
                if (ability.value().perform(new AbilityData(data.powerLevel(), Either.left(key)), event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getTarget())) && ability.value().overrideActive())
                    overrideActive.set(true);
            });
            if (data.mainPowerActive()) {
                if (overrideActive.get()) {
                    event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, data.withPowerStatus(false, false), !event.getLevel().isClientSide);
                } else {
                    boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(new AbilityData(data.powerLevel(), Either.left(key)), event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getTarget()));
                    if (usedPower) {
                        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, data.withUsedPower(), !event.getLevel().isClientSide);
                        if (event.getEntity() instanceof ServerPlayer player) {
                            MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(player, key, event.getTarget() instanceof LivingEntity ? MiraculousUsePowerTrigger.Context.LIVING_ENTITY : MiraculousUsePowerTrigger.Context.ENTITY);
                        }
                    }
                }
            }
        });
        event.getEntity().getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
            ResourceKey<Kamikotization> key = data.kamikotization();
            Kamikotization kamikotization = event.getEntity().level().holderOrThrow(key).value();
            AtomicBoolean overrideActive = new AtomicBoolean(false);
            kamikotization.passiveAbilities().forEach(ability -> {
                if (ability.value().perform(new AbilityData(0, Either.right(key)), event.getEntity().level(), event.getEntity().blockPosition(), event.getEntity(), Ability.Context.from(event.getTarget())) && ability.value().overrideActive())
                    overrideActive.set(true);
            });
            if (data.mainPowerActive()) {
                if (!overrideActive.get()) {
                    boolean usedPower = kamikotization.activeAbility().isPresent() && kamikotization.activeAbility().get().value().perform(new AbilityData(0, Either.right(key)), event.getEntity().level(), event.getEntity().blockPosition(), event.getEntity(), Ability.Context.from(event.getTarget()));
                    if (usedPower) {
                        data.withMainPowerActive(false).save(event.getEntity(), !event.getEntity().level().isClientSide);
                        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                            MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(serverPlayer, key, event.getEntity() instanceof LivingEntity livingEntity ? KamikotizationUsePowerTrigger.Context.LIVING_ENTITY : KamikotizationUsePowerTrigger.Context.ENTITY);
                        }
                    }
                } else
                    data.withMainPowerActive(false).save(event.getEntity(), !event.getLevel().isClientSide);
            }
        });
    }

    public static void onAttackEntity(AttackEntityEvent event) {
        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
            Miraculous miraculous = event.getEntity().level().holderOrThrow(key).value();
            MiraculousData data = event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
            AtomicBoolean overrideActive = new AtomicBoolean(false);
            miraculous.passiveAbilities().forEach(ability -> {
                if (ability.value().perform(new AbilityData(data.powerLevel(), Either.left(key)), event.getEntity().level(), event.getEntity().blockPosition(), event.getEntity(), Ability.Context.from(event.getTarget())) && ability.value().overrideActive())
                    overrideActive.set(true);
            });
            if (data.mainPowerActive()) {
                if (overrideActive.get()) {
                    event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, data.withPowerStatus(false, false), !event.getEntity().level().isClientSide);
                } else {
                    boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(new AbilityData(data.powerLevel(), Either.left(key)), event.getEntity().level(), event.getEntity().blockPosition(), event.getEntity(), Ability.Context.from(event.getTarget()));
                    if (usedPower) {
                        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, data.withUsedPower(), !event.getEntity().level().isClientSide);
                        if (event.getEntity() instanceof ServerPlayer player) {
                            MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(player, key, event.getTarget() instanceof LivingEntity ? MiraculousUsePowerTrigger.Context.LIVING_ENTITY : MiraculousUsePowerTrigger.Context.ENTITY);
                        }
                    }
                }
            }
        });
        event.getEntity().getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
            ResourceKey<Kamikotization> key = data.kamikotization();
            Kamikotization kamikotization = event.getEntity().level().holderOrThrow(key).value();
            AtomicBoolean overrideActive = new AtomicBoolean(false);
            kamikotization.passiveAbilities().forEach(ability -> {
                if (ability.value().perform(new AbilityData(0, Either.right(key)), event.getEntity().level(), event.getEntity().blockPosition(), event.getEntity(), Ability.Context.from(event.getTarget())) && ability.value().overrideActive())
                    overrideActive.set(true);
            });
            if (data.mainPowerActive()) {
                if (!overrideActive.get()) {
                    boolean usedPower = kamikotization.activeAbility().isPresent() && kamikotization.activeAbility().get().value().perform(new AbilityData(0, Either.right(key)), event.getEntity().level(), event.getEntity().blockPosition(), event.getEntity(), Ability.Context.from(event.getTarget()));
                    if (usedPower) {
                        data.withMainPowerActive(false).save(event.getEntity(), !event.getEntity().level().isClientSide);
                        if (event.getEntity() instanceof ServerPlayer player) {
                            MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(player, key, event.getTarget() instanceof LivingEntity ? KamikotizationUsePowerTrigger.Context.LIVING_ENTITY : KamikotizationUsePowerTrigger.Context.ENTITY);
                        }
                    }
                } else
                    data.withMainPowerActive(false).save(event.getEntity(), !event.getEntity().level().isClientSide);
            }
        });
    }

    public static void onLivingAttack(LivingDamageEvent.Post event) {
        Entity attacker = event.getSource().getDirectEntity();
        LivingEntity victim = event.getEntity();
        if (attacker instanceof LivingEntity livingEntity) {
            livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
                Miraculous miraculous = livingEntity.level().holderOrThrow(key).value();
                MiraculousData data = livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                miraculous.passiveAbilities().forEach(ability -> {
                    if (ability.value().perform(new AbilityData(data.powerLevel(), Either.left(key)), livingEntity.level(), livingEntity.blockPosition(), livingEntity, Ability.Context.from(victim)) && ability.value().overrideActive())
                        overrideActive.set(true);
                });
                if (data.mainPowerActive()) {
                    if (overrideActive.get()) {
                        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, data.withPowerStatus(false, false), !event.getEntity().level().isClientSide);
                    } else {
                        boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(new AbilityData(data.powerLevel(), Either.left(key)), livingEntity.level(), livingEntity.blockPosition(), livingEntity, Ability.Context.from(victim));
                        if (usedPower) {
                            event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, data.withUsedPower(), !event.getEntity().level().isClientSide);
                            if (event.getEntity() instanceof ServerPlayer player) {
                                MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(player, key, MiraculousUsePowerTrigger.Context.LIVING_ENTITY);
                            }
                        }
                    }
                }
            });
            livingEntity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                ResourceKey<Kamikotization> key = data.kamikotization();
                Kamikotization kamikotization = livingEntity.level().holderOrThrow(key).value();
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                kamikotization.passiveAbilities().forEach(ability -> {
                    if (ability.value().perform(new AbilityData(0, Either.right(key)), livingEntity.level(), livingEntity.blockPosition(), livingEntity, Ability.Context.from(victim)) && ability.value().overrideActive())
                        overrideActive.set(true);
                });
                if (data.mainPowerActive()) {
                    if (!overrideActive.get()) {
                        boolean usedPower = kamikotization.activeAbility().isPresent() && kamikotization.activeAbility().get().value().perform(new AbilityData(0, Either.right(key)), livingEntity.level(), livingEntity.blockPosition(), livingEntity, Ability.Context.from(victim));
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

    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
            Miraculous miraculous = event.getEntity().level().holderOrThrow(key).value();
            MiraculousData data = event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
            AtomicBoolean overrideActive = new AtomicBoolean(false);
            miraculous.passiveAbilities().forEach(ability -> {
                if (ability.value().perform(new AbilityData(data.powerLevel(), Either.left(key)), event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getLevel().getBlockState(event.getPos()), event.getPos())) && ability.value().overrideActive())
                    overrideActive.set(true);
            });
            if (data.mainPowerActive()) {
                if (overrideActive.get()) {
                    event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, data.withPowerStatus(false, false), !event.getLevel().isClientSide);
                } else {
                    boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(new AbilityData(data.powerLevel(), Either.left(key)), event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getLevel().getBlockState(event.getPos()), event.getPos()));
                    if (usedPower) {
                        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, data.withUsedPower(), !event.getLevel().isClientSide);
                        if (event.getEntity() instanceof ServerPlayer player) {
                            MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(player, key, MiraculousUsePowerTrigger.Context.BLOCK);
                        }
                    }
                }
            }
        });
        event.getEntity().getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
            ResourceKey<Kamikotization> key = data.kamikotization();
            Kamikotization kamikotization = event.getEntity().level().holderOrThrow(key).value();
            AtomicBoolean overrideActive = new AtomicBoolean(false);
            kamikotization.passiveAbilities().forEach(ability -> {
                if (ability.value().perform(new AbilityData(0, Either.right(key)), event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getLevel().getBlockState(event.getPos()), event.getPos())) && ability.value().overrideActive())
                    overrideActive.set(true);
            });
            if (data.mainPowerActive()) {
                if (!overrideActive.get()) {
                    boolean usedPower = kamikotization.activeAbility().isPresent() && kamikotization.activeAbility().get().value().perform(new AbilityData(0, Either.right(key)), event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getLevel().getBlockState(event.getPos()), event.getPos()));
                    if (usedPower) {
                        data.withMainPowerActive(false).save(event.getEntity(), true);
                        if (event.getEntity() instanceof ServerPlayer player) {
                            MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(player, key, KamikotizationUsePowerTrigger.Context.BLOCK);
                        }
                    }
                } else
                    data.withMainPowerActive(false).save(event.getEntity(), !event.getEntity().level().isClientSide);
            }
        });
    }

    public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
            Miraculous miraculous = event.getEntity().level().holderOrThrow(key).value();
            MiraculousData data = event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
            AtomicBoolean overrideActive = new AtomicBoolean(false);
            miraculous.passiveAbilities().forEach(ability -> {
                if (ability.value().perform(new AbilityData(data.powerLevel(), Either.left(key)), event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getLevel().getBlockState(event.getPos()), event.getPos())) && ability.value().overrideActive())
                    overrideActive.set(true);
            });
            if (data.mainPowerActive()) {
                if (overrideActive.get()) {
                    event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, data.withPowerStatus(false, false), !event.getLevel().isClientSide);
                } else {
                    boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(new AbilityData(data.powerLevel(), Either.left(key)), event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getLevel().getBlockState(event.getPos()), event.getPos()));
                    if (usedPower) {
                        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, data.withUsedPower(), !event.getLevel().isClientSide);
                        if (event.getEntity() instanceof ServerPlayer player) {
                            MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(player, key, MiraculousUsePowerTrigger.Context.BLOCK);
                        }
                    }
                }
            }
        });
        event.getEntity().getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
            ResourceKey<Kamikotization> key = data.kamikotization();
            Kamikotization kamikotization = event.getEntity().level().holderOrThrow(key).value();
            AtomicBoolean overrideActive = new AtomicBoolean(false);
            kamikotization.passiveAbilities().forEach(ability -> {
                if (ability.value().perform(new AbilityData(0, Either.right(key)), event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getLevel().getBlockState(event.getPos()), event.getPos())) && ability.value().overrideActive())
                    overrideActive.set(true);
            });
            if (data.mainPowerActive()) {
                if (!overrideActive.get()) {
                    boolean usedPower = kamikotization.activeAbility().isPresent() && kamikotization.activeAbility().get().value().perform(new AbilityData(0, Either.right(key)), event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getLevel().getBlockState(event.getPos()), event.getPos()));
                    if (usedPower) {
                        data.withMainPowerActive(false).save(event.getEntity(), !event.getEntity().level().isClientSide);
                        if (event.getEntity() instanceof ServerPlayer player) {
                            MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(player, key, KamikotizationUsePowerTrigger.Context.BLOCK);
                        }
                    }
                } else
                    data.withMainPowerActive(false).save(event.getEntity(), !event.getEntity().level().isClientSide);
            }
        });
    }

    public static void onEmptyLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
            Miraculous miraculous = event.getEntity().level().holderOrThrow(key).value();
            MiraculousData data = event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
            AtomicBoolean overrideActive = new AtomicBoolean(false);
            miraculous.passiveAbilities().forEach(ability -> {
                if (ability.value().perform(new AbilityData(data.powerLevel(), Either.left(key)), event.getEntity().level(), event.getEntity().blockPosition(), event.getEntity(), Ability.Context.from()) && ability.value().overrideActive())
                    overrideActive.set(true);
            });
            if (data.mainPowerActive()) {
                if (overrideActive.get()) {
                    event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, data.withPowerStatus(false, false), !event.getLevel().isClientSide);
                } else {
                    boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(new AbilityData(data.powerLevel(), Either.left(key)), event.getEntity().level(), event.getEntity().blockPosition(), event.getEntity(), Ability.Context.from());
                    if (usedPower) {
                        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, data.withUsedPower(), !event.getLevel().isClientSide);
                        if (event.getEntity() instanceof ServerPlayer player) {
                            MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(player, key, MiraculousUsePowerTrigger.Context.EMPTY);
                        }
                    }
                }
            }
        });
        event.getEntity().getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
            ResourceKey<Kamikotization> key = data.kamikotization();
            Kamikotization kamikotization = event.getEntity().level().holderOrThrow(key).value();
            AtomicBoolean overrideActive = new AtomicBoolean(false);
            kamikotization.passiveAbilities().forEach(ability -> {
                if (ability.value().perform(new AbilityData(0, Either.right(key)), event.getEntity().level(), event.getEntity().blockPosition(), event.getEntity(), Ability.Context.from()) && ability.value().overrideActive())
                    overrideActive.set(true);
            });
            if (data.mainPowerActive()) {
                if (!overrideActive.get()) {
                    boolean usedPower = kamikotization.activeAbility().isPresent() && kamikotization.activeAbility().get().value().perform(new AbilityData(0, Either.right(key)), event.getEntity().level(), event.getEntity().blockPosition(), event.getEntity(), Ability.Context.from());
                    if (usedPower) {
                        data.withMainPowerActive(false).save(event.getEntity(), !event.getEntity().level().isClientSide);
                        if (event.getEntity() instanceof ServerPlayer player) {
                            MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(player, key, KamikotizationUsePowerTrigger.Context.EMPTY);
                        }
                    }
                } else
                    data.withMainPowerActive(false).save(event.getEntity(), !event.getEntity().level().isClientSide);
            }
        });
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
                Style newStyle = style.withColor(entity.level().holderOrThrow(MineraculousMiraculous.BUTTERFLY).value().color());
                return Component.literal(data.name()).setStyle(newStyle);
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
                event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed(event.getLevel().registryAccess()).forEach(miraculous -> {
                    if (miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value() instanceof NightVisionAbility nightVisionAbility) {
                        nightVisionAbility.resetNightVision(player);
                    }
                    miraculous.passiveAbilities().stream().filter(ability -> ability.value() instanceof NightVisionAbility).map(ability -> (NightVisionAbility) ability.value()).forEach(nightVisionAbility -> nightVisionAbility.resetNightVision(player));
                });

                MiraculousDataSet miraculousDataSet = player.getData(MineraculousAttachmentTypes.MIRACULOUS);
                for (ResourceKey<Miraculous> miraculous : miraculousDataSet.keySet()) {
                    String look = miraculousDataSet.get(miraculous).suitLook();
                    if (!look.isEmpty())
                        TommyLibServices.NETWORK.sendToClient(new ClientboundRequestSyncSuitLookPayload(Optional.empty(), false, miraculous, look), player);
                    look = miraculousDataSet.get(miraculous).miraculousLook();
                    if (!look.isEmpty())
                        TommyLibServices.NETWORK.sendToClient(new ClientboundRequestSyncMiraculousLookPayload(Optional.empty(), false, miraculous, look), player);
                }

                if (player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
                    TommyLibServices.NETWORK.sendToClient(new ClientboundRequestSyncKamikotizationLookPayload(player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).get().kamikotization()), player);
                }
            }
        }
    }

    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            FlattenedLookDataHolder flattenedLookDataHolder = (FlattenedLookDataHolder) serverPlayer.getServer().overworld();
            flattenedLookDataHolder.mineraculous$getSuitLookData().remove(serverPlayer.getUUID());
            flattenedLookDataHolder.mineraculous$getMiraculousLookData().remove(serverPlayer.getUUID());
            flattenedLookDataHolder.mineraculous$getKamikotizationLookData().remove(serverPlayer.getUUID());
        }
    }

    public static void handleKamikotizationTransformation(ServerPlayer player, KamikotizationData data, boolean transform, boolean instant, boolean itemBroken, Vec3 kamikoSpawnPos) {
        if (player != null) {
            ServerLevel serverLevel = player.serverLevel();
            ItemStack kamikotizationStack = data.kamikotizedStack();
            int transformationFrames = 10;
            if (transform) {
                // Transform
                if (player.getData(MineraculousAttachmentTypes.MIRACULOUS).isTransformed()) {
                    return;
                }
                kamikotizationStack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);
                kamikotizationStack.set(MineraculousDataComponents.KAMIKO_DATA.get(), data.kamikoData());
                kamikotizationStack.set(MineraculousDataComponents.KAMIKOTIZATION, data.kamikotization());
                kamikotizationStack.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));

                if (!instant) {
                    kamikotizationStack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, transformationFrames);
                } else {
                    kamikotizationStack.remove(MineraculousDataComponents.TRANSFORMATION_FRAMES);
                    ArmorData armor = new ArmorData(player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST), player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET));
                    player.setData(MineraculousAttachmentTypes.STORED_ARMOR, Optional.of(armor));
                    for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                        ItemStack stack = Kamikotization.createItemStack(MineraculousArmors.KAMIKOTIZATION.getForSlot(slot).get(), data.kamikotization());
                        stack.enchant(player.level().holderOrThrow(Enchantments.BINDING_CURSE), 1);
                        stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);
                        player.setItemSlot(slot, stack);
                    }
                    for (int i = 0; i <= 10; i++) {
                        player.serverLevel().sendParticles(MineraculousParticleTypes.KAMIKOTIZATION.get(), player.getX(), player.getY() + 2 - (11 - transformationFrames) / 5.0, player.getZ(), 100, Math.random() / 3.0, Math.random() / 3.0, Math.random() / 3.0, 0);
                    }
                }

                data = data.withKamikotizationStack(kamikotizationStack);
                data.save(player, true);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), MineraculousSoundEvents.KAMIKOTIZATION_TRANSFORM, SoundSource.PLAYERS, 1, 1);
                if (data.slotInfo().left().isPresent()) {
                    player.getInventory().setItem(data.slotInfo().left().get(), kamikotizationStack);
                } else {
                    CuriosUtils.setStackInSlot(player, data.slotInfo().right().get(), kamikotizationStack, true);
                }
                MIRACULOUS_EFFECTS.forEach(effect -> player.addEffect(INFINITE_HIDDEN_EFFECT.apply(effect, 0)));
                KamikotizationData finalData = data;
                serverLevel.holderOrThrow(data.kamikotization()).value().activeAbility().ifPresent(ability -> ability.value().transform(new AbilityData(0, Either.right(finalData.kamikotization())), serverLevel, player.blockPosition(), player));
                serverLevel.holderOrThrow(data.kamikotization()).value().passiveAbilities().forEach(ability -> ability.value().transform(new AbilityData(0, Either.right(finalData.kamikotization())), serverLevel, player.blockPosition(), player));
            } else {
                // De-transform
                Kamiko kamiko = summonKamiko(player.level(), data, player, kamikoSpawnPos);
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
                    if (itemBroken)
                        player.getItemBySlot(EquipmentSlot.HEAD).set(MineraculousDataComponents.DETRANSFORMATION_FRAMES, transformationFrames);
                    else
                        kamikotizationStack.set(MineraculousDataComponents.DETRANSFORMATION_FRAMES, transformationFrames);
                } else {
                    if (itemBroken)
                        player.getItemBySlot(EquipmentSlot.HEAD).remove(MineraculousDataComponents.DETRANSFORMATION_FRAMES);
                    else
                        kamikotizationStack.remove(MineraculousDataComponents.DETRANSFORMATION_FRAMES);
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
                    CuriosUtils.setStackInSlot(player, data.slotInfo().right().get(), kamikotizationStack, true);
                }
                data = data.withKamikotizationStack(kamikotizationStack);
                data.save(player, true);
                serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(), MineraculousSoundEvents.KAMIKOTIZATION_DETRANSFORM, SoundSource.PLAYERS, 1, 1);
                MIRACULOUS_EFFECTS.forEach(player::removeEffect);
                KamikotizationData finalData1 = data;
                serverLevel.holderOrThrow(data.kamikotization()).value().activeAbility().ifPresent(ability -> ability.value().detransform(new AbilityData(0, Either.right(finalData1.kamikotization())), serverLevel, player.blockPosition(), player));
                serverLevel.holderOrThrow(data.kamikotization()).value().passiveAbilities().forEach(ability -> ability.value().detransform(new AbilityData(0, Either.right(finalData1.kamikotization())), serverLevel, player.blockPosition(), player));
                CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(player);
                entityData.putBoolean(TAG_SHOW_KAMIKO_MASK, false);
                TommyLibServices.ENTITY.setPersistentData(player, entityData, true);
            }
        }
    }

    public static Kamiko summonKamiko(Level level, KamikotizationData data, Player player, Vec3 kamikoSpawnPos) {
        if (data.kamikotizedStack().has(MineraculousDataComponents.KAMIKOTIZATION)) {
            Kamiko kamiko = MineraculousEntityTypes.KAMIKO.get().create(level);
            if (kamiko != null) {
                KamikoData kamikoData = data.kamikotizedStack().get(MineraculousDataComponents.KAMIKO_DATA);
                if (kamikoData != null) {
                    kamiko.setUUID(kamikoData.uuid());
                    kamiko.setOwnerUUID(kamikoData.owner());
                }
                kamiko.setPos(kamikoSpawnPos);
                level.addFreshEntity(kamiko);

                data.kamikotizedStack().remove(MineraculousDataComponents.KAMIKO_DATA);
                if (data.slotInfo().left().isPresent()) {
                    player.getInventory().setItem(data.slotInfo().left().get(), data.kamikotizedStack());
                } else {
                    CuriosUtils.setStackInSlot(player, data.slotInfo().right().get(), data.kamikotizedStack(), true);
                }
                return kamiko;
            }
        }
        return null;
    }

    public static void onPlayerBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack mainHandItem = event.getEntity().getMainHandItem();
        if (mainHandItem.is(MineraculousItems.LADYBUG_YOYO.get()) && mainHandItem.has(MineraculousDataComponents.ACTIVE)) {
            event.setCanceled(true);
        }
    }
}
