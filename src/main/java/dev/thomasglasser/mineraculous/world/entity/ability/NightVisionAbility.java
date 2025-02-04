package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.network.ClientboundToggleNightVisionPayload;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

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

    public NightVisionAbility() {
        this(Optional.empty(), Optional.empty());
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
    public boolean perform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity, Context context) {
        if (context == Context.PASSIVE) {
            checkNightVision((ServerPlayer) entity, level, pos);
            return true;
        }
        return false;
    }

    @Override
    public boolean canActivate(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {
        return entity instanceof ServerPlayer;
    }

    public void checkNightVision(ServerPlayer serverPlayer, Level level, BlockPos pos) {
        if (level.getRawBrightness(pos.above(), level.getSkyDarken()) <= 5) {
            if (!nightVision) {
                enableNightVision(serverPlayer);
            }
        } else if (nightVision) {
            disableNightVision(serverPlayer);
        }
    }

    protected void enableNightVision(ServerPlayer serverPlayer) {
        nightVision = true;
        playStartSound(serverPlayer.serverLevel(), serverPlayer.blockPosition());
        TommyLibServices.NETWORK.sendToClient(new ClientboundToggleNightVisionPayload(true, shader), serverPlayer);
        CompoundTag tag = TommyLibServices.ENTITY.getPersistentData(serverPlayer);
        tag.putBoolean(MineraculousEntityEvents.TAG_HAS_NIGHT_VISION, nightVision);
        TommyLibServices.ENTITY.setPersistentData(serverPlayer, tag, false);
    }

    protected void disableNightVision(ServerPlayer serverPlayer) {
        nightVision = false;
        TommyLibServices.NETWORK.sendToClient(new ClientboundToggleNightVisionPayload(false, shader), serverPlayer);
        CompoundTag tag = TommyLibServices.ENTITY.getPersistentData(serverPlayer);
        tag.putBoolean(MineraculousEntityEvents.TAG_HAS_NIGHT_VISION, false);
        TommyLibServices.ENTITY.setPersistentData(serverPlayer, tag, false);
    }

    public void resetNightVision(ServerPlayer serverPlayer) {
        if (nightVision)
            nightVision = false;
        checkNightVision(serverPlayer, serverPlayer.level(), serverPlayer.blockPosition());
    }

    @Override
    public void detransform(AbilityData data, ServerLevel level, BlockPos pos, LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            disableNightVision(serverPlayer);
        }
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.NIGHT_VISION.get();
    }
}
