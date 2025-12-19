package dev.thomasglasser.mineraculous.api.core.registries;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTargetType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class MineraculousRegistries {
    /**
     * Static registry holding {@link MapCodec}s for {@link Ability}s,
     * used in serializing the {@link MineraculousRegistries#ABILITY} registry.
     */
    public static final ResourceKey<Registry<MapCodec<? extends Ability>>> ABILITY_SERIALIZER = create("ability_serializer");
    /// Static registry holding {@link MiraculousLadybugTargetType}s for serializing {@link dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTarget}s.
    public static final ResourceKey<Registry<MiraculousLadybugTargetType<?>>> MIRACULOUS_LADYBUG_TARGET_TYPE = create("miraculous_ladybug_target_type");
    /// Static registry holding {@link LookContext}s for using and serializing {@link dev.thomasglasser.mineraculous.impl.client.look.Look}s.
    public static final ResourceKey<Registry<LookContext>> LOOK_CONTEXT = create("look_context");
    /// Data-driven registry holding {@link Ability}s based on {@link MineraculousRegistries#ABILITY_SERIALIZER} entries.
    public static final ResourceKey<Registry<Ability>> ABILITY = create("ability");
    /// Data-driven registry holding {@link Miraculous}es containing {@link MineraculousRegistries#ABILITY} entries.
    public static final ResourceKey<Registry<Miraculous>> MIRACULOUS = create("miraculous");
    /// Data-driven registry holding {@link Kamikotization}s containing {@link MineraculousRegistries#ABILITY} entries.
    public static final ResourceKey<Registry<Kamikotization>> KAMIKOTIZATION = create("kamikotization");

    private static <T> ResourceKey<Registry<T>> create(String name) {
        return ResourceKey.createRegistryKey(MineraculousConstants.modLoc(name));
    }
}
