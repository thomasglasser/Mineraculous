package dev.thomasglasser.mineraculous.api.tags;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.tags.TagUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class MineraculousEntityTypeTags {
    /// Entities from any mod that can be considered a butterfly.
    public static final TagKey<EntityType<?>> BUTTERFLIES = createC("butterflies");

    private static TagKey<EntityType<?>> create(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, Mineraculous.modLoc(name));
    }

    private static TagKey<EntityType<?>> createC(String name) {
        return TagUtils.createConventional(Registries.ENTITY_TYPE, name);
    }
}
