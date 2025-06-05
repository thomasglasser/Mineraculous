package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;

public class AbilitySerializers {
    public static final DeferredRegister<MapCodec<? extends Ability>> ABILITIES = DeferredRegister.create(MineraculousRegistries.ABILITY_SERIALIZER, Mineraculous.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ApplyInfiniteEffectsOrDestroyAbility>> APPLY_INFINITE_EFFECTS_OR_DESTROY = ABILITIES.register("apply_infinite_effects_or_destroy", () -> ApplyInfiniteEffectsOrDestroyAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ContextDependentAbility>> CONTEXT_DEPENDENT = ABILITIES.register("context_dependent", () -> ContextDependentAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<DragAbility>> DRAG = ABILITIES.register("drag", () -> DragAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<RevertAbilityEffectsAbility>> RECOVER_ABILITY_DAMAGE = ABILITIES.register("lucky_charm_world_recovery", () -> RevertAbilityEffectsAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<AutomaticNightVisionAbility>> NIGHT_VISION = ABILITIES.register("night_vision", () -> AutomaticNightVisionAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<PassiveEffectsAbility>> PASSIVE_EFFECTS = ABILITIES.register("passive_effects", () -> PassiveEffectsAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ReplaceAdjacentBlocksAbility>> RANDOM_SPREAD = ABILITIES.register("random_spread", () -> ReplaceAdjacentBlocksAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ReplaceItemInHandAbility>> REPLACE_ITEMS_IN_HAND = ABILITIES.register("replace_items_in_hand", () -> ReplaceItemInHandAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<RightHandParticlesAbility>> RIGHT_HAND_PARTICLES = ABILITIES.register("right_hand_particles", () -> RightHandParticlesAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<SummonTargetDependentLuckyCharmAbility>> SUMMON_LUCKY_CHARM = ABILITIES.register("summon_lucky_charm", () -> SummonTargetDependentLuckyCharmAbility.CODEC);

    public static void init() {}
}
