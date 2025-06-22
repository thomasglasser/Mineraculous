package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.context.EntityAbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionItemData;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.Nullable;

/**
 * Applies the provided {@link MobEffect}s with the provided {@link EffectSettings} or,
 * if not a {@link LivingEntity}, simply destroys the {@link Entity}.
 *
 * @param effects            The {@link MobEffect}s to apply
 * @param effectSettings     The {@link EffectSettings} to use when applying the effects
 * @param damageType         The {@link DamageType} to use on the target (for aggro, credit, and destruction)
 * @param overrideKillCredit Whether the target's kill credit should be overridden and given to the performer
 * @param allowBlocking      Whether the target can block the effects with an active blocking item
 * @param dropItem           The {@link Item} to drop in replacement of the blocking item or non-living {@link Entity}'s drops
 * @param applySound         The sound to play when the ability is performed
 */
public record ApplyEffectsOrDestroyAbility(HolderSet<MobEffect> effects, EffectSettings effectSettings, Optional<ResourceKey<DamageType>> damageType, boolean overrideKillCredit, boolean allowBlocking, Optional<Item> dropItem, Optional<Holder<SoundEvent>> applySound) implements Ability {

    public static final MapCodec<ApplyEffectsOrDestroyAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("effects").forGetter(ApplyEffectsOrDestroyAbility::effects),
            EffectSettings.CODEC.optionalFieldOf("effect_settings", EffectSettings.DEFAULT).forGetter(ApplyEffectsOrDestroyAbility::effectSettings),
            ResourceKey.codec(Registries.DAMAGE_TYPE).optionalFieldOf("damage_type").forGetter(ApplyEffectsOrDestroyAbility::damageType),
            Codec.BOOL.optionalFieldOf("override_kill_credit", false).forGetter(ApplyEffectsOrDestroyAbility::overrideKillCredit),
            Codec.BOOL.optionalFieldOf("allow_blocking", true).forGetter(ApplyEffectsOrDestroyAbility::allowBlocking),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("drop_item").forGetter(ApplyEffectsOrDestroyAbility::dropItem),
            SoundEvent.CODEC.optionalFieldOf("apply_sound").forGetter(ApplyEffectsOrDestroyAbility::applySound)).apply(instance, ApplyEffectsOrDestroyAbility::new));

    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, AbilityHandler handler, @Nullable AbilityContext context) {
        if (context instanceof EntityAbilityContext(Entity target)) {
            AbilityReversionEntityData.get(level).putRevertable(performer.getUUID(), target);
            DamageSource source = damageType.map(key -> performer.damageSources().source(key, performer)).orElseGet(() -> performer.damageSources().indirectMagic(performer, performer));
            target.hurt(source, 1);
            if (target instanceof LivingEntity livingEntity) {
                if (allowBlocking && livingEntity.isBlocking()) {
                    ItemStack stack = livingEntity.getUseItem();
                    if (dropItem.isPresent()) {
                        ItemStack replacement = new ItemStack(dropItem.get());
                        UUID id = UUID.randomUUID();
                        replacement.set(MineraculousDataComponents.RECOVERABLE_ITEM_ID, id);
                        AbilityReversionItemData.get(level).putRevertable(performer.getUUID(), id, stack);
                        livingEntity.setItemInHand(livingEntity.getUsedItemHand(), replacement);
                    } else {
                        livingEntity.setItemInHand(livingEntity.getUsedItemHand(), ItemStack.EMPTY);
                    }
                } else {
                    for (Holder<MobEffect> mobEffect : effects) {
                        MobEffectInstance effect = new MobEffectInstance(mobEffect, effectSettings.duration(), (data.powerLevel() / 10), effectSettings.ambient(), effectSettings.showParticles(), effectSettings.showIcon());
                        livingEntity.addEffect(effect);
                    }
                }
            } else if (target instanceof VehicleEntity vehicle) {
                vehicle.kill();
                if (dropItem.isPresent() && level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                    ItemStack stack = new ItemStack(dropItem.get());
                    UUID id = UUID.randomUUID();
                    stack.set(MineraculousDataComponents.RECOVERABLE_ITEM_ID, id);
                    AbilityReversionItemData.get(level).putRemovable(performer.getUUID(), id);
                    vehicle.spawnAtLocation(stack);
                }
            } else {
                target.hurt(source, 1024);
            }
            if (performer instanceof LivingEntity livingEntity) {
                livingEntity.setLastHurtMob(target);
            }
            if (overrideKillCredit) {
                target.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withKillCredit(Optional.of(performer.getUUID())).save(target, true);
            }
            Ability.playSound(level, performer, applySound);
            return true;
        }
        return false;
    }

    @Override
    public void revert(AbilityData data, ServerLevel level, Entity performer) {
        AbilityReversionEntityData.get(level).revert(performer.getUUID(), level, entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                for (Holder<MobEffect> effect : effects) {
                    livingEntity.removeEffect(effect);
                }
            }
        });
        AbilityReversionItemData.get(level).markReverted(performer.getUUID());
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.APPLY_EFFECTS_OR_DESTROY.get();
    }
    /**
     * Holds the settings for how to provide the specified effects
     * 
     * @param duration      The duration of the effects applied (or -1 for infinite)
     * @param ambient       Whether the effect should be ambient, showing up with a different icon and less intrusive particles
     * @param showParticles Whether the effect particles should be visible
     * @param showIcon      Whether the effect icon should show in the HUD
     */
    public record EffectSettings(int duration, boolean ambient, boolean showParticles, boolean showIcon) {

        public static final EffectSettings DEFAULT = new EffectSettings(-1, false, true);
        public static final Codec<EffectSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.intRange(-1, Integer.MAX_VALUE).optionalFieldOf("duration", -1).forGetter(EffectSettings::duration),
                Codec.BOOL.optionalFieldOf("ambient", false).forGetter(EffectSettings::ambient),
                Codec.BOOL.optionalFieldOf("show_particles", true).forGetter(EffectSettings::showParticles),
                Codec.BOOL.optionalFieldOf("show_icon").forGetter(settings -> Optional.of(settings.showIcon))).apply(instance, EffectSettings::new));
        public EffectSettings(int duration, boolean ambient, boolean showParticles, Optional<Boolean> showIcon) {
            this(duration, ambient, showParticles, showIcon.orElse(showParticles));
        }

        public EffectSettings(int duration, boolean ambient, boolean showParticles) {
            this(duration, ambient, showParticles, showParticles);
        }
    }
}
