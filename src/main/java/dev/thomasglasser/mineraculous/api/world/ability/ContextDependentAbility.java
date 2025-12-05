package dev.thomasglasser.mineraculous.api.world.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.context.BlockAbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.context.EntityAbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.handler.AbilityHandler;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import java.util.Optional;
import java.util.SortedSet;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Performs different abilities depending on the context provided in {@link Ability#perform}.
 *
 * @param blockAbility     The ability to perform when given a {@link BlockAbilityContext}
 * @param entityAbility    The ability to perform when given an {@link EntityAbilityContext}
 * @param passiveAbilities The abilities to perform when given a null {@link AbilityContext}
 */
public record ContextDependentAbility(Optional<Holder<Ability>> blockAbility, Optional<Holder<Ability>> entityAbility, HolderSet<Ability> passiveAbilities) implements AbilityWithSubAbilities {

    public static final MapCodec<ContextDependentAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ability.CODEC.optionalFieldOf("block").forGetter(ContextDependentAbility::blockAbility),
            Ability.CODEC.optionalFieldOf("entity").forGetter(ContextDependentAbility::entityAbility),
            Ability.HOLDER_SET_CODEC.optionalFieldOf("passive", HolderSet.empty()).forGetter(ContextDependentAbility::passiveAbilities)).apply(instance, ContextDependentAbility::new));
    @Override
    public State perform(AbilityData data, ServerLevel level, LivingEntity performer, AbilityHandler handler, @Nullable AbilityContext context) {
        switch (context) {
            case BlockAbilityContext blockAbilityContext when blockAbility.isPresent() -> {
                return blockAbility.get().value().perform(data, level, performer, handler, blockAbilityContext);
            }
            case EntityAbilityContext entityAbilityContext when entityAbility.isPresent() -> {
                return entityAbility.get().value().perform(data, level, performer, handler, entityAbilityContext);
            }
            case null -> {
                for (Holder<Ability> holder : passiveAbilities) {
                    State state = holder.value().perform(data, level, performer, handler, null);
                    if (state.shouldStop()) {
                        return state;
                    }
                }
            }
            default -> {}
        }
        return State.PASS;
    }

    @Override
    public SortedSet<Ability> getAll() {
        SortedSet<Ability> abilities = new ReferenceLinkedOpenHashSet<>();
        abilities.add(this);
        blockAbility.ifPresent(ability -> abilities.add(ability.value()));
        entityAbility.ifPresent(ability -> abilities.add(ability.value()));
        for (Holder<Ability> ability : passiveAbilities) {
            abilities.addAll(Ability.getAll(ability.value()));
        }
        return abilities;
    }

    @Override
    public SortedSet<Ability> getMatching(Predicate<Ability> predicate) {
        SortedSet<Ability> abilities = new ReferenceLinkedOpenHashSet<>();
        blockAbility.ifPresent(ability -> abilities.addAll(Ability.getMatching(predicate, ability.value())));
        entityAbility.ifPresent(ability -> abilities.addAll(Ability.getMatching(predicate, ability.value())));
        for (Holder<Ability> ability : passiveAbilities) {
            abilities.addAll(Ability.getMatching(predicate, ability.value()));
        }
        return abilities;
    }

    @Override
    public MapCodec<? extends Ability> codec() {
        return AbilitySerializers.CONTEXT_DEPENDENT.get();
    }
}
