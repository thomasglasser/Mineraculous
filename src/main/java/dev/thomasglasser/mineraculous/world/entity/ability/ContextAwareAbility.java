package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public record ContextAwareAbility(Optional<Holder<Ability>> blockAbility, Optional<Holder<Ability>> entityAbility, Optional<Holder<Ability>> itemAbility, Optional<Holder<Ability>> airAbility, List<Holder<Ability>> passiveAbilities, Optional<Holder<SoundEvent>> startSound, boolean overrideActive) implements Ability, HasSubAbility {

    public static final MapCodec<ContextAwareAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ability.CODEC.optionalFieldOf("block").forGetter(ContextAwareAbility::blockAbility),
            Ability.CODEC.optionalFieldOf("entity").forGetter(ContextAwareAbility::entityAbility),
            Ability.CODEC.optionalFieldOf("item").forGetter(ContextAwareAbility::itemAbility),
            Ability.CODEC.optionalFieldOf("air").forGetter(ContextAwareAbility::airAbility),
            Ability.CODEC.listOf().optionalFieldOf("passive", List.of()).forGetter(ContextAwareAbility::passiveAbilities),
            SoundEvent.CODEC.optionalFieldOf("start_sound").forGetter(ContextAwareAbility::startSound),
            Codec.BOOL.optionalFieldOf("override_active", false).forGetter(ContextAwareAbility::overrideActive)).apply(instance, ContextAwareAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, Context context) {
        return switch (context) {
            case INTERACT_BLOCK -> blockAbility.map(ability -> ability.value().perform(data, level, performer, context)).orElse(false);
            case INTERACT_ENTITY -> entityAbility.map(ability -> ability.value().perform(data, level, performer, context)).orElse(false);
            case INTERACT_ITEM -> itemAbility.map(ability -> ability.value().perform(data, level, performer, context)).orElse(false);
            case INTERACT_AIR -> airAbility.map(ability -> ability.value().perform(data, level, performer, context)).orElse(false);
            case PASSIVE -> {
                passiveAbilities.forEach(ability -> ability.value().perform(data, level, performer, context));
                yield false;
            }
        };
    }

    @Override
    public boolean canActivate(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        return blockAbility.map(ability -> ability.value().canActivate(data, level, performer, )).orElse(true) &&
                entityAbility.map(ability -> ability.value().canActivate(data, level, performer, )).orElse(true) &&
                itemAbility.map(ability -> ability.value().canActivate(data, level, performer, )).orElse(true) &&
                airAbility.map(ability -> ability.value().canActivate(data, level, performer, )).orElse(true) &&
                passiveAbilities.stream().allMatch(ability -> ability.value().canActivate(data, level, performer, ));
    }

    @Override
    public void restore(AbilityData data, ServerLevel level, Entity performer) {
        blockAbility.ifPresent(ability -> ability.value().restore(data, level, performer));
        entityAbility.ifPresent(ability -> ability.value().restore(data, level, performer));
        itemAbility.ifPresent(ability -> ability.value().restore(data, level, performer));
        airAbility.ifPresent(ability -> ability.value().restore(data, level, performer));
        passiveAbilities.forEach(ability -> ability.value().restore(data, level, performer));
    }

    @Override
    public List<Ability> getAll() {
        List<Ability> abilities = new ReferenceArrayList<>();
        abilities.add(this);
        abilities.addAll(blockAbility.map(ability -> Ability.getAll(ability.value())).orElse(List.of()));
        abilities.addAll(entityAbility.map(ability -> Ability.getAll(ability.value())).orElse(List.of()));
        abilities.addAll(itemAbility.map(ability -> Ability.getAll(ability.value())).orElse(List.of()));
        abilities.addAll(airAbility.map(ability -> Ability.getAll(ability.value())).orElse(List.of()));
        for (Holder<Ability> ability : passiveAbilities) {
            abilities.addAll(Ability.getAll(ability.value()));
        }
        return abilities;
    }

    @Override
    public List<Ability> getMatching(Predicate<Ability> predicate) {
        List<Ability> abilities = new ArrayList<>();
        abilities.add(this);
        abilities.addAll(blockAbility.map(ability -> Ability.getMatching(predicate, ability.value())).orElse(List.of()));
        abilities.addAll(entityAbility.map(ability -> Ability.getMatching(predicate, ability.value())).orElse(List.of()));
        abilities.addAll(itemAbility.map(ability -> Ability.getMatching(predicate, ability.value())).orElse(List.of()));
        abilities.addAll(airAbility.map(ability -> Ability.getMatching(predicate, ability.value())).orElse(List.of()));
        for (Holder<Ability> ability : passiveAbilities) {
            abilities.addAll(Ability.getMatching(predicate, ability.value()));
        }
        return abilities;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return MineraculousAbilitySerializers.CONTEXT_AWARE.get();
    }
}
