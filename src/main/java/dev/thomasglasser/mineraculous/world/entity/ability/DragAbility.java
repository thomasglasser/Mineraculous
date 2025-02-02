package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.advancements.critereon.KamikotizationUsePowerTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.MiraculousUsePowerTrigger;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;

public record DragAbility(Ability ability, int dragTicks, Optional<Holder<SoundEvent>> startSound, boolean overrideActive) implements Ability {

    public static final String DRAG_TICKS = "DragTicks";
    public static final MapCodec<DragAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ability.DIRECT_CODEC.fieldOf("ability").forGetter(DragAbility::ability),
            Codec.INT.optionalFieldOf("drag_ticks", 20).forGetter(DragAbility::dragTicks),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(DragAbility::startSound),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(DragAbility::overrideActive)).apply(instance, DragAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity, Context context) {
        CompoundTag performerData = TommyLibServices.ENTITY.getPersistentData(entity);
        int remainingDragTicks = performerData.getInt(DRAG_TICKS);
        boolean consume = ability.perform(data, level, pos, entity, context);
        if (context == Context.PASSIVE) {
            if (remainingDragTicks != 0) {
                int nowRemaining = remainingDragTicks - 1;
                if (nowRemaining <= 0) {
                    performerData.remove(DRAG_TICKS);
                    TommyLibServices.ENTITY.setPersistentData(entity, performerData, true);
                    return true;
                }
                performerData.putInt(DRAG_TICKS, nowRemaining);
                TommyLibServices.ENTITY.setPersistentData(entity, performerData, true);
            }
            return false;
        }
        if (consume) {
            if (entity instanceof ServerPlayer serverPlayer) {
                if (data.power().left().isPresent())
                    MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(serverPlayer, data.power().left().get(), miraculousContextFrom(context));
                if (data.power().right().isPresent())
                    MineraculousCriteriaTriggers.USED_KAMIKOTIZATION_POWER.get().trigger(serverPlayer, data.power().right().get(), kamikotizationContextFrom(context));
            }
            if (remainingDragTicks == 0) {
                performerData.putInt(DRAG_TICKS, dragTicks);
                TommyLibServices.ENTITY.setPersistentData(entity, performerData, true);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean canActivate(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {
        return ability.canActivate(data, level, pos, entity);
    }

    MiraculousUsePowerTrigger.Context miraculousContextFrom(Ability.Context context) {
        return switch (context) {
            case PASSIVE, INTERACT_AIR -> MiraculousUsePowerTrigger.Context.EMPTY;
            case INTERACT_BLOCK -> MiraculousUsePowerTrigger.Context.BLOCK;
            case INTERACT_ENTITY -> context.entity() instanceof LivingEntity ? MiraculousUsePowerTrigger.Context.LIVING_ENTITY : MiraculousUsePowerTrigger.Context.ENTITY;
            case INTERACT_ITEM -> MiraculousUsePowerTrigger.Context.ITEM;
        };
    }

    KamikotizationUsePowerTrigger.Context kamikotizationContextFrom(Ability.Context context) {
        return switch (context) {
            case PASSIVE, INTERACT_AIR -> KamikotizationUsePowerTrigger.Context.EMPTY;
            case INTERACT_BLOCK -> KamikotizationUsePowerTrigger.Context.BLOCK;
            case INTERACT_ENTITY -> context.entity() instanceof LivingEntity ? KamikotizationUsePowerTrigger.Context.LIVING_ENTITY : KamikotizationUsePowerTrigger.Context.ENTITY;
            case INTERACT_ITEM -> KamikotizationUsePowerTrigger.Context.ITEM;
        };
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.DRAG.get();
    }
}
