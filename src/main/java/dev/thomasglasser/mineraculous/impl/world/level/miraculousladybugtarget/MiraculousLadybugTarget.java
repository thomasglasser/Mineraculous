package dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousBuiltInRegistries;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface MiraculousLadybugTarget<T extends MiraculousLadybugTarget<T>> {
    Codec<MiraculousLadybugTarget<?>> CODEC = MineraculousBuiltInRegistries.MIRACULOUS_LADYBUG_TARGET_TYPE.byNameCodec()
            .dispatch(MiraculousLadybugTarget::type, MiraculousLadybugTargetType::codec);
    StreamCodec<RegistryFriendlyByteBuf, MiraculousLadybugTarget<?>> STREAM_CODEC = ByteBufCodecs.registry(MineraculousRegistries.MIRACULOUS_LADYBUG_TARGET_TYPE)
            .dispatch(MiraculousLadybugTarget::type, MiraculousLadybugTargetType::streamCodec);

    MiraculousLadybugTargetType<T> type();

    @Nullable
    MiraculousLadybugTarget<T> revert(ServerLevel level, boolean instant);

    default MiraculousLadybugTarget<T> tick(ServerLevel level) {
        return this;
    }

    Vec3 position();

    List<Vec3> getControlPoints();

    default boolean isReverting() {
        return false;
    }

    default boolean shouldExpandMiraculousLadybug() {
        return false;
    }
}
