package dev.thomasglasser.mineraculous.api.world.item;

import dev.thomasglasser.mineraculous.api.world.ability.RevertLuckyCharmTargetsAbilityEffectsAbility;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTargetCollector;
import net.minecraft.world.entity.LivingEntity;

/// Enables an item to revert trackable effects.
public interface EffectRevertingItem {
    /**
     * Called by {@link RevertLuckyCharmTargetsAbilityEffectsAbility} to revert this item's trackable effects.
     *
     * @param entity          The entity to revert effects for
     * @param targetCollector The target collector to add {@link dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTarget}s
     */
    void revert(LivingEntity entity, MiraculousLadybugTargetCollector targetCollector);
}
