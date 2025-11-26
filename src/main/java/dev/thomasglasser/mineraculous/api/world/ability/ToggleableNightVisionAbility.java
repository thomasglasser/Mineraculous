package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects.AbilityEffectUtils;
import dev.thomasglasser.mineraculous.impl.network.ClientboundToggleNightVisionShaderPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Applies and removes night vision on key press.
 *
 * @param shader      The shader to apply when night vision is applied
 * @param applySound  The sound to play when night vision is applied
 * @param removeSound The sound to play when night vision is removed
 */
public record ToggleableNightVisionAbility(Optional<ResourceLocation> shader, Optional<Holder<SoundEvent>> applySound, Optional<Holder<SoundEvent>> removeSound) implements Ability {

    public static final MapCodec<ToggleableNightVisionAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("shader").forGetter(ToggleableNightVisionAbility::shader),
            SoundEvent.CODEC.optionalFieldOf("apply_sound").forGetter(ToggleableNightVisionAbility::applySound),
            SoundEvent.CODEC.optionalFieldOf("remove_sound").forGetter(ToggleableNightVisionAbility::removeSound)).apply(instance, ToggleableNightVisionAbility::new));
    @Override
    public State perform(AbilityData data, ServerLevel level, LivingEntity performer, AbilityHandler handler, @Nullable AbilityContext context) {
        if (context == null) {
            checkNightVision(data.powerLevel(), level, performer);
        }
        return State.PASS;
    }

    @Override
    public void detransform(AbilityData data, ServerLevel level, LivingEntity performer) {
        updateNightVision(data.powerLevel(), level, performer, false);
    }

    @Override
    public void joinLevel(AbilityData data, ServerLevel level, LivingEntity performer) {
        updateNightVision(data.powerLevel(), level, performer, false);
    }

    public void checkNightVision(int powerLevel, ServerLevel level, LivingEntity performer) {
        boolean hasNightVision = performer.hasEffect(MobEffects.NIGHT_VISION);
        if (performer.getData(MineraculousAttachmentTypes.TRANSIENT_ABILITY_EFFECTS).shouldToggleNightVision()) {
            updateNightVision(powerLevel, level, performer, !hasNightVision);
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
        AbilityEffectUtils.updateNightVision(performer, giveNightVision ? shader : Optional.empty());
        Ability.playSound(level, performer, giveNightVision ? applySound : removeSound);
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.TOGGLEABLE_NIGHT_VISION.get();
    }
}
