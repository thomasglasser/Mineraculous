package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.advancements.critereon.UseKamikotizationPowerTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.UseMiraculousPowerTrigger;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public record DragAbility(Ability ability, int dragTicks, Optional<Holder<SoundEvent>> startSound, boolean overrideActive) implements Ability, HasSubAbility {

    public static final String DRAG_TICKS = "DragTicks";
    public static final MapCodec<DragAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ability.DIRECT_CODEC.fieldOf("ability").forGetter(DragAbility::ability),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("drag_ticks", 20).forGetter(DragAbility::dragTicks),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(DragAbility::startSound),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(DragAbility::overrideActive)).apply(instance, DragAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, Context context) {
        CompoundTag performerData = /*TommyLibServices.ENTITY.getPersistentData(entity)*/new CompoundTag();
        int remainingDragTicks = performerData.getInt(DRAG_TICKS);
        boolean consume = ability.perform(data, level, performer, context);
        if (context == Context.PASSIVE) {
            if (remainingDragTicks != 0) {
                // TODO: Fix
//                int nowRemaining = remainingDragTicks - 1;
//                if (nowRemaining <= 0) {
//                    performerData.remove(DRAG_TICKS);
//                    TommyLibServices.ENTITY.setPersistentData(entity, performerData, true);
//                    return true;
//                }
//                performerData.putInt(DRAG_TICKS, nowRemaining);
//                TommyLibServices.ENTITY.setPersistentData(entity, performerData, true);
            }
            return false;
        }
        if (consume) {
            if (performer instanceof ServerPlayer serverPlayer) {
                if (data.power().left().isPresent())
                    MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(serverPlayer, data.power().left().get(), miraculousContextFrom(context));
                if (data.power().right().isPresent())
                    MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(serverPlayer, data.power().right().get(), kamikotizationContextFrom(context));
            }
            if (remainingDragTicks == 0) {
                performerData.putInt(DRAG_TICKS, dragTicks);
//                TommyLibServices.ENTITY.setPersistentData(entity, performerData, true);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean canActivate(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        return ability.canActivate(data, level, performer, );
    }

    @Override
    public void restore(AbilityData data, ServerLevel level, Entity performer) {
        ability.restore(data, level, performer);
    }

    UseMiraculousPowerTrigger.Context miraculousContextFrom(Ability.Context context) {
        return switch (context) {
            case PASSIVE, INTERACT_AIR -> UseMiraculousPowerTrigger.Context.EMPTY;
            case INTERACT_BLOCK -> UseMiraculousPowerTrigger.Context.BLOCK;
            case INTERACT_ENTITY -> context.entity() instanceof LivingEntity ? UseMiraculousPowerTrigger.Context.LIVING_ENTITY : UseMiraculousPowerTrigger.Context.ENTITY;
            case INTERACT_ITEM -> UseMiraculousPowerTrigger.Context.ITEM;
        };
    }

    UseKamikotizationPowerTrigger.Context kamikotizationContextFrom(Ability.Context context) {
        return switch (context) {
            case PASSIVE, INTERACT_AIR -> UseKamikotizationPowerTrigger.Context.PASSIVE;
            case INTERACT_BLOCK -> UseKamikotizationPowerTrigger.Context.BLOCK;
            case INTERACT_ENTITY -> context.entity() instanceof LivingEntity ? UseKamikotizationPowerTrigger.Context.LIVING_ENTITY : UseKamikotizationPowerTrigger.Context.ENTITY;
            case INTERACT_ITEM -> UseKamikotizationPowerTrigger.Context.ITEM;
        };
    }

    @Override
    public List<Ability> getAll() {
        List<Ability> abilities = new ReferenceArrayList<>();
        abilities.add(this);
        abilities.addAll(Ability.getAll(ability));
        return abilities;
    }

    @Override
    public List<Ability> getMatching(Predicate<Ability> predicate) {
        List<Ability> abilities = new ReferenceArrayList<>();
        abilities.add(this);
        abilities.addAll(Ability.getMatching(predicate, ability));
        return abilities;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.DRAG.get();
    }
}
