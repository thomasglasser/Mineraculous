package dev.thomasglasser.mineraculous.world.entity.ability;

import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public interface AbilityWithSubAbilities extends Ability {
    @Override
    default void transform(AbilityData data, ServerLevel level, Entity performer) {
        for (Ability ability : getAll()) {
            if (ability != this) {
                ability.transform(data, level, performer);
            }
        }
    }

    @Override
    default void detransform(AbilityData data, ServerLevel level, Entity performer) {
        for (Ability ability : getAll()) {
            if (ability != this) {
                ability.detransform(data, level, performer);
            }
        }
    }

    @Override
    default void revert(AbilityData data, ServerLevel level, Entity performer) {
        for (Ability ability : getAll()) {
            if (ability != this) {
                ability.revert(data, level, performer);
            }
        }
    }

    @Override
    default void joinLevel(AbilityData data, ServerLevel level, Entity performer) {
        for (Ability ability : getAll()) {
            if (ability != this) {
                ability.joinLevel(data, level, performer);
            }
        }
    }

    @Override
    default void leaveLevel(AbilityData data, ServerLevel level, Entity performer) {
        for (Ability ability : getAll()) {
            if (ability != this) {
                ability.leaveLevel(data, level, performer);
            }
        }
    }

    List<Ability> getAll();

    List<Ability> getMatching(Predicate<Ability> predicate);
}
