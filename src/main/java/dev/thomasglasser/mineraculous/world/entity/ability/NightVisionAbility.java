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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class NightVisionAbility implements Ability {
    public static final MapCodec<NightVisionAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("shader").forGetter(NightVisionAbility::shader)).apply(instance, NightVisionAbility::new));

    private final Optional<ResourceLocation> shader;
    private final Optional<Holder<SoundEvent>> startSound;
    private boolean nightVision = false;

    public NightVisionAbility(Optional<ResourceLocation> shader, Optional<Holder<SoundEvent>> startSound) {
        this.shader = shader;
        this.startSound = startSound;
    }

    public NightVisionAbility(Optional<ResourceLocation> shader) {
        this(shader, Optional.empty());
    }

    public Optional<ResourceLocation> shader() {
        return shader;
    }

    public Optional<Holder<SoundEvent>> startSound() {
        return startSound;
    }

    @Override
    public boolean overrideActive() {
        return false;
    }

    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, Context context) {
        if (context == Context.PASSIVE) {
            checkNightVision((ServerPlayer) performer, level, pos);
            return true;
        }
        return false;
    }

    @Override
    public boolean canActivate(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        return performer instanceof ServerPlayer;
    }

    public void checkNightVision(ServerPlayer serverPlayer, Level level, BlockPos pos) {
        if (level.getRawBrightness(pos.above(), level.getSkyDarken()) <= 5) {
            if (!nightVision) {
                updateNightVision(serverPlayer, true);
            }
        } else if (nightVision) {
            updateNightVision(serverPlayer, false);
        }
    }

    protected void updateNightVision(ServerPlayer player, boolean nightVision) {
        shader.ifPresent(loc -> TommyLibServices.NETWORK.sendToClient(new ClientboundToggleNightVisionShaderPayload(nightVision, loc), player));
        if (nightVision) {
            player.addEffect(MineraculousEntityEvents.INFINITE_HIDDEN_EFFECT.apply(MobEffects.NIGHT_VISION, 1));
        } else {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
        player.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).withNightVisionShader(nightVision ? shader : Optional.empty()).save(player, true);
    }

    public void refreshNightVision(ServerPlayer serverPlayer) {
        if (nightVision)
            nightVision = false;
        checkNightVision(serverPlayer, serverPlayer.level(), serverPlayer.blockPosition());
    }

    @Override
    public void detransform(AbilityData data, ServerLevel level, Entity performer) {
        if (performer instanceof ServerPlayer serverPlayer) {
            updateNightVision(serverPlayer, false);
        }
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.NIGHT_VISION.get();
    }
}
