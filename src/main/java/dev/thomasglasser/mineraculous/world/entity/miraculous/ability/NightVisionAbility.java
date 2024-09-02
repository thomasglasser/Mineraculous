package dev.thomasglasser.mineraculous.world.entity.miraculous.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.network.ClientboundToggleNightVisionPayload;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class NightVisionAbility implements Ability {
    public static final MapCodec<NightVisionAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("shader").forGetter(NightVisionAbility::shader)).apply(instance, NightVisionAbility::new));

    Optional<ResourceLocation> shader;
    private boolean nightVision = false;

    public NightVisionAbility(Optional<ResourceLocation> shader) {
        this.shader = shader;
    }

    public NightVisionAbility() {
        this(Optional.empty());
    }

    public Optional<ResourceLocation> shader() {
        return shader;
    }

    @Override
    public boolean perform(ResourceKey<Miraculous> type, MiraculousData data, Level level, BlockPos pos, LivingEntity performer, Context context) {
        if (context == Context.PASSIVE && performer instanceof ServerPlayer serverPlayer) {
            checkNightVision(serverPlayer, level, pos);
            return true;
        }
        return false;
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
        TommyLibServices.NETWORK.sendToClient(new ClientboundToggleNightVisionPayload(true, shader), serverPlayer);
        CompoundTag tag = TommyLibServices.ENTITY.getPersistentData(serverPlayer);
        tag.putBoolean(MineraculousEntityEvents.TAG_HASNIGHTVISION, nightVision);
        TommyLibServices.ENTITY.setPersistentData(serverPlayer, tag, false);
    }

    protected void disableNightVision(ServerPlayer serverPlayer) {
        nightVision = false;
        TommyLibServices.NETWORK.sendToClient(new ClientboundToggleNightVisionPayload(false, shader), serverPlayer);
        CompoundTag tag = TommyLibServices.ENTITY.getPersistentData(serverPlayer);
        tag.putBoolean(MineraculousEntityEvents.TAG_HASNIGHTVISION, nightVision);
        TommyLibServices.ENTITY.setPersistentData(serverPlayer, tag, false);
    }

    public void resetNightVision(ServerPlayer serverPlayer) {
        if (nightVision)
            nightVision = false;
        checkNightVision(serverPlayer, serverPlayer.level(), serverPlayer.blockPosition());
    }

    @Override
    public void detransform(ResourceKey<Miraculous> type, MiraculousData data, Level level, BlockPos pos, LivingEntity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            disableNightVision(serverPlayer);
        }
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.NIGHT_VISION.get();
    }
}
