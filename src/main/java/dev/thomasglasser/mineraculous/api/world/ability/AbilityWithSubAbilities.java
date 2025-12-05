package dev.thomasglasser.mineraculous.api.world.ability;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTargetCollector;
import java.util.SortedSet;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/**
 * An {@link Ability} that delegates to or holds other {@link Ability}s.
 * Must provide abilities to ensure they are considered when searching or filtering.
 */
public interface AbilityWithSubAbilities extends Ability {
    @Override
    @OverridingMethodsMustInvokeSuper
    default void transform(AbilityData data, ServerLevel level, LivingEntity performer) {
        for (Ability ability : getAll()) {
            if (ability != this) {
                ability.transform(data, level, performer);
            }
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    default void detransform(AbilityData data, ServerLevel level, LivingEntity performer) {
        for (Ability ability : getAll()) {
            if (ability != this) {
                ability.detransform(data, level, performer);
            }
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    default void revert(AbilityData data, ServerLevel level, LivingEntity performer, MiraculousLadybugTargetCollector targetCollector) {
        for (Ability ability : getAll()) {
            if (ability != this) {
                ability.revert(data, level, performer, targetCollector);
            }
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    default void joinLevel(AbilityData data, ServerLevel level, LivingEntity performer) {
        for (Ability ability : getAll()) {
            if (ability != this) {
                ability.joinLevel(data, level, performer);
            }
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    default void leaveLevel(AbilityData data, ServerLevel level, LivingEntity performer) {
        for (Ability ability : getAll()) {
            if (ability != this) {
                ability.leaveLevel(data, level, performer);
            }
        }
    }

    /**
     * Collects a list of itself and all sub abilities.
     *
     * @return A list of itself and all sub abilities
     */
    SortedSet<Ability> getAll();

    /**
     * Collects a list of itself and all sub abilities filtered by the provided predicate.
     *
     * @param predicate The predicate to test abilities against
     * @return A list of itself and all sub abilities filtered by the provided predicate
     */
    SortedSet<Ability> getMatching(Predicate<Ability> predicate);
}
