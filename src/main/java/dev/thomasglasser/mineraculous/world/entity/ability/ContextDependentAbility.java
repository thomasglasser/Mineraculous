package dev.thomasglasser.mineraculous.world.entity.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.entity.ability.context.AbilityContext;
import dev.thomasglasser.mineraculous.world.entity.ability.context.BlockAbilityContext;
import dev.thomasglasser.mineraculous.world.entity.ability.context.EntityAbilityContext;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public record ContextDependentAbility(Optional<Holder<Ability>> blockAbility, Optional<Holder<Ability>> entityAbility, HolderSet<Ability> passiveAbilities) implements AbilityWithSubAbilities {

    public static final MapCodec<ContextDependentAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ability.CODEC.optionalFieldOf("block").forGetter(ContextDependentAbility::blockAbility),
            Ability.CODEC.optionalFieldOf("entity").forGetter(ContextDependentAbility::entityAbility),
            Ability.HOLDER_SET_CODEC.optionalFieldOf("passive", HolderSet.empty()).forGetter(ContextDependentAbility::passiveAbilities)).apply(instance, ContextDependentAbility::new));
    @Override
    public boolean perform(AbilityData data, ServerLevel level, Entity performer, @Nullable AbilityContext context) {
        switch (context) {
            case BlockAbilityContext blockAbilityContext when blockAbility.isPresent() -> {
                return blockAbility.get().value().perform(data, level, performer, blockAbilityContext);
            }
            case EntityAbilityContext entityAbilityContext when entityAbility.isPresent() -> {
                return entityAbility.get().value().perform(data, level, performer, entityAbilityContext);
            }
            case null -> {
                for (Holder<Ability> holder : passiveAbilities) {
                    if (holder.value().perform(data, level, performer, null)) {
                        return true;
                    }
                }
            }
            default -> {}
        }
        return false;
    }

    @Override
    public List<Ability> getAll() {
        List<Ability> abilities = new ReferenceArrayList<>();
        abilities.add(this);
        blockAbility.ifPresent(ability -> abilities.add(ability.value()));
        entityAbility.ifPresent(ability -> abilities.add(ability.value()));
        for (Holder<Ability> ability : passiveAbilities) {
            abilities.addAll(Ability.getAll(ability.value()));
        }
        return abilities;
    }

    @Override
    public List<Ability> getMatching(Predicate<Ability> predicate) {
        List<Ability> abilities = new ReferenceArrayList<>();
        abilities.add(this);
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
