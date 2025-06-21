package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

public class AbilitySerializers {
    private static final DeferredRegister<MapCodec<? extends Ability>> ABILITIES = DeferredRegister.create(MineraculousRegistries.ABILITY_SERIALIZER, Mineraculous.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ApplyEffectsOrDestroyAbility>> APPLY_EFFECTS_OR_DESTROY = ABILITIES.register("apply_effects_or_destroy", () -> ApplyEffectsOrDestroyAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<AutomaticNightVisionAbility>> AUTOMATIC_NIGHT_VISION = ABILITIES.register("automatic_night_vision", () -> AutomaticNightVisionAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ContextDependentAbility>> CONTEXT_DEPENDENT = ABILITIES.register("context_dependent", () -> ContextDependentAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ContinuousAbility>> CONTINUOUS = ABILITIES.register("continuous", () -> ContinuousAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ConvertAndTameAbility>> CONVERT_AND_TAME = ABILITIES.register("convert_and_tame", () -> ConvertAndTameAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<PassiveEffectsAbility>> PASSIVE_EFFECTS = ABILITIES.register("passive_effects", () -> PassiveEffectsAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ReplaceAdjacentBlocksAbility>> REPLACE_ADJACENT_BLOCKS = ABILITIES.register("replace_adjacent_blocks", () -> ReplaceAdjacentBlocksAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ReplaceItemInMainHandAbility>> REPLACE_ITEM_IN_MAIN_HAND = ABILITIES.register("replace_item_in_main_hand", () -> ReplaceItemInMainHandAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<RevertLuckyCharmTargetsAbilityEffectsAbility>> REVERT_LUCKY_CHARM_TARGETS_ABILITY_EFFECTS = ABILITIES.register("revert_lucky_charm_targets_ability_effects", () -> RevertLuckyCharmTargetsAbilityEffectsAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<RightHandParticlesAbility>> RIGHT_HAND_PARTICLES = ABILITIES.register("right_hand_particles", () -> RightHandParticlesAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<SpectateEntityAbility>> SPECTATE_ENTITY = ABILITIES.register("spectate_entity", () -> SpectateEntityAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<SummonTargetDependentLuckyCharmAbility>> SUMMON_TARGET_DEPENDENT_LUCKY_CHARM = ABILITIES.register("summon_target_dependent_lucky_charm", () -> SummonTargetDependentLuckyCharmAbility.CODEC);

    @ApiStatus.Internal
    public static void init() {}
}
