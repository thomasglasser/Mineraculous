package dev.thomasglasser.mineraculous.data.advancements.packs;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.advancements.critereon.MiraculousTransformTrigger;
import dev.thomasglasser.mineraculous.advancements.critereon.MiraculousUsePowerTrigger;
import dev.thomasglasser.mineraculous.tags.MineraculousDamageTypeTags;
import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.level.block.CheeseBlock;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.advancements.ExtendedAdvancementGenerator;
import java.util.Map;
import java.util.Optional;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class MineraculousMiraculousAdvancements extends ExtendedAdvancementGenerator {
    public MineraculousMiraculousAdvancements(LanguageProvider enUs) {
        super(Mineraculous.MOD_ID, "miraculous", enUs);
    }

    @Override
    public void generate(HolderLookup.Provider provider) {
        HolderGetter<Item> items = provider.lookupOrThrow(Registries.ITEM);

        AdvancementHolder root = root(Miraculous.createItemStack(MineraculousItems.MIRACULOUS.get(), MineraculousMiraculous.LADYBUG), "root", ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png") /* TODO: Custom mod-related background */, AdvancementType.TASK, false, false, false, null, AdvancementRequirements.Strategy.AND, Map.of(
                "get_miraculous", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items, MineraculousItems.MIRACULOUS))), "Miraculous", "A hero's journey begins...");

        AdvancementHolder transformButterfly = create(root, Miraculous.createItemStack(MineraculousItems.MIRACULOUS.get(), MineraculousMiraculous.BUTTERFLY), "transform_butterfly", AdvancementType.TASK, true, true, false, AdvancementRewards.Builder.experience(10).build(), AdvancementRequirements.Strategy.AND, Map.of(
                "transform_butterfly", MiraculousTransformTrigger.TriggerInstance.transformed(MineraculousMiraculous.BUTTERFLY)), "Wings Rise!", "Transform using the Butterfly miraculous");

        AdvancementHolder transformCat = create(root, Miraculous.createItemStack(MineraculousItems.MIRACULOUS.get(), MineraculousMiraculous.CAT), "transform_cat", AdvancementType.TASK, true, true, false, AdvancementRewards.Builder.experience(10).build(), AdvancementRequirements.Strategy.AND, Map.of(
                "transform_cat", MiraculousTransformTrigger.TriggerInstance.transformed(MineraculousMiraculous.CAT)), "Claws Out!", "Transform using the Cat miraculous");

        AdvancementHolder cataclysmBlock = create(transformCat, MineraculousBlocks.CATACLYSM_BLOCK, "cataclysm_block", AdvancementType.TASK, true, true, false, null, AdvancementRequirements.Strategy.AND, Map.of(
                "cataclysm_block", MiraculousUsePowerTrigger.TriggerInstance.usedPower(MineraculousMiraculous.CAT, MiraculousUsePowerTrigger.Context.BLOCK)), "My Castle Crumbled Overnight", "Cataclysm a block and watch it spread");

        AdvancementHolder cataclysmEntity = create(transformCat, MineraculousItems.CATACLYSM_DUST, "cataclysm_living_entity", AdvancementType.TASK, true, true, false, null, AdvancementRequirements.Strategy.AND, Map.of(
                "cataclysm_entity", MiraculousUsePowerTrigger.TriggerInstance.usedPower(MineraculousMiraculous.CAT, MiraculousUsePowerTrigger.Context.LIVING_ENTITY)), "What Have You Done?!", "Cataclysm a living entity");

        AdvancementHolder cataclysmKillEntity = create(cataclysmEntity, MineraculousItems.CATACLYSM_DUST, "cataclysm_kill_living_entity", AdvancementType.TASK, true, true, true, AdvancementRewards.Builder.experience(20).build(), AdvancementRequirements.Strategy.AND, Map.of(
                "cataclysm_kill_entity", KilledTrigger.TriggerInstance.playerKilledEntity(Optional.empty(), DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(MineraculousDamageTypeTags.IS_CATACLYSM)))), "Dead and Gone and Buried", "Cataclysm an entity that dies before it is healed");

        AdvancementHolder obtainCamembert = create(transformCat, MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.FRESH), "obtain_camembert", AdvancementType.TASK, true, true, false, null, AdvancementRequirements.Strategy.OR, Map.of(
                "obtain_camembert", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items, MineraculousItemTags.CAMEMBERT))), "Smelly Cheese, Smelly Cheese", "What are they feeding you to?");

        AdvancementHolder transformLadybug = create(root, Miraculous.createItemStack(MineraculousItems.MIRACULOUS.get(), MineraculousMiraculous.LADYBUG), "transform_ladybug", AdvancementType.TASK, true, true, false, AdvancementRewards.Builder.experience(10).build(), AdvancementRequirements.Strategy.AND, Map.of(
                "transform_ladybug", MiraculousTransformTrigger.TriggerInstance.transformed(MineraculousMiraculous.LADYBUG)), "Spots On!", "Transform using the Ladybug miraculous");
    }
}
