package dev.thomasglasser.mineraculous.api.core.registries;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class MineraculousBuiltInRegistries {
    public static final Registry<MapCodec<? extends Ability>> ABILITY_SERIALIZER = new RegistryBuilder<>(MineraculousRegistries.ABILITY_SERIALIZER).create();

    public static void init() {}
}
