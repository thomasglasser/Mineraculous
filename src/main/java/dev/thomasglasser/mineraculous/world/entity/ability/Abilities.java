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
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import java.util.Optional;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;

public class Abilities {
    // Butterfly
    public static final ResourceKey<Ability> KAMIKOTIZATION = create("kamikotization");
    public static final ResourceKey<Ability> KAMIKO_CONTROL = create("kamiko_control");
    public static final ResourceKey<Ability> KAMIKOTIZED_COMMUNICATION = create("kamikotized_communication");

    // Cat
    public static final ResourceKey<Ability> CATACLYSM = create("cataclysm");
    public static final ResourceKey<Ability> CAT_VISION = create("cat_vision");
    public static final ResourceKey<Ability> PASSIVE_UNLUCK = create("passive_unluck");

    // Ladybug
    public static final ResourceKey<Ability> LUCKY_CHARM = create("lucky_charm");
    public static final ResourceKey<Ability> MIRACULOUS_LADYBUG = create("miraculous_ladybug");
    public static final ResourceKey<Ability> PASSIVE_LUCK = create("passive_luck");

    private static final ResourceLocation KAMIKO_FACE_MASK_TEXTURE = Mineraculous.modLoc("textures/entity/player/face_mask/kamiko.png");

    private static ResourceKey<Ability> create(String name) {
        return ResourceKey.create(MineraculousRegistries.ABILITY, Mineraculous.modLoc(name));
    }

    public static void bootstrap(BootstrapContext<Ability> context) {
        context.register(KAMIKOTIZATION, new ContextDependentAbility(
                Optional.empty(),
                Optional.of(Holder.direct(new ConvertAndTameAbility(
                        MineraculousEntityTypes.KAMIKO.get(),
                        Optional.of(EntityPredicate.Builder.entity()/*.of(MineraculousEntityTypeTags.BUTTERFLIES)*/.build()),
                        Optional.empty(),
                        Optional.of(MineraculousSoundEvents.KAMIKOTIZATION_ACTIVATE)))),
                HolderSet.direct(Holder.direct(new RightHandParticlesAbility(MineraculousParticleTypes.BLACK_ORB.get())))));
        context.register(KAMIKO_CONTROL, new SpectateEntityAbility(
                EntityPredicate.Builder.entity().of(MineraculousEntityTypes.KAMIKO.get()).build(),
                false,
                false,
                Optional.of(Kamiko.SPECTATOR_SHADER),
                Optional.of(KAMIKO_FACE_MASK_TEXTURE),
                Optional.empty(),
                Optional.empty()));
        context.register(KAMIKOTIZED_COMMUNICATION, new SpectateEntityAbility(
                EntityPredicate.Builder.entity().subPredicate(KamikotizationPredicate.ANY).build(),
                true,
                true,
                Optional.of(Kamiko.SPECTATOR_SHADER),
                Optional.of(KAMIKO_FACE_MASK_TEXTURE),
                Optional.of(MineraculousSoundEvents.KAMIKOTIZED_COMMUNICATION_ACTIVATE),
                Optional.empty()));

        // Cat
        context.register(CATACLYSM, new ContinuousAbility(Holder.direct(new ContextDependentAbility(
                Optional.of(Holder.direct(new ReplaceAdjacentBlocksAbility(
                        MineraculousBlocks.CATACLYSM_BLOCK.get().defaultBlockState(),
                        true,
                        Optional.empty(),
                        Optional.of(BlockPredicate.Builder.block().of(MineraculousBlockTags.CATACLYSM_IMMUNE).build()),
                        Optional.of(MineraculousSoundEvents.CATACLYSM_USE)))),
                Optional.of(Holder.direct(new ApplyInfiniteEffectsOrDestroyAbility(
                        HolderSet.direct(MineraculousMobEffects.CATACLYSM),
                        new ApplyInfiniteEffectsOrDestroyAbility.EffectSettings(false, false, true),
                        Optional.of(MineraculousItems.CATACLYSM_DUST.get()),
                        Optional.of(MineraculousDamageTypes.CATACLYSM),
                        true,
                        true,
                        Optional.of(MineraculousSoundEvents.CATACLYSM_USE)))),
                HolderSet.direct(
                        Holder.direct(new RightHandParticlesAbility(MineraculousParticleTypes.BLACK_ORB.get())),
                        Holder.direct(new ReplaceItemInMainHandAbility(
                                MineraculousItems.CATACLYSM_DUST.toStack(),
                                true,
                                Optional.empty(),
                                Optional.of(ItemPredicate.Builder.item().of(MineraculousItemTags.CATACLYSM_IMMUNE).build()),
                                Optional.of(MineraculousSoundEvents.CATACLYSM_USE)))))),
                Optional.of(MineraculousSoundEvents.CATACLYSM_ACTIVATE),
                Optional.empty(),
                Optional.empty()));
        context.register(CAT_VISION, new AutomaticNightVisionAbility(Optional.of(ResourceLocation.withDefaultNamespace("shaders/post/creeper.json")), Optional.empty(), Optional.empty()));
        context.register(PASSIVE_UNLUCK, new PassiveEffectsAbility(HolderSet.direct(MobEffects.UNLUCK), 0));

        // Ladybug
        context.register(LUCKY_CHARM, new SummonTargetDependentLuckyCharmAbility(true, Optional.of(MineraculousSoundEvents.LUCKY_CHARM_ACTIVATE)));
        context.register(MIRACULOUS_LADYBUG, new RevertLuckyCharmTargetsAbilityEffectsAbility(Optional.of(MineraculousSoundEvents.MIRACULOUS_LADYBUG_ACTIVATE)));
        context.register(PASSIVE_LUCK, new PassiveEffectsAbility(HolderSet.direct(MobEffects.LUCK), 0));
    }
}
