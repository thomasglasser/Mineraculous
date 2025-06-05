package dev.thomasglasser.mineraculous.world.entity;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.advancements.critereon.UseKamikotizationPowerTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.UseMiraculousPowerTrigger;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.datamaps.MineraculousDataMaps;
import dev.thomasglasser.mineraculous.network.ClientboundSyncMiraculousLookPayload;
import dev.thomasglasser.mineraculous.network.ClientboundSyncSpecialPlayerChoicesPayload;
import dev.thomasglasser.mineraculous.network.ClientboundSyncSuitLookPayload;
import dev.thomasglasser.mineraculous.network.ServerboundRequestMiraculousDataSetSyncPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSendEmptyLeftClickPayload;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.LuckyCharmIdData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityReversionBlockData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityReversionItemData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import dev.thomasglasser.mineraculous.world.level.storage.ServerLookData;
import dev.thomasglasser.mineraculous.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.mineraculous.world.level.storage.ToolIdData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
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
import org.jetbrains.annotations.Nullable;

public class MineraculousEntityEvents {
    public static final String ITEM_UNBREAKABLE_KEY = "mineraculous.item_unbreakable";

    public static final BiFunction<Holder<MobEffect>, Integer, MobEffectInstance> INFINITE_HIDDEN_EFFECT = (effect, amplifier) -> new MobEffectInstance(effect, -1, amplifier, false, false, false);

    // Entrance
    public static void onServerPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        TommyLibServices.NETWORK.sendToAllClients(ClientboundSyncSpecialPlayerChoicesPayload.INSTANCE, event.getEntity().getServer());
    }

    // Birth

    // Life
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel level) {
            checkBlockingComponent(entity);

            AbilityReversionEntityData.get(level).tick(entity);
            AbilityReversionItemData.get(level).tick(entity);
            ToolIdData.get(level).tick(entity);
            LuckyCharmIdData.get(level).tick(entity);

            entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> data.tick(entity, level));
        }
    }

    public static void checkBlockingComponent(Entity entity) {
        for (ItemStack stack : EntityUtils.getInventory(entity)) {
            if (entity instanceof LivingEntity livingEntity) {
                boolean blocking = livingEntity.isBlocking() && livingEntity.getUseItem() == stack;
                if (!blocking && stack.has(MineraculousDataComponents.BLOCKING))
                    stack.remove(MineraculousDataComponents.BLOCKING);
                else if (blocking && !stack.has(MineraculousDataComponents.BLOCKING))
                    stack.set(MineraculousDataComponents.BLOCKING, Unit.INSTANCE);
            } else if (stack.has(MineraculousDataComponents.BLOCKING)) {
                stack.remove(MineraculousDataComponents.BLOCKING);
            }
        }
    }

    // Death
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel level) {
            MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES.get());
            miraculousesData.forEach(miraculous -> {
                MiraculousData data = miraculousesData.get(miraculous);
                if (data.transformed()) {
                    handleMiraculousTransformation(entity, miraculous, data, false, true, false);
                }
                renounceMiraculous(data.miraculousItem(), level);
            });
            entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> handleKamikotizationTransformation(entity, data, false, true, entity.position().add(0, 1, 0)));
            entity.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).killCredit().ifPresent(killCredit -> {
                Entity killer = level.getEntity(killCredit);
                if (killer instanceof Player player) {
                    entity.setLastHurtByPlayer(player);
                } else if (killer instanceof LivingEntity livingEntity) {
                    entity.setLastHurtByMob(livingEntity);
                }
            });
        }
    }

    // Exit
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        Level level = event.getLevel();
        Entity entity = event.getEntity();
        if (!level.isClientSide) {
            entity.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO).id().ifPresent(id -> {
                Entity yoyo = event.getLevel().getEntity(id);
                if (yoyo != null)
                    yoyo.discard();
                new ThrownLadybugYoyoData().save(entity, true);
            });
        }
    }

    public static Optional<KwamiData> renounceMiraculous(ServerLevel level, ItemStack miraculousStack, Optional<KwamiData> kwamiData, Optional<Boolean> charged) {
        miraculousStack.set(MineraculousDataComponents.POWERED, Unit.INSTANCE);
        miraculousStack.remove(MineraculousDataComponents.REMAINING_TICKS);
        if (kwamiData.isPresent() && level.getEntity(kwamiData.get().uuid()) instanceof Kwami kwami) {
            KwamiData newData = new KwamiData(kwami.getUUID(), kwami.getId(), charged.orElse(kwami.isCharged()));
            miraculousStack.set(MineraculousDataComponents.KWAMI_DATA, newData);
            kwami.discard();
            return Optional.of(newData);
        }
        return Optional.empty();
    }

    public static Kwami summonKwami(ServerLevel level, ResourceKey<Miraculous> miraculous, MiraculousData miraculousData, Entity entity) {
        Kwami kwami = MineraculousEntityTypes.KWAMI.get().create(level);
        if (kwami != null) {
            kwami.setMiraculous(miraculous);
            KwamiData kwamiData = miraculousData.kwamiData().orElse(null);
            if (kwamiData != null) {
                kwami.setUUID(kwamiData.uuid());
                kwami.setCharged(kwamiData.charged());
            } else {
                kwami.setCharged(true);
            }
            Direction direction = entity.getDirection().getOpposite();
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
            kwami.teleportTo(level, entity.getX() + xOffset, entity.getY() + 1, entity.getZ() + zOffset, Set.of(), direction.toYRot(), 0.0F);
            if (entity instanceof Player player) {
                kwami.tame(player);
            } else {
                kwami.setOwnerUUID(entity.getUUID());
                kwami.setTame(true, true);
            }
            level.addFreshEntity(kwami);
            kwami.playSound(MineraculousSoundEvents.KWAMI_SUMMON.get());
            return kwami;
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
            player.getData(MineraculousAttachmentTypes.MIRACULOUSES).getTransformed().forEach(key -> {
                Miraculous miraculous = level.holderOrThrow(key).value();
                MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUSES.get()).get(key);
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                AbilityData abilityData = new AbilityData(data.powerLevel(), Either.left(key));
                miraculous.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                    if (ability.canActivate(abilityData, level, player, ) && ability.perform(abilityData, level, player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target)) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.powerActive()) {
                    if (overrideActive.get()) {
                        player.getData(MineraculousAttachmentTypes.MIRACULOUSES).put(player, key, data.withPowerStatus(false, false), true);
                    } else {
                        boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(abilityData, level, player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target));
                        if (usedPower) {
                            player.getData(MineraculousAttachmentTypes.MIRACULOUSES).put(player, key, data.withUsedPower(), true);
                            MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger((ServerPlayer) player, key, target instanceof LivingEntity ? UseMiraculousPowerTrigger.Context.LIVING_ENTITY : UseMiraculousPowerTrigger.Context.ENTITY);
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
                    if (ability.canActivate(abilityData, level, player, ) && ability.perform(abilityData, level, player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target)) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.powerActive()) {
                    if (!overrideActive.get()) {
                        boolean usedPower = kamikotization.powerSource().right().isPresent() && kamikotization.powerSource().right().get().value().perform(abilityData, level, player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target));
                        if (usedPower) {
                            data.withPowerActive(false).save(player, true);
                            if (player instanceof ServerPlayer serverPlayer) {
                                MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(serverPlayer, key, target instanceof LivingEntity ? UseKamikotizationPowerTrigger.Context.LIVING_ENTITY : UseKamikotizationPowerTrigger.Context.ENTITY);
                            }
                        }
                    } else
                        data.withPowerActive(false).save(player, true);
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
            player.getData(MineraculousAttachmentTypes.MIRACULOUSES).getTransformed().forEach(key -> {
                Miraculous miraculous = level.holderOrThrow(key).value();
                MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUSES.get()).get(key);
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                AbilityData abilityData = new AbilityData(data.powerLevel(), Either.left(key));
                miraculous.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                    if (ability.canActivate(abilityData, level, player, ) && ability.perform(abilityData, level, player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target)) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.powerActive()) {
                    if (overrideActive.get()) {
                        player.getData(MineraculousAttachmentTypes.MIRACULOUSES).put(player, key, data.withPowerStatus(false, false), true);
                    } else {
                        boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(abilityData, level, player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target));
                        if (usedPower) {
                            player.getData(MineraculousAttachmentTypes.MIRACULOUSES).put(player, key, data.withUsedPower(), true);
                            MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger((ServerPlayer) player, key, target instanceof LivingEntity ? UseMiraculousPowerTrigger.Context.LIVING_ENTITY : UseMiraculousPowerTrigger.Context.ENTITY);
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
                    if (ability.canActivate(abilityData, level, player, ) && ability.perform(abilityData, level, player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target)) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.powerActive()) {
                    if (!overrideActive.get()) {
                        boolean usedPower = kamikotization.powerSource().right().isPresent() && kamikotization.powerSource().right().get().value().perform(abilityData, level, player, blocked ? Ability.Context.from(livingTarget.getUseItem(), livingTarget) : Ability.Context.from(target));
                        if (usedPower) {
                            data.withPowerActive(false).save(player, true);
                            MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger((ServerPlayer) player, key, target instanceof LivingEntity ? UseKamikotizationPowerTrigger.Context.LIVING_ENTITY : UseKamikotizationPowerTrigger.Context.ENTITY);
                        }
                    } else
                        data.withPowerActive(false).save(player, true);
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
                livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUSES).getTransformed().forEach(key -> {
                    Miraculous miraculous = level.holderOrThrow(key).value();
                    MiraculousData data = livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUSES.get()).get(key);
                    AtomicBoolean overrideActive = new AtomicBoolean(false);
                    AbilityData abilityData = new AbilityData(data.powerLevel(), Either.left(key));
                    miraculous.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                        if (ability.canActivate(abilityData, level, livingEntity, ) && ability.perform(abilityData, level, livingEntity, blocked ? Ability.Context.from(victim.getUseItem(), victim) : Ability.Context.from(victim)) && ability.overrideActive())
                            overrideActive.set(true);
                    });
                    if (data.powerActive()) {
                        if (overrideActive.get()) {
                            event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUSES).put(event.getEntity(), key, data.withPowerStatus(false, false), true);
                        } else {
                            boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(abilityData, level, livingEntity, blocked ? Ability.Context.from(victim.getUseItem(), victim) : Ability.Context.from(victim));
                            if (usedPower) {
                                event.getEntity().getData(MineraculousAttachmentTypes.MIRACULOUSES).put(event.getEntity(), key, data.withUsedPower(), true);
                                if (event.getEntity() instanceof ServerPlayer player) {
                                    MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(player, key, UseMiraculousPowerTrigger.Context.LIVING_ENTITY);
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
                        if (ability.canActivate(abilityData, level, livingEntity, ) && ability.perform(abilityData, level, livingEntity, blocked ? Ability.Context.from(victim.getUseItem(), victim) : Ability.Context.from(victim)) && ability.overrideActive())
                            overrideActive.set(true);
                    });
                    if (data.powerActive()) {
                        if (!overrideActive.get()) {
                            boolean usedPower = kamikotization.powerSource().right().isPresent() && kamikotization.powerSource().right().get().value().perform(abilityData, level, livingEntity, blocked ? Ability.Context.from(victim.getUseItem(), victim) : Ability.Context.from(victim));
                            if (usedPower) {
                                data.withPowerActive(false).save(livingEntity, true);
                                if (event.getEntity() instanceof ServerPlayer player) {
                                    MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(player, key, UseKamikotizationPowerTrigger.Context.LIVING_ENTITY);
                                }
                            }
                        } else
                            data.withPowerActive(false).save(livingEntity, true);
                    }
                });
            }
//            CompoundTag data = TommyLibServices.ENTITY.getPersistentData(victim);
//            if (data.getBoolean(TAG_SHOW_KAMIKO_MASK)) {
//                data.putBoolean(TAG_CAMERA_CONTROL_INTERRUPTED, true);
//                TommyLibServices.ENTITY.setPersistentData(victim, data, true);
//            }
        }
    }

    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel() instanceof ServerLevel level) {
            Player player = event.getEntity();
            player.getData(MineraculousAttachmentTypes.MIRACULOUSES).getTransformed().forEach(key -> {
                Miraculous miraculous = level.holderOrThrow(key).value();
                MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUSES.get()).get(key);
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                AbilityData abilityData = new AbilityData(data.powerLevel(), Either.left(key));
                miraculous.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                    if (ability.canActivate(abilityData, level, player, ) && ability.perform(abilityData, level, player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos())) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.powerActive()) {
                    if (overrideActive.get()) {
                        player.getData(MineraculousAttachmentTypes.MIRACULOUSES).put(player, key, data.withPowerStatus(false, false), true);
                    } else {
                        boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(abilityData, level, player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos()));
                        if (usedPower) {
                            player.getData(MineraculousAttachmentTypes.MIRACULOUSES).put(player, key, data.withUsedPower(), true);
                            MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger((ServerPlayer) player, key, UseMiraculousPowerTrigger.Context.BLOCK);
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
                    if (ability.canActivate(abilityData, level, player, ) && ability.perform(abilityData, level, player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos())) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.powerActive()) {
                    if (!overrideActive.get()) {
                        boolean usedPower = kamikotization.powerSource().right().isPresent() && kamikotization.powerSource().right().get().value().perform(abilityData, level, player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos()));
                        if (usedPower) {
                            data.withPowerActive(false).save(player, true);
                            MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger((ServerPlayer) player, key, UseKamikotizationPowerTrigger.Context.BLOCK);
                        }
                    } else
                        data.withPowerActive(false).save(player, true);
                }
            });
        }
    }

    public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel() instanceof ServerLevel level) {
            Player player = event.getEntity();
            player.getData(MineraculousAttachmentTypes.MIRACULOUSES).getTransformed().forEach(key -> {
                Miraculous miraculous = level.holderOrThrow(key).value();
                MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUSES.get()).get(key);
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                AbilityData abilityData = new AbilityData(data.powerLevel(), Either.left(key));
                miraculous.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                    if (ability.canActivate(abilityData, level, player, ) && ability.perform(abilityData, level, player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos())) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.powerActive()) {
                    if (overrideActive.get()) {
                        player.getData(MineraculousAttachmentTypes.MIRACULOUSES).put(player, key, data.withPowerStatus(false, false), true);
                    } else {
                        boolean usedPower = miraculous.activeAbility().isPresent() && miraculous.activeAbility().get().value().perform(abilityData, level, player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos()));
                        if (usedPower) {
                            player.getData(MineraculousAttachmentTypes.MIRACULOUSES).put(player, key, data.withUsedPower(), true);
                            MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger((ServerPlayer) player, key, UseMiraculousPowerTrigger.Context.BLOCK);
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
                    if (ability.canActivate(abilityData, level, player, ) && ability.perform(abilityData, level, player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos())) && ability.overrideActive())
                        overrideActive.set(true);
                });
                if (data.powerActive()) {
                    if (!overrideActive.get()) {
                        boolean usedPower = kamikotization.powerSource().right().isPresent() && kamikotization.powerSource().right().get().value().perform(abilityData, level, player, Ability.Context.from(level.getBlockState(event.getPos()), event.getPos()));
                        if (usedPower) {
                            data.withPowerActive(false).save(player, true);
                            MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger((ServerPlayer) player, key, UseKamikotizationPowerTrigger.Context.BLOCK);
                        }
                    } else
                        data.withPowerActive(false).save(player, true);
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

    public static Component formatDisplayName(Entity entity, Component original) {
        if (original != null) {
            Style style = original.getStyle();
            MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            List<ResourceKey<Miraculous>> transformed = miraculousesData.getTransformed();
            if (!transformed.isEmpty()) {
                MiraculousData data = miraculousesData.get(transformed.getFirst());
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
        event.put(MineraculousEntityTypes.KWAMI.get(), Kwami.createAttributes().build());
        event.put(MineraculousEntityTypes.KAMIKO.get(), Kamiko.createAttributes().build());
    }

    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEffect() == MineraculousMobEffects.CATACLYSM && !(event.getEntity() instanceof Player player && player.isCreative()))
            event.setCanceled(true);
    }

    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity().hasEffect(MineraculousMobEffects.CATACLYSM))
            event.setCanceled(true);
    }

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.getLevel() instanceof ServerLevel level) {
            MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            for (ResourceKey<Miraculous> miraculous : miraculousesData.getTransformed()) {
                MiraculousData data = miraculousesData.get(miraculous);
                AbilityData abilityData = new AbilityData(data.powerLevel(), Either.left(miraculous), data.powerActive());
                for (Ability ability : Ability.getAll(level.holderOrThrow(miraculous).value(), true)) {
                    ability.joinLevel(abilityData, level, entity);
                }
            }
            entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                AbilityData abilityData = new AbilityData(0, Either.right(data.kamikotization()), data.powerActive());
                for (Ability ability : Ability.getAll(level.holderOrThrow(data.kamikotization()).value(), true)) {
                    ability.joinLevel(abilityData, level, entity);
                }
            });
        } else {
            TommyLibServices.NETWORK.sendToServer(new ServerboundRequestMiraculousDataSetSyncPayload(entity.getId()));
        }
    }

    public static void updateAndSyncSuitLook(ServerPlayer player, ResourceKey<Miraculous> miraculous, FlattenedSuitLookData data) {
        TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncSuitLookPayload(player.getUUID(), miraculous, data), player.getServer());
        MiraculousesData miraculousesData = player.getData(MineraculousAttachmentTypes.MIRACULOUSES);
        miraculousesData.put(player, miraculous, miraculousesData.get(miraculous).withSuitLook(data.look()), true);
    }

    public static void updateAndSyncMiraculousLook(ServerPlayer player, ResourceKey<Miraculous> miraculous, FlattenedMiraculousLookData data) {
        TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncMiraculousLookPayload(player.getUUID(), miraculous, data), player.getServer());
        MiraculousesData miraculousesData = player.getData(MineraculousAttachmentTypes.MIRACULOUSES);
        miraculousesData.put(player, miraculous, miraculousesData.get(miraculous).withMiraculousLook(data.look()), true);
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer serverPlayer = (ServerPlayer) event.getEntity();
        ServerLookData.getPlayerSuits().remove(serverPlayer.getUUID());
        ServerLookData.getPlayerMiraculouses().remove(serverPlayer.getUUID());
        ServerLookData.getPlayerKamikotizations().remove(serverPlayer.getUUID());
        serverPlayer.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO).id().ifPresent(id -> {
            Entity yoyo = serverPlayer.level().getEntity(id);
            if (yoyo != null)
                yoyo.discard();
            new ThrownLadybugYoyoData().save(serverPlayer, true);
        });
    }

    public static void handleKamikotizationTransformation(Entity entity, KamikotizationData data, boolean transform, boolean instant, Vec3 kamikoSpawnPos) {
        ServerLevel serverLevel = player.serverLevel();
        ItemStack originalStack = data.slotInfo().left().isPresent() ? player.getInventory().getItem(data.slotInfo().left().get()) : CuriosUtils.getStackInSlot(player, data.slotInfo().right().get());
        Kamikotization kamikotization = serverLevel.holderOrThrow(data.kamikotization()).value();
        ItemStack kamikotizationStack = originalStack.copy();
        int transformationFrames = 10;
        if (transform) {
            // Transform
            if (player.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed()) {
                return;
            }

            if (kamikotization.powerSource().left().isPresent())
                kamikotizationStack = kamikotization.powerSource().left().get().copyWithCount(Math.min(kamikotizationStack.getCount(), kamikotization.powerSource().left().get().getMaxStackSize()));

            data = data.withStackCount(kamikotizationStack.getCount());

            kamikotizationStack.set(MineraculousDataComponents.HIDE_ENCHANTMENTS.get(), Unit.INSTANCE);
            kamikotizationStack.set(MineraculousDataComponents.KAMIKO_DATA.get(), data.kamikoData());
            kamikotizationStack.set(MineraculousDataComponents.KAMIKOTIZATION, data.kamikotization());
            kamikotizationStack.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));

            AbilityReversionItemData.get(serverLevel).putKamikotized(player.getUUID(), originalStack);

            if (!instant) {
                data = data.withTransformationFrames(transformationFrames);
            } else {
                data = data.clearTransformationFrames();
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
            kamikotization.powerSource().right().ifPresent(ability -> ability.value().transform(new AbilityData(0, Either.right(finalData.kamikotization())), serverLevel, player));
            kamikotization.passiveAbilities().forEach(ability -> ability.value().transform(new AbilityData(0, Either.right(finalData.kamikotization())), serverLevel, player));
            AbilityReversionEntityData.get(serverLevel).startTracking(player.getUUID());
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
                data = data.clearTransformationFrames();
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
            kamikotization.powerSource().right().ifPresent(ability -> ability.value().detransform(new AbilityData(0, Either.right(finalData1.kamikotization())), serverLevel, player));
            kamikotization.passiveAbilities().forEach(ability -> ability.value().detransform(new AbilityData(0, Either.right(finalData1.kamikotization())), serverLevel, player));
//            CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(player);
//            entityData.putBoolean(TAG_SHOW_KAMIKO_MASK, false);
//            TommyLibServices.ENTITY.setPersistentData(player, entityData, true);
        }
        player.refreshDisplayName();
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

    public static void checkKamikotizationStack(ItemStack stack, ServerLevel level, @Nullable Entity breaker) {
        checkKamikotizationStack(stack, level, breaker != null ? breaker.position().add(0, 1, 0) : null);
    }

    public static void checkKamikotizationStack(ItemStack stack, ServerLevel level, @Nullable Vec3 kamikoPos) {
        ResolvableProfile profile = stack.get(DataComponents.PROFILE);
        if (stack.has(MineraculousDataComponents.KAMIKOTIZATION) && profile != null) {
            if (level.getPlayerByUUID(profile.id().orElse(profile.gameProfile().getId())) instanceof ServerPlayer target) {
                KamikotizationData data = target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).orElseThrow();
                if (data.stackCount() <= 1) {
                    MineraculousEntityEvents.handleKamikotizationTransformation(target, data, false, false, kamikoPos != null ? kamikoPos : target.position().add(0, 1, 0));
                } else {
                    data.decrementStackCount().save(target, true);
                }
            }
        }
    }

    public static void onPlayerBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack mainHandItem = event.getEntity().getMainHandItem();
        if (mainHandItem.is(MineraculousItems.LADYBUG_YOYO.get()) && mainHandItem.getOrDefault(MineraculousDataComponents.ACTIVE, false)) {
            event.setCanceled(true);
        }
    }

    public static void onBlockDrops(BlockDropsEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            UUID recoverer = AbilityReversionBlockData.get(serverLevel).getRecoverer(event.getPos());
            if (recoverer != null) {
                for (ItemEntity item : event.getDrops()) {
                    UUID id = UUID.randomUUID();
                    AbilityReversionItemData.get(serverLevel).putRemovable(recoverer, id);
                    item.getItem().set(MineraculousDataComponents.RECOVERABLE_ITEM_ID, id);
                }
            }
        }
    }

    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel serverLevel) {
            UUID recoverer = AbilityReversionEntityData.get(serverLevel).getRecoverer(entity, serverLevel);
            for (ItemEntity item : event.getDrops()) {
                if (entity.hasEffect(MineraculousMobEffects.CATACLYSM) ) {
                    item.setItem(convertToCataclysmDust(item.getItem()));
                }
                if (recoverer != null) {
                    UUID id = UUID.randomUUID();
                    AbilityReversionItemData.get(serverLevel).putRemovable(recoverer, id);
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

    public static Pair<ItemStack, ItemStack> tryBreakItem(ItemStack stack, ServerLevel serverLevel, Vec3 pos, @Nullable LivingEntity breaker) {
        ItemStack rest = stack.copyWithCount(stack.getCount() - 1);
        stack.setCount(1);
        if (!stack.isDamageableItem()) {
            if (stack.getItem() instanceof BlockItem blockItem) {
                float max = blockItem.getBlock().defaultDestroyTime();
                if (max > -1) {
                    stack.set(DataComponents.MAX_DAMAGE, (int) (max * 100));
                    stack.set(DataComponents.DAMAGE, 0);
                    stack.set(DataComponents.MAX_STACK_SIZE, 1);
                } else {
                    stack.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
                }
            } else if (stack.is(MineraculousItemTags.TOUGH)) {
                stack.set(DataComponents.MAX_DAMAGE, 200);
                stack.set(DataComponents.DAMAGE, 0);
                stack.set(DataComponents.MAX_STACK_SIZE, 1);
            }
        }
        if (stack.has(DataComponents.UNBREAKABLE)) {
            if (breaker instanceof Player player) {
                player.displayClientMessage(Component.translatable(ITEM_UNBREAKABLE_KEY), true);
            }
        } else {
            if (stack.isDamageableItem()) {
                int damage = 100;
                if (breaker != null) {
                    MiraculousesData data = breaker.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                    for (ResourceKey<Miraculous> type : data.getTransformed()) {
                        damage += 100 * data.get(type).powerLevel();
                    }
                }
                hurtAndBreak(stack, damage, serverLevel, breaker, EquipmentSlot.MAINHAND);
            } else {
                MineraculousEntityEvents.checkKamikotizationStack(stack, serverLevel, pos);
                stack.shrink(1);
                serverLevel.playSound(null, new BlockPos((int) pos.x, (int) pos.y, (int) pos.z), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1f, 1f);
            }
        }
        return Pair.of(stack, rest);
    }

    public static void hurtAndBreak(ItemStack stack, int damage, ServerLevel level, @Nullable LivingEntity breaker, @Nullable EquipmentSlot slot) {
        if (stack.isDamageableItem()) {
            Consumer<Item> itemConsumer = item -> {
                if (breaker != null && slot != null)
                    breaker.onEquippedItemBroken(item, slot);
            };
            damage = stack.getItem().damageItem(stack, damage, breaker, itemConsumer);

            if (damage > 0) {
                damage = EnchantmentHelper.processDurabilityChange(level, stack, damage);
                if (damage <= 0) {
                    return;
                }
            }

            if (breaker instanceof ServerPlayer serverPlayer) {
                if (damage != 0) {
                    CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(serverPlayer, stack, stack.getDamageValue() + damage);
                }
            }

            int newDamage = stack.getDamageValue() + damage;
            stack.setDamageValue(newDamage);
            if (newDamage >= stack.getMaxDamage()) {
                MineraculousEntityEvents.checkKamikotizationStack(stack, level, breaker);
                Item item = stack.getItem();
                stack.shrink(1);
                itemConsumer.accept(item);
            }
        }
    }

    public static void tryBreakItemEntity(EntityHitResult entityHitResult, ItemEntity itemEntity, ServerLevel serverLevel, Position pos) {
        Pair<ItemStack, ItemStack> result = MineraculousEntityEvents.tryBreakItem(itemEntity.getItem(), serverLevel, entityHitResult.getLocation(), null);
        ItemStack stack = result.getFirst();
        ItemStack rest = result.getSecond();
        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
        }
        if (!rest.isEmpty()) {
            ItemEntity restEntity = new ItemEntity(serverLevel, pos.x(), pos.y(), pos.z(), rest);
            serverLevel.addFreshEntity(restEntity);
        }
    }

}
