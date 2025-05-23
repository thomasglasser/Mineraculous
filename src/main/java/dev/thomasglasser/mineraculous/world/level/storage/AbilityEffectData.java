package dev.thomasglasser.mineraculous.world.level.storage;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public record AbilityEffectData(Optional<ResourceLocation> faceMaskTexture) {
    public AbilityEffectData() {
        this(Optional.empty());
    }

    public AbilityEffectData withFaceMaskTexture(ResourceLocation faceMaskTexture) {
        return new AbilityEffectData(Optional.of(faceMaskTexture));
    }
}
