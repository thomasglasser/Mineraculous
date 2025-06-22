package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityData;
import dev.thomasglasser.mineraculous.api.world.ability.AbilityUtils;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
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
import dev.thomasglasser.mineraculous.impl.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.LuckyCharmIdData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ToolIdData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
        if (event.getLevel() instanceof ServerLevel level) {
            MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            miraculousesData.getTransformed().forEach(miraculous -> {
                Miraculous value = miraculous.value();
                MiraculousData miraculousData = miraculousesData.get(miraculous);
                AbilityData abilityData = new AbilityData(miraculousData.powerLevel(), miraculousData.powerActive());
                value.passiveAbilities().forEach(ability -> ability.value().joinLevel(abilityData, level, entity));
                value.activeAbility().value().joinLevel(abilityData, level, entity);
            });
            entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                Kamikotization value = data.kamikotization().value();
                AbilityData abilityData = new AbilityData(0, data.powerActive());
                value.passiveAbilities().forEach(ability -> ability.value().joinLevel(abilityData, level, entity));
                value.powerSource().right().ifPresent(ability -> ability.value().joinLevel(abilityData, level, entity));
            });
        }
    }

    public static void onServerPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        TommyLibServices.NETWORK.sendToAllClients(ClientboundSyncSpecialPlayerChoicesPayload.INSTANCE, event.getEntity().getServer());
    }

    /// Life
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel level) {
            checkInventoryComponents(entity);

            AbilityReversionEntityData.get(level).tick(entity);
            AbilityReversionItemData.get(level).tick(entity);
            ToolIdData.get(level).tick(entity);
            LuckyCharmIdData.get(level).tick(entity);

            entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).tick(entity, level);
            entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> data.tick(entity, level));
            entity.getData(MineraculousAttachmentTypes.YOYO_LEASH).ifPresent(data -> data.tick(entity, level));
        }
    }

    public static void checkInventoryComponents(Entity entity) {
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
            Integer carrierId = stack.get(MineraculousDataComponents.CARRIER);
            if (carrierId == null || carrierId != entity.getId()) {
                stack.set(MineraculousDataComponents.CARRIER, entity.getId());
            }
        }
    }

    // Ladybug Yoyo
    public static void onPlayerBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack mainHandItem = event.getEntity().getMainHandItem();
        if (mainHandItem.is(MineraculousItems.LADYBUG_YOYO) && mainHandItem.getOrDefault(MineraculousDataComponents.ACTIVE, false)) {
            event.setCanceled(true);
        }
    }

    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity().getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO).safeFallTicks() > 0) {
            event.setDamageMultiplier(0);
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

    public static void onLivingAttack(LivingDamageEvent.Post event) {
        if (event.getEntity().level() instanceof ServerLevel level) {
            Entity attacker = event.getSource().getEntity();
            LivingEntity target = event.getEntity();
            if (attacker != null) {
                AbilityUtils.performEntityAbilities(level, attacker, target);
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

    // Cataclysm
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        LivingEntity entity = event.getEntity();
        if (event.getEffect() == MineraculousMobEffects.CATACLYSM && !(entity instanceof Player player && player.getAbilities().invulnerable)) {
            event.setCanceled(true);
        }
        if (entity.level() instanceof ServerLevel level) {
            for (ServerPlayer player : level.players()) {
                player.connection.send(new ClientboundRemoveMobEffectPacket(entity.getId(), event.getEffect()));
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

    /// Death
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level() instanceof ServerLevel level) {
            MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            for (ItemStack stack : EntityUtils.getInventory(entity)) {
                Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
                if (miraculous != null) {
                    MiraculousData data = miraculousesData.get(miraculous);
                    if (data.transformed()) {
                        data.detransform(entity, level, miraculous, true);
                    } else {
                        data.withKwamiData(KwamiData.renounce(data.kwamiData(), stack, level)).save(miraculous, entity, true);
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
            UUID recoverer = AbilityReversionEntityData.get(level).getCause(entity, level);
            for (ItemEntity item : event.getDrops()) {
                ItemStack stack = item.getItem();
                if (entity.hasEffect(MineraculousMobEffects.CATACLYSM) && !stack.is(MineraculousItemTags.CATACLYSM_IMMUNE)) {
                    item.setItem(MineraculousItems.CATACLYSM_DUST.toStack());
                }
                if (recoverer != null) {
                    UUID id = UUID.randomUUID();
                    AbilityReversionItemData.get(level).putRemovable(recoverer, id);
                    item.getItem().set(MineraculousDataComponents.RECOVERABLE_ITEM_ID, id);
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
            MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            miraculousesData.getTransformed().forEach(miraculous -> {
                Miraculous value = miraculous.value();
                MiraculousData miraculousData = miraculousesData.get(miraculous);
                AbilityData abilityData = new AbilityData(miraculousData.powerLevel(), miraculousData.powerActive());
                value.passiveAbilities().forEach(ability -> ability.value().leaveLevel(abilityData, level, entity));
                value.activeAbility().value().leaveLevel(abilityData, level, entity);
            });
            entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).ifPresent(data -> {
                Kamikotization value = data.kamikotization().value();
                AbilityData abilityData = new AbilityData(0, data.powerActive());
                value.passiveAbilities().forEach(ability -> ability.value().leaveLevel(abilityData, level, entity));
                value.powerSource().right().ifPresent(ability -> ability.value().leaveLevel(abilityData, level, entity));
            });
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
