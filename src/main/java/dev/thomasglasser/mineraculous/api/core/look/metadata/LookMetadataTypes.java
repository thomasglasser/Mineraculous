package dev.thomasglasser.mineraculous.api.core.look.metadata;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

public class LookMetadataTypes {
    private static final DeferredRegister<LookMetadataType<?>> LOOK_METADATA_TYPES = DeferredRegister.create(MineraculousRegistries.LOOK_METADATA_TYPE, MineraculousConstants.MOD_ID);

    /// The miraculous that the look can apply to.
    public static final DeferredHolder<LookMetadataType<?>, LookMetadataType<Set<ResourceKey<Miraculous>>>> VALID_MIRACULOUSES = LOOK_METADATA_TYPES.register("valid_miraculouses", () -> new LookMetadataType<>(ResourceKey.codec(MineraculousRegistries.MIRACULOUS).listOf().xmap(ImmutableSet::copyOf, ImmutableList::copyOf)));
    /// The kamikotizations that the look can apply to.
    public static final DeferredHolder<LookMetadataType<?>, LookMetadataType<Set<ResourceKey<Kamikotization>>>> VALID_KAMIKOTIZATIONS = LOOK_METADATA_TYPES.register("valid_kamikotizations", () -> new LookMetadataType<>(ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).listOf().xmap(ImmutableSet::copyOf, ImmutableList::copyOf)));

    @ApiStatus.Internal
    public static void init() {}
}
