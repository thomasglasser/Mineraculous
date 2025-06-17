package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.impl.network.ClientboundToggleNightVisionShaderPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public record AutomaticNightVisionAbility(Optional<ResourceLocation> shader, Optional<Holder<SoundEvent>> applySound, Optional<Holder<SoundEvent>> removeSound) implements Ability {

    public static final MapCodec<AutomaticNightVisionAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("shader").forGetter(AutomaticNightVisionAbility::shader),
            SoundEvent.CODEC.optionalFieldOf("apply_sound").forGetter(AutomaticNightVisionAbility::applySound),
            SoundEvent.CODEC.optionalFieldOf("remove_sound").forGetter(AutomaticNightVisionAbility::removeSound)).apply(instance, AutomaticNightVisionAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        if (context == null && performer instanceof LivingEntity livingEntity) {
            checkNightVision(level, livingEntity);
        }
        return false;
    }

    @Override
    public void detransform(AbilityData data, ServerLevel level, Entity performer) {
        if (performer instanceof LivingEntity livingEntity) {
            updateNightVision(level, livingEntity, false);
        }
    }

    @Override
    public void joinLevel(AbilityData data, ServerLevel level, Entity performer) {
        if (performer instanceof LivingEntity livingEntity) {
            checkNightVision(level, livingEntity);
        }
    }

    public void checkNightVision(ServerLevel level, LivingEntity performer) {
        boolean hasNightVision = performer.hasEffect(MobEffects.NIGHT_VISION);
        if (level.getRawBrightness(performer.blockPosition().above(), level.getSkyDarken()) <= 5) {
            if (!hasNightVision) {
                updateNightVision(level, performer, true);
            }
        } else if (hasNightVision) {
            updateNightVision(level, performer, false);
        }
    }

    private void updateNightVision(ServerLevel level, LivingEntity performer, boolean giveNightVision) {
        shader.ifPresent(shader -> {
            if (performer instanceof ServerPlayer player) {
                TommyLibServices.NETWORK.sendToClient(new ClientboundToggleNightVisionShaderPayload(giveNightVision, shader), player);
            }
        });
        if (giveNightVision) {
            MineraculousEntityUtils.applyInfiniteHiddenEffect(performer, MobEffects.NIGHT_VISION, 1);
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
