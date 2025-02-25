package dev.thomasglasser.mineraculous.world.entity.ability;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.advancements.critereon.KamikotizationPredicate;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.tags.MineraculousBlockTags;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.damagesource.MineraculousDamageTypes;
import dev.thomasglasser.mineraculous.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;

public class MineraculousAbilities {
    public static final ResourceKey<Ability> KAMIKOTIZATION = register("kamikotization");
    public static final ResourceKey<Ability> KAMIKO_CONTROL = register("kamiko_control");
    public static final ResourceKey<Ability> KAMIKOTIZED_COMMUNICATION = register("kamikotized_communication");
    public static final ResourceKey<Ability> CATACLYSM = register("cataclysm");
    public static final ResourceKey<Ability> CAT_VISION = register("cat_vision");
    public static final ResourceKey<Ability> PASSIVE_UNLUCK = register("passive_unluck");
    public static final ResourceKey<Ability> LUCKY_CHARM = register("lucky_charm");
    public static final ResourceKey<Ability> MIRACULOUS_LADYBUG = register("miraculous_ladybug");
    public static final ResourceKey<Ability> PASSIVE_LUCK = register("passive_luck");

    private static ResourceKey<Ability> register(String id) {
        return ResourceKey.create(MineraculousRegistries.ABILITY, Mineraculous.modLoc(id));
    }

    public static void bootstrap(BootstrapContext<Ability> context) {
        context.register(KAMIKOTIZATION, new ContextAwareAbility(
                Optional.empty(),
                Optional.of(new SetOwnerAbility(
                        Optional.of(1),
                        Optional.of(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(MineraculousEntityTypes.KAMIKO.get())).build()),
                        Optional.empty(),
                        Optional.of(MineraculousSoundEvents.KAMIKOTIZATION_ACTIVATE),
                        false)),
                Optional.empty(),
                Optional.empty(),
                List.of(new RightHandParticlesAbility(MineraculousParticleTypes.BLACK_ORB.get(), Optional.empty(), false)),
                Optional.empty(),
                false));
        context.register(KAMIKO_CONTROL, new SetCameraEntityAbility(
                EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(MineraculousEntityTypes.KAMIKO.get())).build(),
                Optional.empty(),
                Optional.of(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK),
                true,
                true,
                Optional.empty(),
                true));
        context.register(KAMIKOTIZED_COMMUNICATION, new SetCameraEntityAbility(
                EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.PLAYER)).subPredicate(KamikotizationPredicate.ANY).build(),
                Optional.of(Kamiko.SPECTATOR_SHADER),
                Optional.of(MineraculousEntityEvents.TAG_SHOW_KAMIKO_MASK),
                false,
                false,
                Optional.empty(),
                true));

        context.register(CATACLYSM, new DragAbility(new ContextAwareAbility(
                Optional.of(new RandomSpreadAbility(
                        MineraculousBlocks.CATACLYSM_BLOCK.get().defaultBlockState(),
                        Optional.empty(),
                        Optional.of(BlockPredicate.Builder.block().of(MineraculousBlockTags.CATACLYSM_IMMUNE).build()),
                        Optional.of(MineraculousSoundEvents.CATACLYSM_USE),
                        false)),
                Optional.of(new ApplyInfiniteEffectsOrDestroyAbility(
                        HolderSet.direct(MineraculousMobEffects.CATACLYSMED),
                        Optional.of(MineraculousItems.CATACLYSM_DUST.get()),
                        Optional.of(MineraculousDamageTypes.CATACLYSM),
                        Optional.of(MineraculousEntityEvents.TAG_CATACLYSMED),
                        Optional.of(MineraculousSoundEvents.CATACLYSM_USE),
                        false)),
                Optional.of(new ReplaceItemsInHandAbility(
                        MineraculousItems.CATACLYSM_DUST.toStack(),
                        true,
                        Optional.empty(),
                        Optional.of(ItemPredicate.Builder.item().of(MineraculousItemTags.CATACLYSM_IMMUNE).build()),
                        Optional.of(MineraculousSoundEvents.CATACLYSM_USE),
                        false)),
                Optional.empty(),
                List.of(new RightHandParticlesAbility(MineraculousParticleTypes.BLACK_ORB.get(), Optional.empty(), false)),
                Optional.empty(),
                false),
                20,
                Optional.of(MineraculousSoundEvents.CATACLYSM_ACTIVATE),
                false));
        context.register(CAT_VISION, new NightVisionAbility(Optional.of(ResourceLocation.withDefaultNamespace("shaders/post/creeper.json"))));
        context.register(PASSIVE_UNLUCK, new ApplyEffectsWhileTransformedAbility(
                HolderSet.direct(MobEffects.UNLUCK),
                1,
                Optional.empty()));

        context.register(LUCKY_CHARM, new SummonLuckyCharmAbility(true, Optional.of(MineraculousSoundEvents.LUCKY_CHARM_ACTIVATE), false));
        context.register(MIRACULOUS_LADYBUG, new LuckyCharmWorldRecoveryAbility(true, Optional.of(MineraculousParticleTypes.SPREADING_LADYBUG.get()), Optional.of(MineraculousSoundEvents.MIRACULOUS_LADYBUG_ACTIVATE), true));
        context.register(PASSIVE_LUCK, new ApplyEffectsWhileTransformedAbility(
                HolderSet.direct(MobEffects.LUCK),
                1,
                Optional.empty()));
    }
}
