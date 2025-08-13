package dev.thomasglasser.mineraculous.api.world.item;

import dev.thomasglasser.mineraculous.api.world.ability.RevertLuckyCharmTargetsAbilityEffectsAbility;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionBlockData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionItemData;
import net.minecraft.world.entity.Entity;

/// Enables an item to revert trackable effects via {@link RevertLuckyCharmTargetsAbilityEffectsAbility}.
public interface EffectRevertingItem {
    /**
     * Called by {@link RevertLuckyCharmTargetsAbilityEffectsAbility} to revert this item's trackable effects.
     * This should use {@link AbilityReversionItemData},
     * {@link AbilityReversionEntityData}, and {@link AbilityReversionBlockData} for ease and compat.
     *
     * @param entity The entity to revert effects for
     */
    void revert(Entity entity);
}
