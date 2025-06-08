package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.thomasglasser.mineraculous.core.registries.MineraculousBuiltInRegistries;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
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
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface Ability {
    Codec<Ability> DIRECT_CODEC = MineraculousBuiltInRegistries.ABILITY_SERIALIZER.byNameCodec()
            .dispatch(Ability::codec, Function.identity());
    Codec<Holder<Ability>> CODEC = RegistryFileCodec.create(MineraculousRegistries.ABILITY, DIRECT_CODEC);
    Codec<HolderSet<Ability>> HOLDER_SET_CODEC = RegistryCodecs.homogeneousList(MineraculousRegistries.ABILITY, DIRECT_CODEC);

    StreamCodec<RegistryFriendlyByteBuf, Holder<Ability>> STREAM_CODEC = ByteBufCodecs.holderRegistry(MineraculousRegistries.ABILITY);
    StreamCodec<RegistryFriendlyByteBuf, HolderSet<Ability>> HOLDER_SET_STREAM_CODEC = ByteBufCodecs.holderSet(MineraculousRegistries.ABILITY);

    boolean perform(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context);

    default void transform(AbilityData data, ServerLevel level, Entity performer) {}

    default void detransform(AbilityData data, ServerLevel level, Entity performer) {}

    default void revert(AbilityData data, ServerLevel level, Entity performer) {}

    default void joinLevel(AbilityData data, ServerLevel level, Entity performer) {}

    default void leaveLevel(AbilityData data, ServerLevel level, Entity performer) {}

    MapCodec<? extends Ability> codec();

    static List<Ability> getAll(Ability ability) {
        List<Ability> abilities = new ReferenceArrayList<>();
        abilities.add(ability);
        if (ability instanceof AbilityWithSubAbilities abilityWithSubAbilities) {
            abilities.addAll(abilityWithSubAbilities.getAll());
        }
        return abilities;
    }

    static List<Ability> getMatching(Predicate<Ability> predicate, Ability ability) {
        List<Ability> abilities = new ReferenceArrayList<>();
        if (predicate.test(ability))
            abilities.add(ability);
        if (ability instanceof AbilityWithSubAbilities abilityWithSubAbilities)
            abilities.addAll(abilityWithSubAbilities.getMatching(predicate));
        return abilities;
    }

    static @Nullable Ability getFirstMatching(Predicate<Ability> predicate, Ability ability) {
        List<Ability> abilities = getMatching(predicate, ability);
        return abilities.isEmpty() ? null : abilities.getFirst();
    }

    static boolean hasMatching(Predicate<Ability> predicate, Ability ability) {
        return !getMatching(predicate, ability).isEmpty();
    }

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
}
