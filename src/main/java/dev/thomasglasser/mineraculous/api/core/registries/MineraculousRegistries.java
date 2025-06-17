package dev.thomasglasser.mineraculous.api.core.registries;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class MineraculousRegistries {
    public static final ResourceKey<Registry<MapCodec<? extends Ability>>> ABILITY_SERIALIZER = create("ability_serializer");
    public static final ResourceKey<Registry<Ability>> ABILITY = create("ability");
    public static final ResourceKey<Registry<Miraculous>> MIRACULOUS = create("miraculous");
    public static final ResourceKey<Registry<Kamikotization>> KAMIKOTIZATION = create("kamikotization");

    private static <T> ResourceKey<Registry<T>> create(String name) {
        return ResourceKey.createRegistryKey(Mineraculous.modLoc(name));
    }
}
