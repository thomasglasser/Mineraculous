package dev.thomasglasser.mineraculous.impl.data.advancements.packs;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.advancements.ExtendedAdvancementGenerator;
import java.util.function.BiConsumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

public class MineraculousHusbandryAdvancements extends ExtendedAdvancementGenerator {
    public MineraculousHusbandryAdvancements(BiConsumer<String, String> lang) {
        super(MineraculousConstants.MOD_ID, "husbandry", lang);
    }

    @Override
    protected void generate(HolderLookup.Provider provider) {
        ResourceLocation root = ResourceLocation.withDefaultNamespace("husbandry/root");

        AdvancementHolder obtainCamembert = builder("obtain_camembert", MineraculousBlocks.CAMEMBERT.get(AgeingCheese.Age.FRESH).toStack(), "Smelly Cheese, Smelly Cheese", "What are they feeding you to?")
                .parent(root)
                .strategy(AdvancementRequirements.Strategy.OR)
                .trigger("obtain_camembert_wedge", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(MineraculousItemTags.CAMEMBERT)))
                .trigger("obtain_camembert_block", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(MineraculousItemTags.CAMEMBERT_BLOCKS)))
                .build();
    }
}
