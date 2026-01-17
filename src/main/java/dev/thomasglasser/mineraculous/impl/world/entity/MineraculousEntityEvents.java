package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.event.CanBeForceKamikotizedEvent;
import dev.thomasglasser.mineraculous.api.event.CollectMiraculousLadybugTargetsEvent;
import dev.thomasglasser.mineraculous.api.event.ShouldTrackEntityEvent;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityData;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityUtils;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.item.EffectRevertingItem;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.level.storage.BlockReversionData;
import dev.thomasglasser.mineraculous.api.world.level.storage.EntityReversionData;
import dev.thomasglasser.mineraculous.api.world.level.storage.ItemReversionData;
import dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects.SyncedTransientAbilityEffectData;
import dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects.TransientAbilityEffectData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.network.ClientboundSyncSpecialPlayerChoicesPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundEmptyLeftClickItemPayload;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import dev.thomasglasser.mineraculous.impl.world.item.KwamiItem;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.impl.world.item.ability.CatStaffPerchCommander;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugBlockTarget;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugEntityTarget;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTargetCollector;
import dev.thomasglasser.mineraculous.impl.world.level.storage.LuckyCharmIdData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ToolIdData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
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
        event.put(MineraculousEntityTypes.KAMIKOTIZED_MINION.get(), PlayerLike.createDefaultAttributes().build());
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

        if (entity instanceof ServerPlayer player) {
            player.getData(MineraculousAttachmentTypes.MIRACULOUSES).forEach((miraculous, data) -> ServerLookManager.requestMissingLooks(data.lookData(), player));
            player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> ServerLookManager.requestMissingLooks(data.lookData(), player));
            ServerLookManager.sendServerLooks(player);
        }
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
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
            EntityReversionData.get(level).tick(entity);
            ItemReversionData.get(level).tick(entity);
            ToolIdData.get(level).tick(entity);
            LuckyCharmIdData.get(level).tick(entity);

            if (entity instanceof LivingEntity livingEntity) {
                entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).tick(livingEntity, level);
                entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> data.tick(livingEntity, level));
                entity.getData(MineraculousAttachmentTypes.PERSISTENT_ABILITY_EFFECTS).tick(livingEntity, level);
            }

            entity.getData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TRIGGER).ifPresent(data -> data.tick(entity, level));

            ItemStack weaponItem = entity.getWeaponItem();
            if (entity.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).isPresent() && (weaponItem == null || !weaponItem.is(MineraculousItems.LADYBUG_YOYO))) {
                LadybugYoyoItem.removeHeldLeash(entity);
            }
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
        Entity entity = event.getEntity();
        if (entity.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO).safeFallTicks() > 0) {
            event.setDamageMultiplier(0);
        }
        CatStaffPerchCommander.entityFall(event);
    }

    public static void onEntityTeleport(EntityTeleportEvent event) {
        Entity entity = event.getEntity();
        if (entity.getData(MineraculousAttachmentTypes.YOYO_LEASH_OVERRIDE))
            LadybugYoyoItem.removeLeashFrom(entity);
    }

    // Abilities
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (event.getHand() == InteractionHand.MAIN_HAND && player.level() instanceof ServerLevel level) {
            AbilityUtils.performEntityAbilities(level, player, event.getTarget());
        }
    }

    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        SyncedTransientAbilityEffectData abilityEffectData = player.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS);
        if (abilityEffectData.spectatingId().isPresent()) {
            event.setCanceled(true);
            return;
        }
        if (player.level() instanceof ServerLevel level) {
            Entity target = event.getTarget();
            AbilityUtils.performEntityAbilities(level, player, target);
        }
    }

    public static void onPostLivingDamage(LivingDamageEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) {
            LivingEntity target = event.getEntity();
            SyncedTransientAbilityEffectData syncedTransientAbilityEffectData = target.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS);
            TransientAbilityEffectData transientAbilityEffectData = target.getData(MineraculousAttachmentTypes.TRANSIENT_ABILITY_EFFECTS);
            if (syncedTransientAbilityEffectData.spectatingId().isPresent()) {
                transientAbilityEffectData.withSpectationInterrupted(true).save(target);
            }
        }
    }

    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel() instanceof ServerLevel level && event.getHand() == InteractionHand.MAIN_HAND) {
            AbilityUtils.performBlockAbilities(level, event.getEntity(), event.getPos());
        }
    }

    public static void onBlockLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        SyncedTransientAbilityEffectData abilityEffectData = player.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS);
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

    public static void onShouldTrackEntity(ShouldTrackEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed() || entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent())
            event.setShouldTrack(true);
    }

    // Miraculous Ladybug
    public static void onCollectMiraculousLadybugTargets(CollectMiraculousLadybugTargetsEvent event) {
        ServerLevel level = event.getLevel();
        UUID targetId = event.getTargetId();
        MiraculousLadybugTargetCollector collector = event.getTargetCollector();

        BlockReversionData blockData = BlockReversionData.get(level);
        EntityReversionData entityData = EntityReversionData.get(level);
        ItemReversionData itemData = ItemReversionData.get(level);

        for (UUID relatedId : collectToRevert(targetId, entityData)) {
            beginReversionAndGatherTargets(level, relatedId, collector, blockData, entityData, itemData);
            if (level.getEntity(relatedId) instanceof LivingEntity related) {
                related.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).or(() -> related.getData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION)).ifPresent(kamikotizationData -> {
                    Kamikotization value = kamikotizationData.kamikotization().value();
                    AbilityData abilityData = AbilityData.of(kamikotizationData);
                    value.powerSource().ifLeft(tool -> {
                        if (tool.getItem() instanceof EffectRevertingItem item) {
                            item.revert(related, collector);
                        }
                    }).ifRight(ability -> ability.value().revert(abilityData, level, related, collector));
                    value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related, collector));
                });
                MiraculousesData miraculousesData = related.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                for (Holder<Miraculous> miraculous : miraculousesData.keySet()) {
                    Miraculous value = miraculous.value();
                    AbilityData abilityData = AbilityData.of(miraculousesData.get(miraculous));
                    value.activeAbility().value().revert(abilityData, level, related, collector);
                    value.passiveAbilities().forEach(ability -> ability.value().revert(abilityData, level, related, collector));
                }
            }
        }
    }

    private static void beginReversionAndGatherTargets(
            ServerLevel level,
            UUID relatedId,
            MiraculousLadybugTargetCollector targetCollector,
            BlockReversionData blockData,
            EntityReversionData entityData,
            ItemReversionData itemData) {
        itemData.markReverted(relatedId);
        entityData.revertRemovableAndCopied(relatedId, level);
        for (Map.Entry<ResourceKey<Level>, BlockPos> location : blockData.getReversionPositions(relatedId).entries()) {
            targetCollector.putClusterable(location.getKey(), new MiraculousLadybugBlockTarget(location.getValue(), relatedId));
        }
        for (Map.Entry<ResourceKey<Level>, Vec3> location : entityData.getReversionAndConversionPositions(relatedId).entries()) {
            ResourceKey<Level> dimension = location.getKey();
            Vec3 pos = location.getValue();
            for (Map.Entry<UUID, CompoundTag> entry : entityData.getRevertibleAndConvertedAt(relatedId, dimension, pos).entrySet()) {
                UUID entityId = entry.getKey();
                Entity entity = MineraculousEntityUtils.findEntity(level, entityId);
                if (entity != null) {
                    targetCollector.put(dimension, new MiraculousLadybugEntityTarget(pos, relatedId, entity.getBbWidth(), entity.getBbHeight()));
                } else {
                    CompoundTag tag = entry.getValue();
                    EntityType.by(tag).ifPresentOrElse(type -> targetCollector.put(dimension, new MiraculousLadybugEntityTarget(pos, relatedId, type.getWidth(), type.getHeight())), () -> MineraculousConstants.LOGGER.error("Invalid entity data passed to RevertLuckyCharmTargetsAbilityEffectsAbility: {}", tag));
                }
            }
        }
    }

    private static Set<UUID> collectToRevert(UUID uuid, EntityReversionData entityData) {
        Set<UUID> toRevert = new ReferenceOpenHashSet<>();
        toRevert.add(uuid);
        for (UUID related : entityData.getAndClearTrackedAndRelatedEntities(uuid)) {
            if (!toRevert.contains(related)) {
                toRevert.add(related);
                collectToRevert(related, entityData);
            }
        }
        return toRevert;
    }

    // Cataclysm
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        if (event.getEffect().is(MineraculousMobEffects.CATACLYSM) && !(entity instanceof Player player && player.getAbilities().invulnerable)) {
            event.setCanceled(true);
        } else {
            if (entity.level() instanceof ServerLevel level && level.getChunkSource() instanceof ServerChunkCache chunkCache) {
                chunkCache.broadcastAndSend(entity, new ClientboundRemoveMobEffectPacket(entity.getId(), event.getEffect()));
            }
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
        if (entity instanceof MiraculousLadybug) event.setCanceled(true);
    }

    public static void onLivingSwapHands(LivingSwapItemsEvent.Hands event) {
        if (event.getItemSwappedToMainHand().has(MineraculousDataComponents.KAMIKOTIZING) || event.getItemSwappedToOffHand().has(MineraculousDataComponents.KAMIKOTIZING))
            event.setCanceled(true);
    }

    public static void onCanBeForceKamikotized(CanBeForceKamikotizedEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player && player.getAbilities().instabuild && !MineraculousServerConfig.get().forceKamikotizeCreativePlayers.getAsBoolean())
            event.setCanBeKamikotized(false);
    }

    /// Death
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel level) {
            MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            for (ItemStack stack : MineraculousEntityUtils.getInventoryAndCurios(entity)) {
                if (stack.getItem() instanceof KwamiItem) {
                    KwamiItem.summonKwami(stack, entity);
                }
                Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
                if (stack.getItem() instanceof MiraculousItem && miraculous != null) {
                    MiraculousData data = miraculousesData.get(miraculous);
                    if (data.transformed()) {
                        data.detransform(entity, level, miraculous, stack, true);
                    } else {
                        MineraculousEntityUtils.renounceKwami(true, stack, entity);
                    }
                }
                entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                    Holder<Kamikotization> kamikotization = stack.get(MineraculousDataComponents.KAMIKOTIZATION);
                    if (kamikotization == data.kamikotization()) {
                        UUID ownerId = stack.get(MineraculousDataComponents.OWNER);
                        if (ownerId == null || ownerId.equals(entity.getUUID())) {
                            data.detransform(entity, level, entity.position().add(0, 1, 0), true, true, null);
                        }
                    }
                });
            }
            entity.getData(MineraculousAttachmentTypes.PERSISTENT_ABILITY_EFFECTS).killCreditOverride().ifPresent(killCredit -> {
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
            UUID recoverer = EntityReversionData.get(level).getCause(entity);
            for (ItemEntity item : event.getDrops()) {
                ItemStack stack = item.getItem();
                if (entity.hasEffect(MineraculousMobEffects.CATACLYSM) && !stack.is(MineraculousItemTags.CATACLYSM_IMMUNE)) {
                    item.setItem(MineraculousItems.CATACLYSM_DUST.toStack());
                }
                if (recoverer != null) {
                    UUID id = UUID.randomUUID();
                    ItemReversionData.get(level).putRemovable(recoverer, id);
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
                new ThrownLadybugYoyoData().save(entity);
            });
            SyncedTransientAbilityEffectData abilityEffectData = entity.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS);
            abilityEffectData.privateChat().ifPresent(id -> {
                Entity target = level.getEntity(id);
                if (target != null) {
                    target.getData(MineraculousAttachmentTypes.SYNCED_TRANSIENT_ABILITY_EFFECTS).withPrivateChat(Optional.empty(), Optional.empty()).save(target);
                }
            });
            if (entity instanceof MiraculousLadybug miraculousLadybug) {
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
            new ThrownLadybugYoyoData().save(serverPlayer);
        });
    }
}
