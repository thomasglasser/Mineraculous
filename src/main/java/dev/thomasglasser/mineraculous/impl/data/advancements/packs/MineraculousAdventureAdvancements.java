package dev.thomasglasser.mineraculous.impl.data.advancements.packs;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.advancements.critereon.CatStaffPredicate;
import dev.thomasglasser.mineraculous.api.advancements.critereon.ReleasedPurifiedEntitiesTrigger;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.tags.MineraculousEntityTypeTags;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.world.item.component.Active;
import dev.thomasglasser.tommylib.api.data.advancements.ExtendedAdvancementGenerator;
import java.util.function.BiConsumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class MineraculousAdventureAdvancements extends ExtendedAdvancementGenerator {
    public MineraculousAdventureAdvancements(BiConsumer<String, String> lang) {
        super(MineraculousConstants.MOD_ID, "adventure", lang);
    }

    @Override
    protected void generate(HolderLookup.Provider provider) {
        ResourceLocation root = ResourceLocation.withDefaultNamespace("adventure/root");

        AdvancementHolder releasePurifiedButterfly = builder("release_purified_butterfly", MineraculousItems.LADYBUG_YOYO.toStack(), "Bye Bye, Little Butterfly", "Purify and release a butterfly")
                .parent(root)
                .experience(20)
                .trigger("release_purified_butterfly", ReleasedPurifiedEntitiesTrigger.TriggerInstance.releasedPurifiedEntities(EntityPredicate.Builder.entity().of(MineraculousEntityTypeTags.BUTTERFLIES)))
                .build();

        ItemStack catStaff = MineraculousItems.CAT_STAFF.toStack();
        catStaff.set(MineraculousDataComponents.ACTIVE, new Active(true, true));
        AdvancementHolder perchAtWorldHeight = builder("perch_at_world_height", catStaff, "Into the Catmosphere", "Perch above the top of the world (build limit) using the Cat Staff")
                .parent(root)
                .experience(20)
                .trigger("perch_at_world_height", PlayerTrigger.TriggerInstance.located(EntityPredicate.Builder.entity().subPredicate(CatStaffPredicate.perching(true)).located(LocationPredicate.Builder.atYLocation(MinMaxBounds.Doubles.atLeast(319.0)))))
                .build();
    }
}
