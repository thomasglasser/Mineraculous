package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.network.ClientboundToggleNightVisionShaderPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public record AutomaticNightVisionAbility(Optional<ResourceLocation> shader) implements Ability {
    public static final MapCodec<AutomaticNightVisionAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("shader").forGetter(AutomaticNightVisionAbility::shader)).apply(instance, AutomaticNightVisionAbility::new));

    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        if (context == null && performer instanceof LivingEntity livingEntity) {
            checkNightVision(livingEntity, level);
            return true;
        }
        return false;
    }

    @Override
    public void detransform(AbilityData data, ServerLevel level, Entity performer) {
        if (performer instanceof ServerPlayer serverPlayer) {
            updateNightVision(serverPlayer, false);
        }
    }

    @Override
    public void joinLevel(AbilityData data, ServerLevel level, Entity performer) {
        if (performer instanceof LivingEntity livingEntity) {
            livingEntity.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    public void checkNightVision(LivingEntity entity, ServerLevel level) {
        boolean hasNightVision = entity.hasEffect(MobEffects.NIGHT_VISION);
        if (level.getRawBrightness(entity.blockPosition().above(), level.getSkyDarken()) <= 5) {
            if (!hasNightVision) {
                updateNightVision(entity, true);
            }
        } else if (hasNightVision) {
            updateNightVision(entity, false);
        }
    }

    private void updateNightVision(LivingEntity entity, boolean giveNightVision) {
        shader.ifPresent(shader -> {
            if (entity instanceof ServerPlayer player) {
                TommyLibServices.NETWORK.sendToClient(new ClientboundToggleNightVisionShaderPayload(giveNightVision, shader), player);
            }
        });
        if (giveNightVision) {
            entity.addEffect(MineraculousEntityEvents.INFINITE_HIDDEN_EFFECT.apply(MobEffects.NIGHT_VISION, 1));
        } else {
            entity.removeEffect(MobEffects.NIGHT_VISION);
        }
        entity.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withNightVisionShader(giveNightVision ? shader : Optional.empty()).save(entity, true);
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.NIGHT_VISION.get();
    }
}
