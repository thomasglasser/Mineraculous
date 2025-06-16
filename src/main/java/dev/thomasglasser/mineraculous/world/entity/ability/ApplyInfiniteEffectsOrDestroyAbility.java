package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.entity.ability.context.EntityAbilityContext;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityReversionItemData;
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

public record ApplyInfiniteEffectsOrDestroyAbility(HolderSet<MobEffect> effects, EffectSettings effectSettings, Optional<Item> dropItem, Optional<ResourceKey<DamageType>> damageType, boolean overrideKillCredit, boolean allowBlocking, Optional<Holder<SoundEvent>> applySound) implements Ability {

    public static final MapCodec<ApplyInfiniteEffectsOrDestroyAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("effects").forGetter(ApplyInfiniteEffectsOrDestroyAbility::effects),
            EffectSettings.CODEC.optionalFieldOf("effect_settings", EffectSettings.DEFAULT).forGetter(ApplyInfiniteEffectsOrDestroyAbility::effectSettings),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("drop_item").forGetter(ApplyInfiniteEffectsOrDestroyAbility::dropItem),
            ResourceKey.codec(Registries.DAMAGE_TYPE).optionalFieldOf("damage_type").forGetter(ApplyInfiniteEffectsOrDestroyAbility::damageType),
            Codec.BOOL.optionalFieldOf("override_kill_credit", false).forGetter(ApplyInfiniteEffectsOrDestroyAbility::overrideKillCredit),
            Codec.BOOL.optionalFieldOf("allow_blocking", true).forGetter(ApplyInfiniteEffectsOrDestroyAbility::allowBlocking),
            SoundEvent.CODEC.optionalFieldOf("apply_sound").forGetter(ApplyInfiniteEffectsOrDestroyAbility::applySound)).apply(instance, ApplyInfiniteEffectsOrDestroyAbility::new));

    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        if (context instanceof EntityAbilityContext(Entity target)) {
            AbilityReversionEntityData.get(level).putRevertable(performer.getUUID(), target);
            DamageSource source = damageType.map(key -> performer.damageSources().source(key, performer)).orElse(performer.damageSources().indirectMagic(performer, performer));
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
                        MobEffectInstance effect = new MobEffectInstance(mobEffect, -1, (data.powerLevel() / 10), effectSettings.ambient(), effectSettings.showParticles(), effectSettings.showIcon());
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
        return AbilitySerializers.APPLY_INFINITE_EFFECTS_OR_DESTROY.get();
    }
    public record EffectSettings(boolean ambient, boolean showParticles, boolean showIcon) {
        public static final EffectSettings DEFAULT = new EffectSettings(false, true, true);
        public static final Codec<EffectSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("ambient").forGetter(EffectSettings::ambient),
                Codec.BOOL.fieldOf("show_particles").forGetter(EffectSettings::showParticles),
                Codec.BOOL.fieldOf("show_icon").forGetter(EffectSettings::showIcon)).apply(instance, EffectSettings::new));
    }
}
