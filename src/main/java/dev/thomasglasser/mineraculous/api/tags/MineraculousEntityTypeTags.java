package dev.thomasglasser.mineraculous.api.tags;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.tommylib.api.tags.TagUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class MineraculousEntityTypeTags {
    /// Entities that cannot be affected by {@link dev.thomasglasser.mineraculous.api.world.ability.Abilities#CATACLYSM}.
    public static final TagKey<EntityType<?>> CATACLYSM_IMMUNE = create("cataclysm_immune");

    /// Entities from any mod that can be considered a butterfly.
    public static final TagKey<EntityType<?>> BUTTERFLIES = createC("butterflies");

    /// Entities that can be hit from a further distance by the {@link dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo}.
    public static final TagKey<EntityType<?>> LADYBUG_YOYO_EXTENDED_RANGE = create("ladybug_yoyo_extended_range");

    private static TagKey<EntityType<?>> create(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, MineraculousConstants.modLoc(name));
    }

    private static TagKey<EntityType<?>> createC(String name) {
        return TagUtils.createConventional(Registries.ENTITY_TYPE, name);
    }
}
