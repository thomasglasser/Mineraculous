package dev.thomasglasser.mineraculous.world.entity.miraculous.ability;

import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;

public class MineraculousAbilitySerializers {
    public static final DeferredRegister<MapCodec<? extends Ability>> ABILITIES = DeferredRegister.create(MineraculousRegistries.ABILITY_SERIALIZER, Mineraculous.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ApplyInfiniteEffectsOrDestroyAbility>> APPLY_INFINITE_EFFECTS_OR_DESTROY = ABILITIES.register("apply_infinite_effects_or_destroy", () -> ApplyInfiniteEffectsOrDestroyAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ContextAwareAbility>> CONTEXT_AWARE = ABILITIES.register("context_aware", () -> ContextAwareAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<RandomDirectionalSpreadAbility>> RANDOM_DIRECTIONAL_SPREAD = ABILITIES.register("random_directional_spread", () -> RandomDirectionalSpreadAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<ReplaceItemsInHandAbility>> REPLACE_ITEMS_IN_HAND = ABILITIES.register("replace_items_in_hand", () -> ReplaceItemsInHandAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<RightHandParticlesAbility>> RIGHT_HAND_PARTICLES = ABILITIES.register("right_hand_particles", () -> RightHandParticlesAbility.CODEC);
    public static final DeferredHolder<MapCodec<? extends Ability>, MapCodec<NightVisionAbility>> NIGHT_VISION = ABILITIES.register("night_vision", () -> NightVisionAbility.CODEC);

    public static void init() {}
}
