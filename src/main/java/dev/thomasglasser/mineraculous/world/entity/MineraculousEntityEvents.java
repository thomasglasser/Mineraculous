package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.gui.screens.inventory.ExternalCuriosInventoryScreen;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ClientboundMiraculousTransformPayload;
import dev.thomasglasser.mineraculous.network.ServerboundRequestInventorySyncPayload;
import dev.thomasglasser.mineraculous.network.ServerboundRequestMiraculousDataSetSyncPayload;
import dev.thomasglasser.mineraculous.network.ServerboundTryBreakItemPayload;
import dev.thomasglasser.mineraculous.network.ServerboundWakeUpPayload;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.NightVisionAbility;
import dev.thomasglasser.mineraculous.world.entity.npc.MineraculousVillagerTrades;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

public class MineraculousEntityEvents {
    public static final String TAG_WAITTICKS = "WaitTicks";
    public static final String TAG_HASNIGHTVISION = "HasNightVision";
    public static final String TAG_TAKETICKS = "TakeTicks";
    public static final String TAG_CATACLYSMED = "Cataclysmed";

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

        if (player.level().isClientSide && entityData.getInt(TAG_WAITTICKS) == 0) {
            int takeTicks = entityData.getInt(MineraculousEntityEvents.TAG_TAKETICKS);
            if (MineraculousKeyMappings.TAKE_BREAK_ITEM.isDown()) {
                ItemStack mainHandItem = player.getMainHandItem();
                if (mainHandItem.isEmpty()) {
                    if (MineraculousClientUtils.getLookEntity() instanceof Player target && (MineraculousServerConfig.INSTANCE.enableUniversalStealing.get() || /*TODO: Is akumatized*/ player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).isTransformed()) && (MineraculousServerConfig.INSTANCE.enableSleepStealing.get() || !target.isSleeping())) {
                        entityData.putInt(MineraculousEntityEvents.TAG_TAKETICKS, ++takeTicks);
                        if (target.isSleeping() && MineraculousServerConfig.INSTANCE.wakeUpChance.get() > 0 && (MineraculousServerConfig.INSTANCE.wakeUpChance.get() >= 100 || player.getRandom().nextFloat() < MineraculousServerConfig.INSTANCE.wakeUpChance.get() / (20f * 5 * 100))) {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundWakeUpPayload(target.getUUID(), true));
                        }
                        if (takeTicks > (20 * MineraculousServerConfig.INSTANCE.stealingDuration.get())) {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundRequestInventorySyncPayload(target.getUUID()));
                            ClientUtils.setScreen(new ExternalCuriosInventoryScreen(target));
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
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS.get());
        if (entity instanceof ServerPlayer player) {
            miraculousDataSet.keySet().forEach(miraculous -> {
                MiraculousData data = miraculousDataSet.get(miraculous);
                if (data.transformed())
                    handleTransformation(player, miraculous, data, false);
                KwamiData kwamiData = data.miraculousItem().get(MineraculousDataComponents.KWAMI_DATA.get());
                if (kwamiData != null && player.serverLevel().getEntity(kwamiData.uuid()) instanceof Kwami kwami) {
                    renounceMiraculous(data.miraculousItem(), kwami);
                }
            });
        }
        if (entity.hasEffect(MineraculousMobEffects.CATACLYSMED) && TommyLibServices.ENTITY.getPersistentData(entity).hasUUID(TAG_CATACLYSMED)) {
            entity.setLastHurtByPlayer(entity.level().getPlayerByUUID(TommyLibServices.ENTITY.getPersistentData(entity).getUUID(TAG_CATACLYSMED)));
        }
    }

    public static void handleTransformation(ServerPlayer player, ResourceKey<Miraculous> miraculous, MiraculousData data, boolean transform) {
        if (player != null) {
            ServerLevel serverLevel = player.serverLevel();
            ItemStack miraculousStack = data.miraculousItem();
            if (transform) {
                // Transform
                KwamiData kwamiData = miraculousStack.get(MineraculousDataComponents.KWAMI_DATA.get());
                Entity entity = serverLevel.getEntity(kwamiData.uuid());
                if (entity instanceof Kwami kwami) {
                    if (kwami.isCharged() && miraculousStack.getItem() instanceof MiraculousItem miraculousItem) {
                        // TODO: Sound
//						level.playSound(null, player.getX(), player.getY(), player.getZ(), transformSound, SoundSource.PLAYERS, 1, 1);

                        ArmorData armor = new ArmorData(player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST), player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET));
                        entity.setData(MineraculousAttachmentTypes.STORED_ARMOR, armor);
                        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
                            ItemStack stack = MineraculousArmors.MIRACULOUS.getForSlot(slot).get().getDefaultInstance();
                            stack.enchant(serverLevel.holderOrThrow(Enchantments.BINDING_CURSE), 1);
                            stack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);
                            stack.set(MineraculousDataComponents.MIRACULOUS, miraculous);
                            player.setItemSlot(slot, stack);
                        }

                        miraculousStack.enchant(serverLevel.holderOrThrow(Enchantments.BINDING_CURSE), 1);
                        miraculousStack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);

                        ItemStack tool = serverLevel.holderOrThrow(miraculousStack.get(MineraculousDataComponents.MIRACULOUS)).value().tool();
                        tool.set(MineraculousDataComponents.KWAMI_DATA.get(), new KwamiData(kwami.getUUID(), kwami.isCharged()));
                        miraculousStack.set(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE);
                        data = new MiraculousData(true, miraculousStack, data.curiosData(), tool, data.powerLevel(), false, false, data.name(), data.look());
                        player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).put(player, miraculous, data, true);
                        CuriosUtils.setStackInSlot(player, data.curiosData(), miraculousStack, true);
                        TommyLibServices.NETWORK.sendToAllClients(new ClientboundMiraculousTransformPayload(miraculous, data), serverLevel.getServer());
                        int powerLevel = data.powerLevel();
                        MIRACULOUS_EFFECTS.forEach(effect -> player.addEffect(INFINITE_HIDDEN_EFFECT.apply(effect, powerLevel)));
                        player.addItem(tool);
                        kwami.discard();
                        // TODO: Advancement trigger with miraculous item context
                        MiraculousData finalData = data;
                        serverLevel.holderOrThrow(miraculous).value().passiveAbilities().forEach(ability -> ability.value().transform(miraculous, finalData, serverLevel, player.blockPosition(), player));
                    } else {
                        // TODO: Hungry sound
//						kwami.playSound(kwami.getHungrySound());
                    }
                } else {
                    miraculousStack.remove(MineraculousDataComponents.KWAMI_DATA.get());
                    CuriosUtils.setStackInSlot(player, data.curiosData(), miraculousStack, true);
                    player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).put(player, miraculous, new MiraculousData(true, miraculousStack, data.curiosData(), data.tool(), data.powerLevel(), false, false, data.name(), data.look()), true);
                }
            } else {
                // De-transform
                Kwami kwami = summonKwami(player.level(), miraculous, data, player);
                if (kwami != null) {
                    kwami.setCharged(false);
                } else {
                    Mineraculous.LOGGER.error("Kwami could not be created for player " + player.getName().plainCopy().getString());
                    return;
                }
                ArmorData armor = player.getData(MineraculousAttachmentTypes.STORED_ARMOR);
                for (EquipmentSlot slot : Arrays.stream(EquipmentSlot.values()).filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR).toArray(EquipmentSlot[]::new)) {
                    player.setItemSlot(slot, armor.forSlot(slot));
                }
                miraculousStack.remove(DataComponents.ENCHANTMENTS);
                miraculousStack.remove(MineraculousDataComponents.REMAINING_TICKS.get());
                miraculousStack.remove(MineraculousDataComponents.POWERED.get());
                CuriosUtils.setStackInSlot(player, data.curiosData(), miraculousStack, true);
                // TODO: If item not in inventory, make it disappear when found, in item entity or chest or something
                data.tool().setCount(1);
                data.tool().set(MineraculousDataComponents.RECALLED.get(), true);
                if (data.tool().has(MineraculousDataComponents.KWAMI_DATA.get())) {
                    MiraculousData finalData = data;
                    player.getInventory().clearOrCountMatchingItems(stack -> {
                        if (stack.has(MineraculousDataComponents.KWAMI_DATA.get()))
                            return stack.get(MineraculousDataComponents.KWAMI_DATA.get()).uuid().equals(finalData.tool().get(MineraculousDataComponents.KWAMI_DATA.get()).uuid());
                        return false;
                    }, 1, new SimpleContainer(data.tool()));
                }
                data = new MiraculousData(false, miraculousStack, data.curiosData(), ItemStack.EMPTY, data.powerLevel(), false, false, data.name(), data.look());
                player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).put(player, miraculous, data, true);
                TommyLibServices.NETWORK.sendToAllClients(new ClientboundMiraculousTransformPayload(miraculous, data), serverLevel.getServer());
                MIRACULOUS_EFFECTS.forEach(player::removeEffect);
                MiraculousData finalData = data;
                serverLevel.holderOrThrow(miraculous).value().passiveAbilities().forEach(ability -> ability.value().detransform(miraculous, finalData, serverLevel, player.blockPosition(), player));
            }
        }
    }

    public static boolean renounceMiraculous(ItemStack miraculous, Kwami kwami) {
        KwamiData kwamiData = miraculous.get(MineraculousDataComponents.KWAMI_DATA.get());
        if (kwamiData != null && kwami.getUUID().equals(kwamiData.uuid())) {
            miraculous.set(MineraculousDataComponents.KWAMI_DATA.get(), new KwamiData(kwami.getUUID(), kwami.isCharged()));
            miraculous.set(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE);
            kwami.discard();
            // TODO: Play kwami hiding sound
            return true;
        }
        return false;
    }

    public static Kwami summonKwami(Level level, ResourceKey<Miraculous> miraculous, MiraculousData miraculousData, Player player) {
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
                kwami.setPos(player.getX() + level.random.nextInt(3), player.getY() + 2, player.getZ() + +level.random.nextInt(3));
                kwami.tame(player);
                level.addFreshEntity(kwami);

                miraculousData.miraculousItem().set(MineraculousDataComponents.KWAMI_DATA.get(), new KwamiData(kwami.getUUID(), kwami.isCharged()));
                CuriosUtils.setStackInSlot(player, miraculousData.curiosData(), miraculousData.miraculousItem(), true);
                player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).put(player, miraculous, new MiraculousData(false, miraculousData.miraculousItem(), miraculousData.curiosData(), miraculousData.tool(), miraculousData.powerLevel(), false, false, miraculousData.name(), miraculousData.look()), true);
                return kwami;
            }
        }
        return null;
    }

    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
            Miraculous miraculous = event.getEntity().level().holderOrThrow(key).value();
            MiraculousData data = event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
            miraculous.passiveAbilities().forEach(ability -> ability.value().perform(key, data, event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getTarget())));
            if (data.mainPowerActive()) {
                boolean usedPower = miraculous.activeAbility().value().perform(key, data, event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getTarget()));
                if (usedPower)
                    event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), data.powerLevel(), true, false, data.name(), data.look()), !event.getLevel().isClientSide);
            }
        });
    }

    public static void onAttackEntity(AttackEntityEvent event) {
        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
            Miraculous miraculous = event.getEntity().level().holderOrThrow(key).value();
            MiraculousData data = event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
            miraculous.passiveAbilities().forEach(ability -> ability.value().perform(key, data, event.getEntity().level(), event.getEntity().blockPosition(), event.getEntity(), Ability.Context.from(event.getTarget())));
            if (data.mainPowerActive()) {
                boolean usedPower = miraculous.activeAbility().value().perform(key, data, event.getEntity().level(), event.getEntity().blockPosition(), event.getEntity(), Ability.Context.from(event.getTarget()));
                if (usedPower)
                    event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), data.powerLevel(), true, false, data.name(), data.look()), !event.getEntity().level().isClientSide);
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
                miraculous.passiveAbilities().forEach(ability -> ability.value().perform(key, data, livingEntity.level(), livingEntity.blockPosition(), livingEntity, Ability.Context.from(victim)));
                if (data.mainPowerActive()) {
                    boolean usedPower = miraculous.activeAbility().value().perform(key, data, livingEntity.level(), livingEntity.blockPosition(), livingEntity, Ability.Context.from(victim));
                    if (usedPower)
                        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), data.powerLevel(), true, false, data.name(), data.look()), !event.getEntity().level().isClientSide);
                }
            });
        }
    }

    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
            Miraculous miraculous = event.getEntity().level().holderOrThrow(key).value();
            MiraculousData data = event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
            miraculous.passiveAbilities().forEach(ability -> ability.value().perform(key, data, event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getLevel().getBlockState(event.getPos()), event.getPos())));
            if (data.mainPowerActive()) {
                boolean usedPower = miraculous.activeAbility().value().perform(key, data, event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getLevel().getBlockState(event.getPos()), event.getPos()));
                if (usedPower)
                    event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), data.powerLevel(), true, false, data.name(), data.look()), !event.getLevel().isClientSide);
            }
        });
    }

    public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
            Miraculous miraculous = event.getEntity().level().holderOrThrow(key).value();
            MiraculousData data = event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
            miraculous.passiveAbilities().forEach(ability -> ability.value().perform(key, data, event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getLevel().getBlockState(event.getPos()), event.getPos())));
            if (data.mainPowerActive()) {
                boolean usedPower = miraculous.activeAbility().value().perform(key, data, event.getEntity().level(), event.getPos(), event.getEntity(), Ability.Context.from(event.getLevel().getBlockState(event.getPos()), event.getPos()));
                if (usedPower)
                    event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), data.powerLevel(), true, false, data.name(), data.look()), !event.getLevel().isClientSide);
            }
        });
    }

    public static void onEmptyLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed().forEach(key -> {
            Miraculous miraculous = event.getEntity().level().holderOrThrow(key).value();
            MiraculousData data = event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(key);
            miraculous.passiveAbilities().forEach(ability -> ability.value().perform(key, data, event.getEntity().level(), event.getEntity().blockPosition(), event.getEntity(), Ability.Context.from()));
            if (data.mainPowerActive()) {
                boolean usedPower = miraculous.activeAbility().value().perform(key, data, event.getEntity().level(), event.getEntity().blockPosition(), event.getEntity(), Ability.Context.from());
                if (usedPower)
                    event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUS).put(event.getEntity(), key, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), data.powerLevel(), true, false, data.name(), data.look()), !event.getLevel().isClientSide);
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
            if (transformed.size() > 1) {
                // TODO: Support for name and color based on combinations of miraculous, like Monarch being purple
            } else {
                MiraculousData data = miraculousDataSet.get(transformed.getFirst());
                if (data.miraculousItem().has(MineraculousDataComponents.MIRACULOUS)) {
                    Style newStyle = style.withColor(entity.level().holderOrThrow(data.miraculousItem().get(MineraculousDataComponents.MIRACULOUS)).value().color());
                    if (!data.name().isEmpty())
                        return Component.literal(data.name()).setStyle(newStyle);
                    return original.copy().setStyle(newStyle.withObfuscated(true));
                }
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
        if (MineraculousEntityEvents.isCataclysmed(event.getEntity()))
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
                    if (miraculous.activeAbility().value() instanceof NightVisionAbility nightVisionAbility) {
                        nightVisionAbility.resetNightVision(player);
                    }
                    miraculous.passiveAbilities().stream().filter(ability -> ability.value() instanceof NightVisionAbility).map(ability -> (NightVisionAbility) ability.value()).forEach(nightVisionAbility -> nightVisionAbility.resetNightVision(player));
                });
            }
        }
    }
}
