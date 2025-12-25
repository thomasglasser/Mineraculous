package dev.thomasglasser.mineraculous.api.core.look.metadata;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.HolderSetCodec;
import org.jetbrains.annotations.ApiStatus;

public class LookMetadataTypes {
    private static final DeferredRegister<LookMetadataType<?>> LOOK_METADATA_TYPES = DeferredRegister.create(MineraculousRegistries.LOOK_METADATA_TYPE, MineraculousConstants.MOD_ID);

    /// The miraculous that the look can apply to.
    public static final DeferredHolder<LookMetadataType<?>, LookMetadataType<HolderSet<Miraculous>>> VALID_MIRACULOUSES = LOOK_METADATA_TYPES.register("valid_miraculouses", () -> new LookMetadataType<>(HolderSetCodec.create(MineraculousRegistries.MIRACULOUS, Miraculous.CODEC, false)));
    /// The kamikotizations that the look can apply to.
    public static final DeferredHolder<LookMetadataType<?>, LookMetadataType<HolderSet<Kamikotization>>> VALID_KAMIKOTIZATIONS = LOOK_METADATA_TYPES.register("valid_kamikotizations", () -> new LookMetadataType<>(HolderSetCodec.create(MineraculousRegistries.KAMIKOTIZATION, Kamikotization.CODEC, false)));

    @ApiStatus.Internal
    public static void init() {}
}
