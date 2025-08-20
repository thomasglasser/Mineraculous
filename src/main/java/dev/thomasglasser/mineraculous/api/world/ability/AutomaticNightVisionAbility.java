package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.impl.network.ClientboundToggleNightVisionShaderPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Applies and removes night vision automatically based on light level.
 *
 * @param lightLevel  The maximum light level to apply night vision at
 * @param shader      The shader to apply when night vision is applied
 * @param applySound  The sound to play when night vision is applied
 * @param removeSound The sound to play when night vision is removed
 */
public record AutomaticNightVisionAbility(int lightLevel, Optional<ResourceLocation> shader, Optional<Holder<SoundEvent>> applySound, Optional<Holder<SoundEvent>> removeSound) implements Ability {

    public static final int DEFAULT_LIGHT_LEVEL = 5;
    public static final MapCodec<AutomaticNightVisionAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("light_level", DEFAULT_LIGHT_LEVEL).forGetter(AutomaticNightVisionAbility::lightLevel),
            ResourceLocation.CODEC.optionalFieldOf("shader").forGetter(AutomaticNightVisionAbility::shader),
            SoundEvent.CODEC.optionalFieldOf("apply_sound").forGetter(AutomaticNightVisionAbility::applySound),
            SoundEvent.CODEC.optionalFieldOf("remove_sound").forGetter(AutomaticNightVisionAbility::removeSound)).apply(instance, AutomaticNightVisionAbility::new));
    public AutomaticNightVisionAbility(Optional<ResourceLocation> shader, Optional<Holder<SoundEvent>> applySound, Optional<Holder<SoundEvent>> removeSound) {
        this(DEFAULT_LIGHT_LEVEL, shader, applySound, removeSound);
    }

    @Override
    public State perform(AbilityData data, ServerLevel level, LivingEntity performer, AbilityHandler handler, @Nullable AbilityContext context) {
        if (context == null) {
            checkNightVision(data.powerLevel(), level, performer);
        }
        return State.CONTINUE;
    }

    @Override
    public void detransform(AbilityData data, ServerLevel level, LivingEntity performer) {
        updateNightVision(data.powerLevel(), level, performer, false);
    }

    @Override
    public void joinLevel(AbilityData data, ServerLevel level, LivingEntity performer) {
        checkNightVision(data.powerLevel(), level, performer);
    }

    public void checkNightVision(int powerLevel, ServerLevel level, LivingEntity performer) {
        boolean hasNightVision = performer.hasEffect(MobEffects.NIGHT_VISION);
        if (level.getRawBrightness(performer.blockPosition().above(), level.getSkyDarken()) <= lightLevel) {
            if (!hasNightVision) {
                updateNightVision(powerLevel, level, performer, true);
            }
        } else if (hasNightVision) {
            updateNightVision(powerLevel, level, performer, false);
        }
    }

    private void updateNightVision(int powerLevel, ServerLevel level, LivingEntity performer, boolean giveNightVision) {
        shader.ifPresent(shader -> {
            if (performer instanceof ServerPlayer player) {
                TommyLibServices.NETWORK.sendToClient(new ClientboundToggleNightVisionShaderPayload(giveNightVision, shader), player);
            }
        });
        if (giveNightVision) {
            MineraculousEntityUtils.applyInfiniteHiddenEffect(performer, MobEffects.NIGHT_VISION, powerLevel / 10);
        } else {
            performer.removeEffect(MobEffects.NIGHT_VISION);
        }
        performer.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withShader(giveNightVision ? shader : Optional.empty()).save(performer, true);
        Ability.playSound(level, performer, giveNightVision ? applySound : removeSound);
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.AUTOMATIC_NIGHT_VISION.get();
    }
}
