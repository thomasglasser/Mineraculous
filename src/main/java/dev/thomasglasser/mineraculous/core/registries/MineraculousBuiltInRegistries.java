package dev.thomasglasser.mineraculous.core.registries;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.Ability;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class MineraculousBuiltInRegistries {
    public static final Registry<MapCodec<? extends Ability>> ABILITY_SERIALIZER = new RegistryBuilder<>(MineraculousRegistries.ABILITY_SERIALIZER).create();

    public static void init() {}
}
