package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousBuiltInRegistries;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionBlockData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionItemData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Performs actions given a context, with additional points of interest.
 * Backed by serializers in {@link MineraculousRegistries#ABILITY_SERIALIZER} matching {@link Ability#codec()}
 */
public interface Ability {
    Codec<Ability> DIRECT_CODEC = MineraculousBuiltInRegistries.ABILITY_SERIALIZER.byNameCodec()
            .dispatch(Ability::codec, Function.identity());
    Codec<Holder<Ability>> CODEC = RegistryFileCodec.create(MineraculousRegistries.ABILITY, DIRECT_CODEC);
    Codec<HolderSet<Ability>> HOLDER_SET_CODEC = RegistryCodecs.homogeneousList(MineraculousRegistries.ABILITY, DIRECT_CODEC);

    StreamCodec<RegistryFriendlyByteBuf, Holder<Ability>> STREAM_CODEC = ByteBufCodecs.holderRegistry(MineraculousRegistries.ABILITY);
    StreamCodec<RegistryFriendlyByteBuf, HolderSet<Ability>> HOLDER_SET_STREAM_CODEC = ByteBufCodecs.holderSet(MineraculousRegistries.ABILITY);

    /**
     * Performs actions based on the given context.
     *
     * @param data      The relevant {@link AbilityData} of the performer
     * @param level     The level the ability is being performed in
     * @param performer The performer of the ability
     * @param handler   The handler of the ability
     * @param context   The context of the ability (null if passive)
     * @return Whether the ability should consume the active state (i.e., stop the ability and trigger completion)
     */
    State perform(AbilityData data, ServerLevel level, LivingEntity performer, AbilityHandler handler, @Nullable AbilityContext context);

    /**
     * Called when the performer transforms.
     * This can mean different things depending on how it's used.
     * It is simply a starting point where the ability becomes relevant and,
     * if passive, starts being performed.
     * 
     * @param data      The relevant {@link AbilityData} of the performer
     * @param level     The level the ability is being performed in
     * @param performer The performer of the ability
     */
    default void transform(AbilityData data, ServerLevel level, LivingEntity performer) {}

    /**
     * Called when the performer detransforms.
     * This can mean different things depending on how its used.
     * It is simply a stopping point where the ability is no longer relevant and,
     * if passive, stops being performed.
     * 
     * @param data      The relevant {@link AbilityData} of the performer
     * @param level     The level the ability is being performed in
     * @param performer The performer of the ability
     */
    default void detransform(AbilityData data, ServerLevel level, LivingEntity performer) {}

    /**
     * Called by {@link RevertLuckyCharmTargetsAbilityEffectsAbility} to revert this ability's trackable effects.
     * This should use {@link AbilityReversionItemData},
     * {@link AbilityReversionEntityData}, and {@link AbilityReversionBlockData} for ease and compat.
     * 
     * @param data      The relevant {@link AbilityData} of the performer
     * @param level     The level the ability is being performed in
     * @param performer The performer of the ability
     */
    default void revert(AbilityData data, ServerLevel level, LivingEntity performer) {}

    /**
     * Called when the performer joins a new {@link Level}.
     * Should be used to ensure data is properly handled on dimension change or world entrance.
     * 
     * @param data      The relevant {@link AbilityData} of the performer
     * @param level     The level the performer just joined
     * @param performer The performer of the ability
     */
    default void joinLevel(AbilityData data, ServerLevel level, LivingEntity performer) {}

    /**
     * Called when the performer leaves their current {@link Level}.
     * Should be used to ensure data is properly handled on dimension change or world exit.
     * 
     * @param data      The relevant {@link AbilityData} of the performer
     * @param level     The level the performer just left
     * @param performer The performer of the ability
     */
    default void leaveLevel(AbilityData data, ServerLevel level, LivingEntity performer) {}

    /**
     * The dispatch {@link MapCodec} that defines and constructs the ability.
     * Should point to an entry in {@link MineraculousRegistries#ABILITY_SERIALIZER}.
     * 
     * @return The ability dispatch codec
     */
    MapCodec<? extends Ability> codec();

    /**
     * Plays a sound (if present) at the performer's block position with the performer's {@link SoundSource}.
     * 
     * @param level     The level the ability is being performed in
     * @param performer The performer of the ability
     * @param sound     The optional sound to play if present
     */
    static void playSound(ServerLevel level, LivingEntity performer, Optional<Holder<SoundEvent>> sound) {
        sound.ifPresent(soundEvent -> level.playSound(null, performer.blockPosition(), soundEvent.value(), performer.getSoundSource(), 1, 1));
    }

    /**
     * Collects all abilities in an ability, including sub abilities.
     * 
     * @param ability The ability to collect sub abilities from
     * @return A list with the passed ability and all contained sub abilities
     */
    static List<Ability> getAll(Ability ability) {
        List<Ability> abilities = new ReferenceArrayList<>();
        abilities.add(ability);
        if (ability instanceof AbilityWithSubAbilities abilityWithSubAbilities) {
            abilities.addAll(abilityWithSubAbilities.getAll());
        }
        return abilities;
    }

    /**
     * Collects all abilities matching the provided predicate in an ability, including sub abilities.
     * 
     * @param predicate The predicate to filter abilities with
     * @param ability   The ability to test and collect matching sub abilities from
     * @return A list with any matching of the passed ability and all contained sub abilities
     */
    static List<Ability> getMatching(Predicate<Ability> predicate, Ability ability) {
        List<Ability> abilities = new ReferenceArrayList<>();
        if (predicate.test(ability))
            abilities.add(ability);
        if (ability instanceof AbilityWithSubAbilities abilityWithSubAbilities)
            abilities.addAll(abilityWithSubAbilities.getMatching(predicate));
        return abilities;
    }

    /**
     * Finds the first ability or sub ability matching the provided predicate from the provided ability.
     * 
     * @param predicate The predicate to filter abilities with
     * @param ability   The ability to test and test sub abilities from
     * @return The matching ability or first sub ability matching the provided predicate
     */
    static @Nullable Ability getFirstMatching(Predicate<Ability> predicate, Ability ability) {
        List<Ability> abilities = getMatching(predicate, ability);
        return abilities.isEmpty() ? null : abilities.getFirst();
    }

    /**
     * Checks if the provided ability or any sub ability match the provided predicate.
     * 
     * @param predicate The predicate to check abilities
     * @param ability   The ability to test and test sub abilities from
     * @return Whether the ability or any sub abilities match the provided predicate
     */
    static boolean hasMatching(Predicate<Ability> predicate, Ability ability) {
        return !getMatching(predicate, ability).isEmpty();
    }

    // Overloads for active and passive abilities
    static List<Ability> getAll(Optional<Holder<Ability>> activeAbility, HolderSet<Ability> passiveAbilities) {
        List<Ability> abilities = new ReferenceArrayList<>();
        activeAbility.ifPresent(ability -> abilities.addAll(getAll(ability.value())));
        for (Holder<Ability> ability : passiveAbilities) {
            abilities.addAll(getAll(ability.value()));
        }
        return abilities;
    }

    static List<Ability> getMatching(Predicate<Ability> predicate, Optional<Holder<Ability>> activeAbility, HolderSet<Ability> passiveAbilities) {
        List<Ability> abilities = new ReferenceArrayList<>();
        activeAbility.ifPresent(ability -> abilities.addAll(getMatching(predicate, ability.value())));
        for (Holder<Ability> ability : passiveAbilities) {
            abilities.addAll(getMatching(predicate, ability.value()));
        }
        return abilities;
    }

    static @Nullable Ability getFirstMatching(Predicate<Ability> predicate, Optional<Holder<Ability>> activeAbility, HolderSet<Ability> passiveAbilities) {
        List<Ability> abilities = getMatching(predicate, activeAbility, passiveAbilities);
        return abilities.isEmpty() ? null : abilities.getFirst();
    }

    static boolean hasMatching(Predicate<Ability> predicate, Optional<Holder<Ability>> activeAbility, HolderSet<Ability> passiveAbilities) {
        return !getMatching(predicate, activeAbility, passiveAbilities).isEmpty();
    }

    // Overloads for Miraculous abilities
    static List<Ability> getAll(Miraculous miraculous, boolean includeActive) {
        return getAll(Optional.ofNullable(includeActive ? miraculous.activeAbility() : null), miraculous.passiveAbilities());
    }

    static List<Ability> getMatching(Predicate<Ability> predicate, Miraculous miraculous, boolean includeActive) {
        return getMatching(predicate, Optional.ofNullable(includeActive ? miraculous.activeAbility() : null), miraculous.passiveAbilities());
    }

    static @Nullable Ability getFirstMatching(Predicate<Ability> predicate, Miraculous miraculous, boolean includeActive) {
        return getFirstMatching(predicate, Optional.ofNullable(includeActive ? miraculous.activeAbility() : null), miraculous.passiveAbilities());
    }

    static boolean hasMatching(Predicate<Ability> predicate, Miraculous miraculous, boolean includeActive) {
        return hasMatching(predicate, Optional.ofNullable(includeActive ? miraculous.activeAbility() : null), miraculous.passiveAbilities());
    }

    // Overloads for Kamikotization abilities
    static List<Ability> getAll(Kamikotization kamikotization, boolean includeActive) {
        return getAll(kamikotization.powerSource().right().filter(a -> includeActive), kamikotization.passiveAbilities());
    }

    static List<Ability> getMatching(Predicate<Ability> predicate, Kamikotization kamikotization, boolean includeActive) {
        return getMatching(predicate, kamikotization.powerSource().right().filter(a -> includeActive), kamikotization.passiveAbilities());
    }

    static @Nullable Ability getFirstMatching(Predicate<Ability> predicate, Kamikotization kamikotization, boolean includeActive) {
        return getFirstMatching(predicate, kamikotization.powerSource().right().filter(a -> includeActive), kamikotization.passiveAbilities());
    }

    static boolean hasMatching(Predicate<Ability> predicate, Kamikotization kamikotization, boolean includeActive) {
        return hasMatching(predicate, kamikotization.powerSource().right().filter(a -> includeActive), kamikotization.passiveAbilities());
    }

    enum State {
        CONSUME,
        PASS,
        CANCEL;

        public boolean shouldStop() {
            return this == CANCEL || this == CONSUME;
        }

        public boolean isSuccess() {
            return this == CONSUME;
        }
    }
}
