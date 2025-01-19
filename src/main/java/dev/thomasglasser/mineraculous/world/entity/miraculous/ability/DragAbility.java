package dev.thomasglasser.mineraculous.world.entity.miraculous.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public record DragAbility(Ability ability, int dragTicks, Optional<Holder<SoundEvent>> startSound, boolean overrideActive) implements Ability {

    public static final String DRAG_TICKS = "DragTicks";
    public static final MapCodec<DragAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ability.DIRECT_CODEC.fieldOf("ability").forGetter(DragAbility::ability),
            Codec.INT.optionalFieldOf("drag_ticks", 20).forGetter(DragAbility::dragTicks),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(DragAbility::startSound),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(DragAbility::overrideActive)).apply(instance, DragAbility::new));
    @Override
    public boolean perform(AbilityData data, Level level, BlockPos pos, LivingEntity performer, Context context) {
        CompoundTag performerData = TommyLibServices.ENTITY.getPersistentData(performer);
        int remainingDragTicks = performerData.getInt(DRAG_TICKS);
        if (context == Context.PASSIVE) {
            ability.perform(data, level, pos, performer, context);
            if (remainingDragTicks != 0) {
                int nowRemaining = remainingDragTicks - 1;
                if (nowRemaining <= 0) {
                    performerData.remove(DRAG_TICKS);
                    TommyLibServices.ENTITY.setPersistentData(performer, performerData, !level.isClientSide);
                    return true;
                }
                performerData.putInt(DRAG_TICKS, nowRemaining);
                TommyLibServices.ENTITY.setPersistentData(performer, performerData, !level.isClientSide);
            }
            return false;
        }
        if (ability.perform(data, level, pos, performer, context) && remainingDragTicks == 0) {
            performerData.putInt(DRAG_TICKS, dragTicks);
            TommyLibServices.ENTITY.setPersistentData(performer, performerData, !level.isClientSide);
            return false;
        }
        return false;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.DRAG.get();
    }
}
