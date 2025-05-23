package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;

public class MineraculousAbilitySerializers {
    public static final DeferredRegister<MapCodec<? extends Ability>> ABILITIES = DeferredRegister.create(MineraculousRegistries.ABILITY_SERIALIZER, Mineraculous.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ApplyEffectsWhileTransformedAbility>> APPLY_EFFECTS_WHILE_TRANSFORMED = ABILITIES.register("apply_effects_while_transformed", () -> ApplyEffectsWhileTransformedAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ApplyInfiniteEffectsOrDestroyAbility>> APPLY_INFINITE_EFFECTS_OR_DESTROY = ABILITIES.register("apply_infinite_effects_or_destroy", () -> ApplyInfiniteEffectsOrDestroyAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ContextAwareAbility>> CONTEXT_AWARE = ABILITIES.register("context_aware", () -> ContextAwareAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<DragAbility>> DRAG = ABILITIES.register("drag", () -> DragAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<LuckyCharmWorldRecoveryAbility>> LUCKY_CHARM_WORLD_RECOVERY = ABILITIES.register("lucky_charm_world_recovery", () -> LuckyCharmWorldRecoveryAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<NightVisionAbility>> NIGHT_VISION = ABILITIES.register("night_vision", () -> NightVisionAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<RandomSpreadAbility>> RANDOM_SPREAD = ABILITIES.register("random_spread", () -> RandomSpreadAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ReplaceItemsInHandAbility>> REPLACE_ITEMS_IN_HAND = ABILITIES.register("replace_items_in_hand", () -> ReplaceItemsInHandAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<RightHandParticlesAbility>> RIGHT_HAND_PARTICLES = ABILITIES.register("right_hand_particles", () -> RightHandParticlesAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<SetCameraEntityAbility>> SET_CAMERA_ENTITY = ABILITIES.register("set_camera_entity", () -> SetCameraEntityAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<SetOwnerAbility>> SET_OWNER = ABILITIES.register("set_owner", () -> SetOwnerAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<SummonLuckyCharmAbility>> SUMMON_LUCKY_CHARM = ABILITIES.register("summon_lucky_charm", () -> SummonLuckyCharmAbility.CODEC);

    public static void init() {}
}
