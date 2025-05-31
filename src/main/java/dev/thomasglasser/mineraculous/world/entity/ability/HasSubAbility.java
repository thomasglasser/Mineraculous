package dev.thomasglasser.mineraculous.world.entity.ability;

import java.util.List;
import java.util.function.Predicate;

public interface HasSubAbility {
    List<Ability> getAll();

    List<Ability> getMatching(Predicate<Ability> predicate);
}
