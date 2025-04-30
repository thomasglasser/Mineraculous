package dev.thomasglasser.mineraculous.data.advancements.packs;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.data.advancements.ExtendedAdvancementGenerator;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class MineraculousMiraculousAdvancements extends ExtendedAdvancementGenerator {
    public MineraculousMiraculousAdvancements(LanguageProvider enUs) {
        super(Mineraculous.MOD_ID, "miraculous", enUs);
    }

    @Override
    public void generate(HolderLookup.Provider provider) {
//        AdvancementHolder root = root(Miraculous.createMiraculousStack(MineraculousMiraculous.LADYBUG), "root", Mineraculous.modLoc("textures/gui/advancements/backgrounds/miraculous.png"), AdvancementType.TASK, false, false, false, null, AdvancementRequirements.Strategy.AND, Map.of(
//                "get_miraculous", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(MineraculousItems.MIRACULOUS))), "Miraculous", "A hero's journey begins...");
//
//        AdvancementHolder transformLadybug = create(root, Miraculous.createMiraculousStack(MineraculousMiraculous.LADYBUG), "transform_ladybug", AdvancementType.TASK, true, true, false, AdvancementRewards.Builder.experience(10).build(), AdvancementRequirements.Strategy.AND, Map.of(
//                "transform_ladybug", MiraculousTransformTrigger.TriggerInstance.transformed(MineraculousMiraculous.LADYBUG)), "Spots On!", "Transform using the Ladybug miraculous");
//
//        AdvancementHolder releasePurifiedKamiko = create(transformLadybug, MineraculousItems.LADYBUG_YOYO, "release_purified_kamiko", AdvancementType.TASK, true, true, false, null, AdvancementRequirements.Strategy.AND, Map.of(
//                "release_purified_kamiko", ReleasePurifiedKamikoTrigger.TriggerInstance.releasedPurifiedKamiko()), "Bye Bye, Little Butterfly", "Purify and release a Kamiko");
//
//        AdvancementHolder transformCat = create(root, Miraculous.createMiraculousStack(MineraculousMiraculous.CAT), "transform_cat", AdvancementType.TASK, true, true, false, AdvancementRewards.Builder.experience(10).build(), AdvancementRequirements.Strategy.AND, Map.of(
//                "transform_cat", MiraculousTransformTrigger.TriggerInstance.transformed(MineraculousMiraculous.CAT)), "Claws Out!", "Transform using the Cat miraculous");
//
//        AdvancementHolder cataclysmBlock = create(transformCat, MineraculousBlocks.CATACLYSM_BLOCK, "cataclysm_block", AdvancementType.TASK, true, true, false, null, AdvancementRequirements.Strategy.AND, Map.of(
//                "cataclysm_block", MiraculousUsePowerTrigger.TriggerInstance.usedPower(MineraculousMiraculous.CAT, MiraculousUsePowerTrigger.Context.BLOCK)), "My Castle Crumbled Overnight", "Cataclysm a block and watch it spread");
//
//        AdvancementHolder cataclysmEntity = create(transformCat, MineraculousItems.CATACLYSM_DUST, "cataclysm_living_entity", AdvancementType.TASK, true, true, false, null, AdvancementRequirements.Strategy.AND, Map.of(
//                "cataclysm_entity", MiraculousUsePowerTrigger.TriggerInstance.usedPower(MineraculousMiraculous.CAT, MiraculousUsePowerTrigger.Context.LIVING_ENTITY)), "What Have You Done?!", "Cataclysm a living entity");
//
//        AdvancementHolder cataclysmKillEntity = create(cataclysmEntity, MineraculousItems.CATACLYSM_DUST, "cataclysm_kill_living_entity", AdvancementType.TASK, true, true, true, AdvancementRewards.Builder.experience(20).build(), AdvancementRequirements.Strategy.AND, Map.of(
//                "cataclysm_kill_entity", KilledTrigger.TriggerInstance.playerKilledEntity(Optional.empty(), DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(MineraculousDamageTypeTags.IS_CATACLYSM)))), "Dead and Gone and Buried", "Cataclysm an entity that dies before it is healed");
//
//        AdvancementHolder obtainCamembert = create(transformCat, MineraculousItems.CAMEMBERT_WEDGES.get(CheeseBlock.Age.FRESH), "obtain_camembert", AdvancementType.TASK, true, true, false, null, AdvancementRequirements.Strategy.OR, Map.of(
//                "obtain_camembert", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(MineraculousItemTags.CAMEMBERT))), "Smelly Cheese, Smelly Cheese", "What are they feeding you to?");
//
//        AdvancementHolder transformButterfly = create(root, Miraculous.createMiraculousStack(MineraculousMiraculous.BUTTERFLY), "transform_butterfly", AdvancementType.TASK, true, true, false, AdvancementRewards.Builder.experience(10).build(), AdvancementRequirements.Strategy.AND, Map.of(
//                "transform_butterfly", MiraculousTransformTrigger.TriggerInstance.transformed(MineraculousMiraculous.BUTTERFLY)), "Wings Rise!", "Transform using the Butterfly miraculous");
//
//        AdvancementHolder powerKamiko = create(transformButterfly, MineraculousArmors.KAMIKOTIZATION.HEAD.toStack(), "power_kamiko", AdvancementType.TASK, true, true, false, null, AdvancementRequirements.Strategy.AND, Map.of(
//                "power_kamiko", MiraculousUsePowerTrigger.TriggerInstance.usedPower(MineraculousMiraculous.BUTTERFLY, MiraculousUsePowerTrigger.Context.LIVING_ENTITY)), "Fly Away My Little Kamiko", "Power up a Kamiko");
//
//        AdvancementHolder kamikotizePlayer = create(powerKamiko, MineraculousArmors.KAMIKOTIZATION.HEAD.toStack(), "kamikotize_player", AdvancementType.TASK, true, true, false, null, AdvancementRequirements.Strategy.AND, Map.of(
//                "kamikotize_player", KamikotizePlayerTrigger.TriggerInstance.kamikotizedPlayer()), "Sharing the Wealth", "Provide power to another player with a kamikotization");
//
//        AdvancementHolder kamikotizeSelf = create(kamikotizePlayer, Miraculous.createMiraculousStack(MineraculousMiraculous.BUTTERFLY), "kamikotize_self", AdvancementType.TASK, true, true, false, AdvancementRewards.Builder.experience(5).build(), AdvancementRequirements.Strategy.AND, Map.of(
//                "kamikotize_self", KamikotizationTransformTrigger.TriggerInstance.transformed(true)), "Deception", "Use the butterfly miraculous to kamikotize yourself");
//
//        AdvancementHolder transformKamikotization = create(root, MineraculousArmors.KAMIKOTIZATION.HEAD.toStack(), "transform_kamikotization", AdvancementType.TASK, true, true, false, AdvancementRewards.Builder.experience(5).build(), AdvancementRequirements.Strategy.AND, Map.of(
//                "transform_kamikotization", KamikotizationTransformTrigger.TriggerInstance.transformed(false)), "Powered Up", "Accept a kamikotization from the Butterfly miraculous holder");
    }
}
