package dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects;

import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import net.minecraft.world.entity.Entity;

/**
 * Holds information used by existing {@link Ability}s.
 *
 * @param spectationInterrupted   Whether spectation should be interrupted for the entity
 * @param shouldToggleNightVision Whether night vision should be toggled for the entity
 */
public record TransientAbilityEffectData(boolean spectationInterrupted, boolean shouldToggleNightVision) {
    public TransientAbilityEffectData() {
        this(false, false);
    }

    public TransientAbilityEffectData withSpectationInterrupted(boolean spectationInterrupted) {
        return new TransientAbilityEffectData(spectationInterrupted, shouldToggleNightVision);
    }

    public TransientAbilityEffectData withShouldToggleNightVision(boolean shouldToggleNightVision) {
        return new TransientAbilityEffectData(spectationInterrupted, shouldToggleNightVision);
    }

    public void save(Entity entity) {
        entity.setData(MineraculousAttachmentTypes.TRANSIENT_ABILITY_EFFECTS, this);
    }
}
