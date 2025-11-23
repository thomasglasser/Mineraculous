package dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class MiraculousLadybugTargetTypes {
    private static final DeferredRegister<MiraculousLadybugTargetType<?>> MIRACULOUS_LADYBUG_TARGET_TYPES = DeferredRegister.create(MineraculousRegistries.MIRACULOUS_LADYBUG_TARGET_TYPE, MineraculousConstants.MOD_ID);

    public static final DeferredHolder<MiraculousLadybugTargetType<?>, MiraculousLadybugTargetType<MiraculousLadybugBlockTarget>> BLOCK = register("block", MiraculousLadybugBlockTarget.MAP_CODEC, MiraculousLadybugBlockTarget.STREAM_CODEC);
    public static final DeferredHolder<MiraculousLadybugTargetType<?>, MiraculousLadybugTargetType<MiraculousLadybugBlockClusterTarget>> BLOCK_CLUSTER = register("block_cluster", MiraculousLadybugBlockClusterTarget.MAP_CODEC, MiraculousLadybugBlockClusterTarget.STREAM_CODEC);
    public static final DeferredHolder<MiraculousLadybugTargetType<?>, MiraculousLadybugTargetType<MiraculousLadybugEntityTarget>> ENTITY = register("entity", MiraculousLadybugEntityTarget.MAP_CODEC, MiraculousLadybugEntityTarget.STREAM_CODEC);

    private static <T extends MiraculousLadybugTarget<T>> DeferredHolder<MiraculousLadybugTargetType<?>, MiraculousLadybugTargetType<T>> register(String name, MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        return MIRACULOUS_LADYBUG_TARGET_TYPES.register(name, () -> new MiraculousLadybugTargetType<>(codec, streamCodec));
    }

    public static void init() {}
}
