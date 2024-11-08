package dev.thomasglasser.mineraculous.world.entity.miraculous.ability;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.tags.MineraculousBlockTags;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.damagesource.MineraculousDamageTypes;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class MineraculousAbilities {
    public static final ResourceKey<Ability> KAMIKOTIZATION = register("kamikotization");
    public static final ResourceKey<Ability> KAMIKO_CONTROL = register("kamiko_control");
    public static final ResourceKey<Ability> CATACLYSM = register("cataclysm");
    public static final ResourceKey<Ability> CAT_VISION = register("cat_vision");
    public static final ResourceKey<Ability> MIRACULOUS_LADYBUG = register("miraculous_ladybug");

    private static ResourceKey<Ability> register(String id) {
        return ResourceKey.create(MineraculousRegistries.ABILITY, Mineraculous.modLoc(id));
    }

    public static void bootstrap(BootstrapContext<Ability> context) {
        // TODO: Implement Kamikotization and Kamiko Control abilities

        context.register(CATACLYSM, new ContextAwareAbility(
                Optional.of(new RandomDirectionalSpreadAbility(
                        MineraculousBlocks.CATACLYSM_BLOCK.get().defaultBlockState(),
                        Optional.empty(),
                        Optional.of(context.lookup(Registries.BLOCK).getOrThrow(MineraculousBlockTags.CATACLYSM_IMMUNE)))),
                Optional.of(new ApplyInfiniteEffectsOrDestroyAbility(
                        HolderSet.direct(MineraculousMobEffects.CATACLYSMED),
                        Optional.of(MineraculousItems.CATACLYSM_DUST.get()),
                        Optional.of(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(MineraculousDamageTypes.CATACLYSM).key()))),
                Optional.of(new ReplaceItemsInHandAbility(
                        MineraculousItems.CATACLYSM_DUST.toStack(),
                        Optional.empty(),
                        Optional.of(context.lookup(Registries.ITEM).getOrThrow(MineraculousItemTags.CATACLYSM_IMMUNE)))),
                Optional.empty(),
                Optional.of(List.of(
                        new RightHandParticlesAbility(MineraculousParticleTypes.CATACLYSM.get())))));
        context.register(CAT_VISION, new NightVisionAbility(Optional.of(ResourceLocation.withDefaultNamespace("shaders/post/creeper.json"))));

        // TODO: Implement Miraculous Ladybug ability
    }
}
