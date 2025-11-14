package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityData;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityUtils;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityEffectData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionItemData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.network.ClientboundSyncSpecialPlayerChoicesPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundEmptyLeftClickItemPayload;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.impl.world.level.storage.LuckyCharmIdData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTargetData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchingCatStaffData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ToolIdData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public class MineraculousEntityEvents {
    /// Registration
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(MineraculousEntityTypes.KWAMI.get(), Kwami.createAttributes().build());
        event.put(MineraculousEntityTypes.KAMIKO.get(), Kamiko.createAttributes().build());
    }

    /// Entrance
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.getLevel() instanceof ServerLevel level && entity instanceof LivingEntity livingEntity) {
            MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            miraculousesData.getTransformed().forEach(miraculous -> {
                Miraculous value = miraculous.value();
                MiraculousData miraculousData = miraculousesData.get(miraculous);
                AbilityData abilityData = AbilityData.of(miraculousData);
                value.passiveAbilities().forEach(ability -> ability.value().joinLevel(abilityData, level, livingEntity));
                value.activeAbility().value().joinLevel(abilityData, level, livingEntity);
            });
            entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                Kamikotization value = data.kamikotization().value();
                AbilityData abilityData = AbilityData.of(data);
                value.passiveAbilities().forEach(ability -> ability.value().joinLevel(abilityData, level, livingEntity));
                value.powerSource().right().ifPresent(ability -> ability.value().joinLevel(abilityData, level, livingEntity));
            });
        }
    }

    public static void onServerPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        TommyLibServices.NETWORK.sendToAllClients(ClientboundSyncSpecialPlayerChoicesPayload.INSTANCE, event.getEntity().getServer());
    }

    /// Life
    public static void onPreEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        Level level = entity.level();
        if (!level.isClientSide()) {
            if (entity.getData(MineraculousAttachmentTypes.YOYO_LEASH_OVERRIDE) && entity instanceof Leashable leashable) {
                Entity holder = leashable.getLeashHolder();
                if (!entity.isAlive() || holder == null || !holder.isAlive()) {
                    LadybugYoyoItem.removeLeashFrom(entity);
                }
            } else if (entity.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).map(data -> level.getEntity(data.leashedId())).orElse(null) instanceof Leashable leashable && leashable.getLeashHolder() != entity) {
                LadybugYoyoItem.removeHeldLeash(entity);
            }
        }
    }

    public static void onPostEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();

        if (entity.tickCount % 10 == 0) {
            checkInventoryComponents(entity);
        }

        if (entity.level() instanceof ServerLevel level) {
            AbilityReversionEntityData.get(level).tick(entity);
            AbilityReversionItemData.get(level).tick(entity);
            ToolIdData.get(level).tick(entity);
            LuckyCharmIdData.get(level).tick(entity);

            if (entity instanceof LivingEntity livingEntity) {
                entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).tick(livingEntity, level);
                entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> data.tick(livingEntity, level));
            }

            if (entity instanceof MiraculousLadybug miraculousLadybug) {
                MiraculousLadybugTargetData targetData = miraculousLadybug.getTargetData();
                miraculousLadybug.setTargetData(targetData.tick(level));
            }

            if (entity instanceof ItemEntity itemEntity) {
                itemEntity.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TRIGGER).ifPresent(data -> {
                    data.tick(itemEntity, level);
                });
            }

            ItemStack weaponItem = entity.getWeaponItem();
            if (entity.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).isPresent() && (weaponItem == null || !weaponItem.is(MineraculousItems.LADYBUG_YOYO))) {
                LadybugYoyoItem.removeHeldLeash(entity);
            }
        }

        if (entity instanceof MiraculousLadybug miraculousLadybug) {
            MiraculousLadybugTargetData targetData = miraculousLadybug.getTargetData();
            miraculousLadybug.setOldSplinePosition(targetData.splinePosition());
        }
    }

    public static void checkInventoryComponents(Entity entity) {
        for (ItemStack stack : MineraculousEntityUtils.getInventoryAndCurios(entity)) {
            if (!stack.isEmpty()) {
                if (entity instanceof LivingEntity livingEntity) {
                    boolean blocking = livingEntity.isBlocking() && livingEntity.getUseItem() == stack;
                    if (!blocking && stack.has(MineraculousDataComponents.BLOCKING))
                        stack.remove(MineraculousDataComponents.BLOCKING);
                    else if (blocking && !stack.has(MineraculousDataComponents.BLOCKING))
                        stack.set(MineraculousDataComponents.BLOCKING, Unit.INSTANCE);
                } else if (stack.has(MineraculousDataComponents.BLOCKING)) {
                    stack.remove(MineraculousDataComponents.BLOCKING);
                }
                Integer carrierId = stack.get(MineraculousDataComponents.CARRIER);
                if (carrierId == null || carrierId != entity.getId()) {
                    stack.set(MineraculousDataComponents.CARRIER, entity.getId());
                }
            }
        }
    }

    // Ladybug Yoyo
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity().getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO).safeFallTicks() > 0) {
            event.setDamageMultiplier(0);
        }

        if (event.getEntity().getData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF).fastDescending()) {
            event.setDamageMultiplier(0);
            if (!event.getEntity().level().isClientSide)
                PerchingCatStaffData.remove(event.getEntity(), true);
        }
    }

    // Abilities
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.level() instanceof ServerLevel level) {
            AbilityUtils.performEntityAbilities(level, player, event.getTarget());
        }
    }

    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        AbilityEffectData abilityEffectData = player.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS);
        if (abilityEffectData.spectatingId().isPresent()) {
            event.setCanceled(true);
            return;
        }
        if (player.level() instanceof ServerLevel level) {
            Entity target = event.getTarget();
            // Living entities are handled via LivingDamageEvent.Post
            if (!(target instanceof LivingEntity)) {
                AbilityUtils.performEntityAbilities(level, player, target);
            }
        }
    }

    public static void onPostLivingDamage(LivingDamageEvent.Post event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            Entity attacker = event.getSource().getEntity();
            LivingEntity target = event.getEntity();
            if (attacker instanceof LivingEntity livingAttacker) {
                AbilityUtils.performEntityAbilities(level, livingAttacker, target);
            }
            AbilityEffectData abilityEffectData = target.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS);
            if (abilityEffectData.spectatingId().isPresent()) {
                abilityEffectData.withSpectationInterrupted().save(target, true);
            }
        }
    }

    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel() instanceof ServerLevel level) {
            AbilityUtils.performBlockAbilities(level, event.getEntity(), event.getPos());
        }
    }

    public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        AbilityEffectData abilityEffectData = player.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS);
        if (abilityEffectData.spectatingId().isPresent()) {
            event.setCanceled(true);
            return;
        }
        if (event.getLevel() instanceof ServerLevel level) {
            AbilityUtils.performBlockAbilities(level, player, event.getPos());
        }
    }

    public static void onEmptyLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
        ItemStack mainHandItem = event.getEntity().getMainHandItem();
        if (!mainHandItem.isEmpty()) {
            TommyLibServices.NETWORK.sendToServer(ServerboundEmptyLeftClickItemPayload.INSTANCE);
        }
    }

    // Cataclysm
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        if (event.getEffect() == MineraculousMobEffects.CATACLYSM && !(entity instanceof Player player && player.getAbilities().invulnerable)) {
            event.setCanceled(true);
        }
        if (entity.level() instanceof ServerLevel level && level.getChunkSource() instanceof ServerChunkCache chunkCache) {
            chunkCache.broadcastAndSend(entity, new ClientboundRemoveMobEffectPacket(entity.getId(), event.getEffect()));
        }
    }

    public static void onEffectAdded(MobEffectEvent.Added event) {
        LivingEntity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel level) {
            for (ServerPlayer player : level.players()) {
                player.connection.send(new ClientboundUpdateMobEffectPacket(entity.getId(), event.getEffectInstance(), false));
            }
        }
    }

    public static void onEffectExpired(MobEffectEvent.Expired event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance effectInstance = event.getEffectInstance();
        if (entity.level() instanceof ServerLevel level && effectInstance != null) {
            for (ServerPlayer player : level.players()) {
                player.connection.send(new ClientboundRemoveMobEffectPacket(entity.getId(), effectInstance.getEffect()));
            }
        }
    }

    public static void onLivingHeal(LivingHealEvent event) {
        if (event.getEntity().hasEffect(MineraculousMobEffects.CATACLYSM)) {
            event.setCanceled(true);
        }
    }

    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            Level level = entity.level();
            for (Entity other : level.getEntities().getAll()) {
                if (other instanceof Kwami kwami && kwami.isOwnedBy(livingEntity)) {
                    kwami.changeDimension(new DimensionTransition(level.getServer().getLevel(event.getDimension()), entity, DimensionTransition.DO_NOTHING));
                }
            }
        }
        if (entity instanceof NewMiraculousLadybug) event.setCanceled(true);
    }

    public static void onLivingSwapHands(LivingSwapItemsEvent.Hands event) {
        if (event.getItemSwappedToMainHand().has(MineraculousDataComponents.KAMIKOTIZING) || event.getItemSwappedToOffHand().has(MineraculousDataComponents.KAMIKOTIZING))
            event.setCanceled(true);
    }

    /// Death
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel level) {
            MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            for (ItemStack stack : MineraculousEntityUtils.getInventoryAndCurios(entity)) {
                Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
                if (stack.getItem() instanceof MiraculousItem && miraculous != null) {
                    MiraculousData data = miraculousesData.get(miraculous);
                    if (data.transformed()) {
                        data.detransform(entity, level, miraculous, stack, true);
                    } else {
                        MineraculousEntityUtils.renounceKwami(stack.get(MineraculousDataComponents.KWAMI_ID), stack, level);
                    }
                }
                entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                    Holder<Kamikotization> kamikotization = stack.get(MineraculousDataComponents.KAMIKOTIZATION);
                    if (kamikotization == data.kamikotization()) {
                        UUID ownerId = stack.get(MineraculousDataComponents.OWNER);
                        if (ownerId == null || ownerId.equals(entity.getUUID())) {
                            data.detransform(entity, level, entity.position().add(0, 1, 0), true);
                        }
                    }
                });
            }
            entity.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).killCredit().ifPresent(killCredit -> {
                Entity killer = level.getEntity(killCredit);
                if (killer instanceof Player player) {
                    entity.setLastHurtByPlayer(player);
                } else if (killer instanceof LivingEntity livingEntity) {
                    entity.setLastHurtByMob(livingEntity);
                }
            });
            if (entity.hasEffect(MineraculousMobEffects.CATACLYSM)) {
                entity.deathTime = 20;
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, MineraculousBlocks.CATACLYSM_BLOCK.get().defaultBlockState()), entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ(), 30, Math.random() / 3.0, Math.random() / 3.0, Math.random() / 3.0, 0);
            }
        }
    }

    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel level) {
            UUID recoverer = AbilityReversionEntityData.get(level).findCause(entity, level);
            for (ItemEntity item : event.getDrops()) {
                ItemStack stack = item.getItem();
                if (entity.hasEffect(MineraculousMobEffects.CATACLYSM) && !stack.is(MineraculousItemTags.CATACLYSM_IMMUNE)) {
                    item.setItem(MineraculousItems.CATACLYSM_DUST.toStack());
                }
                if (recoverer != null) {
                    UUID id = UUID.randomUUID();
                    AbilityReversionItemData.get(level).putRemovable(recoverer, id);
                    item.getItem().set(MineraculousDataComponents.REVERTIBLE_ITEM_ID, id);
                }
                if (stack.has(MineraculousDataComponents.KAMIKOTIZING)) {
                    stack.remove(MineraculousDataComponents.KAMIKOTIZING);
                }
            }
        }
    }

    /// Exit
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.getLevel() instanceof ServerLevel level) {
            entity.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO).id().ifPresent(id -> {
                Entity yoyo = event.getLevel().getEntity(id);
                if (yoyo != null)
                    yoyo.discard();
                new ThrownLadybugYoyoData().save(entity, true);
            });
            AbilityEffectData abilityEffectData = entity.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS);
            abilityEffectData.privateChat().ifPresent(id -> {
                Entity target = level.getEntity(id);
                if (target != null) {
                    target.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withPrivateChat(Optional.empty(), Optional.empty()).save(target, true);
                }
            });
            if (entity instanceof NewMiraculousLadybug miraculousLadybug) {
                miraculousLadybug.revertAllTargets(level);
            }
            if (entity instanceof LivingEntity livingEntity) {
                MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                miraculousesData.getTransformed().forEach(miraculous -> {
                    Miraculous value = miraculous.value();
                    MiraculousData miraculousData = miraculousesData.get(miraculous);
                    AbilityData abilityData = AbilityData.of(miraculousData);
                    value.passiveAbilities().forEach(ability -> ability.value().leaveLevel(abilityData, level, livingEntity));
                    value.activeAbility().value().leaveLevel(abilityData, level, livingEntity);
                });
                entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                    Kamikotization value = data.kamikotization().value();
                    AbilityData abilityData = AbilityData.of(data);
                    value.passiveAbilities().forEach(ability -> ability.value().leaveLevel(abilityData, level, livingEntity));
                    value.powerSource().right().ifPresent(ability -> ability.value().leaveLevel(abilityData, level, livingEntity));
                });
            }
            entity.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).ifPresent(data -> LadybugYoyoItem.removeHeldLeash(entity));
            if (entity.getData(MineraculousAttachmentTypes.YOYO_LEASH_OVERRIDE)) {
                LadybugYoyoItem.removeLeashFrom(entity);
            }
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayer serverPlayer = (ServerPlayer) event.getEntity();
        serverPlayer.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO).id().ifPresent(id -> {
            Entity yoyo = serverPlayer.level().getEntity(id);
            if (yoyo != null)
                yoyo.discard();
            new ThrownLadybugYoyoData().save(serverPlayer, true);
        });
    }
}
