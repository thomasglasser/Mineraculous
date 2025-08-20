package dev.thomasglasser.mineraculous.api.world.ability;

import dev.thomasglasser.mineraculous.api.advancements.critereon.KamikotizationPredicate;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.tags.MineraculousBlockTags;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.damagesource.MineraculousDamageTypes;
import dev.thomasglasser.mineraculous.api.world.effect.MineraculousMobEffects;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
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
import org.jetbrains.annotations.ApiStatus;

public class Abilities {
    // Butterfly
    /// Converts an entity in {@link MineraculousEntityTypeTags#BUTTERFLIES} to a tamed {@link Kamiko} on right click.
    public static final ResourceKey<Ability> KAMIKOTIZATION = create("kamikotization");
    /// Toggles spectation of a nearby {@link Kamiko}.
    public static final ResourceKey<Ability> KAMIKO_CONTROL = create("kamiko_control");
    /// Toggles spectation of a nearby kamikotized entity with a private chat and remote damage.
    public static final ResourceKey<Ability> KAMIKOTIZED_COMMUNICATION = create("kamikotized_communication");

    // Cat
    /**
     * Replaces a block with a {@link MineraculousBlocks#CATACLYSM_BLOCK} on right click,
     * applies {@link MineraculousMobEffects#CATACLYSM} to
     * (or destroys if not applicable) an entity on right click,
     * or replaces an item with {@link MineraculousItems#CATACLYSM_DUST} when held.
     * Continuous for 1 second.
     */
    public static final ResourceKey<Ability> CATACLYSM = create("cataclysm");
    /// Automatic Night Vision with a green tint.
    public static final ResourceKey<Ability> CAT_VISION = create("cat_vision");
    /// Passively applies {@link MobEffects#UNLUCK}.
    public static final ResourceKey<Ability> PASSIVE_UNLUCK = create("passive_unluck");

    // Ladybug
    /// Summons a target-dependent lucky charm when tool in hand.
    public static final ResourceKey<Ability> LUCKY_CHARM = create("lucky_charm");
    /// Reverts target and related ability effects when lucky charm in hand.
    public static final ResourceKey<Ability> MIRACULOUS_LADYBUG = create("miraculous_ladybug");
    /// Passively applies {@link MobEffects#LUCK}.
    public static final ResourceKey<Ability> PASSIVE_LUCK = create("passive_luck");

    private static final ResourceLocation KAMIKO_FACE_MASK_TEXTURE = Mineraculous.modLoc("textures/entity/player/face_mask/kamiko.png");

    private static ResourceKey<Ability> create(String name) {
        return ResourceKey.create(MineraculousRegistries.ABILITY, Mineraculous.modLoc(name));
    }

    @ApiStatus.Internal
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
                Optional.of(EntityPredicate.Builder.entity().of(MineraculousEntityTypes.KAMIKO.get()).build()),
                Optional.empty(),
                false,
                false,
                true,
                Optional.of(Kamiko.SPECTATOR_SHADER),
                Optional.of(KAMIKO_FACE_MASK_TEXTURE),
                Optional.empty(),
                Optional.empty()));
        context.register(KAMIKOTIZED_COMMUNICATION, new SpectateEntityAbility(
                Optional.of(EntityPredicate.Builder.entity().subPredicate(KamikotizationPredicate.ANY).build()),
                Optional.empty(),
                true,
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
                Optional.of(Holder.direct(new ApplyEffectsOrDestroyAbility(
                        HolderSet.direct(MineraculousMobEffects.CATACLYSM),
                        new ApplyEffectsOrDestroyAbility.EffectSettings(-1, false, false),
                        Optional.of(MineraculousDamageTypes.CATACLYSM),
                        true,
                        true,
                        Optional.of(MineraculousItems.CATACLYSM_DUST.get()),
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
        context.register(CAT_VISION, new AutomaticNightVisionAbility(Optional.of(Mineraculous.modLoc("shaders/post/cat_vision.json")), Optional.empty(), Optional.empty()));
        context.register(PASSIVE_UNLUCK, new PassiveEffectsAbility(HolderSet.direct(MobEffects.UNLUCK), 0));

        // Ladybug
        context.register(LUCKY_CHARM, new SummonTargetDependentLuckyCharmAbility(true, Optional.of(MineraculousSoundEvents.LUCKY_CHARM_ACTIVATE)));
        context.register(MIRACULOUS_LADYBUG, new RevertLuckyCharmTargetsAbilityEffectsAbility(Optional.of(MineraculousSoundEvents.MIRACULOUS_LADYBUG_ACTIVATE)));
        context.register(PASSIVE_LUCK, new PassiveEffectsAbility(HolderSet.direct(MobEffects.LUCK), 0));
    }
}
