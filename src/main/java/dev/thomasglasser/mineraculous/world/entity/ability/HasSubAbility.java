package dev.thomasglasser.mineraculous.world.entity.ability;

import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

public interface HasSubAbility {
    @Nullable
    Ability getFirstMatching(Predicate<Ability> predicate);
}
