package dev.thomasglasser.mineraculous.api.core.registries;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousBuiltInRegistries {
    /// The static registry for {@link MineraculousRegistries#ABILITY_SERIALIZER}.
    public static final Registry<MapCodec<? extends Ability>> ABILITY_SERIALIZER = new RegistryBuilder<>(MineraculousRegistries.ABILITY_SERIALIZER).create();

    @ApiStatus.Internal
    public static void init() {}
}
