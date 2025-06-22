package dev.thomasglasser.mineraculous.impl.data.advancements.packs;

import dev.thomasglasser.mineraculous.api.advancements.critereon.KamikotizeEntityTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.PerformMiraculousActiveAbilityTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.ReleasePurifiedEntitiesTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.TransformKamikotizationTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.TransformMiraculousTrigger;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.tags.MineraculousDamageTypeTags;
import dev.thomasglasser.mineraculous.api.tags.MineraculousEntityTypeTags;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.ability.context.BlockAbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.context.EntityAbilityContext;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.level.block.AgeingCheese;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.tommylib.api.data.advancements.ExtendedAdvancementGenerator;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;

public class MiraculousAdvancements extends ExtendedAdvancementGenerator {
    public MiraculousAdvancements(BiConsumer<String, String> lang) {
        super(Mineraculous.MOD_ID, "miraculous", lang);
    }

    @Override
    public void generate(HolderLookup.Provider provider) {
        HolderGetter<Miraculous> miraculouses = provider.lookupOrThrow(MineraculousRegistries.MIRACULOUS);
        Holder<Miraculous> ladybug = miraculouses.getOrThrow(Miraculouses.LADYBUG);
        Holder<Miraculous> cat = miraculouses.getOrThrow(Miraculouses.CAT);
        Holder<Miraculous> butterfly = miraculouses.getOrThrow(Miraculouses.BUTTERFLY);

        AdvancementHolder root = builder("root", Miraculous.createMiraculousStack(ladybug) /*TODO: Replace with Miraculous box*/, "Miraculous", "A hero's journey begins...")
                .background(Mineraculous.modLoc("textures/gui/advancements/backgrounds/miraculous.png"))
                .toast(false)
                .announce(false)
                .trigger("get_miraculous", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(MineraculousItems.MIRACULOUS)))
                .build();

        AdvancementHolder transformLadybug = builder("transform_ladybug", Miraculous.createMiraculousStack(ladybug), "Spots On!", "Transform using the Ladybug miraculous")
                .parent(root)
                .experience(10)
                .trigger("transform_ladybug", TransformMiraculousTrigger.TriggerInstance.transformed(ladybug.getKey()))
                .build();

        AdvancementHolder releasePurifiedButterfly = builder("release_purified_butterfly", MineraculousItems.LADYBUG_YOYO.toStack(), "Bye Bye, Little Butterfly", "Purify and release a butterfly")
                .parent(transformLadybug)
                .trigger("release_purified_butterfly", ReleasePurifiedEntitiesTrigger.TriggerInstance.releasedPurifiedEntities(EntityPredicate.Builder.entity().of(MineraculousEntityTypeTags.BUTTERFLIES)))
                .build();

        AdvancementHolder transformCat = builder("transform_cat", Miraculous.createMiraculousStack(cat), "Claws Out!", "Transform using the Cat miraculous")
                .parent(root)
                .experience(10)
                .trigger("transform_cat", TransformMiraculousTrigger.TriggerInstance.transformed(cat.getKey()))
                .build();

        AdvancementHolder cataclysmBlock = builder("cataclysm_block", MineraculousBlocks.CATACLYSM_BLOCK.toStack(), "My Castle Crumbled Overnight", "Cataclysm a block and watch it spread")
                .parent(transformCat)
                .trigger("cataclysm_block", PerformMiraculousActiveAbilityTrigger.TriggerInstance.performedActiveAbility(cat.getKey(), BlockAbilityContext.ADVANCEMENT_CONTEXT))
                .build();

        AdvancementHolder cataclysmLivingEntity = builder("cataclysm_living_entity", MineraculousItems.CATACLYSM_DUST.toStack(), "What Have I Done?!", "Cataclysm a living entity")
                .parent(transformCat)
                .trigger("cataclysm_entity", PerformMiraculousActiveAbilityTrigger.TriggerInstance.performedActiveAbility(cat.getKey(), EntityAbilityContext.ADVANCEMENT_CONTEXT_LIVING))
                .build();

        AdvancementHolder cataclysmKillLivingEntity = builder("cataclysm_kill_living_entity", MineraculousItems.CATACLYSM_DUST.toStack(), "Dead and Gone and Buried", "Cataclysm an entity that dies before it is cured")
                .parent(cataclysmLivingEntity)
                .hidden(true)
                .experience(20)
                .trigger("cataclysm_kill_living_entity", KilledTrigger.TriggerInstance.playerKilledEntity(Optional.empty(), DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(MineraculousDamageTypeTags.IS_CATACLYSM))))
                .build();

        AdvancementHolder obtainCamembert = builder("obtain_camembert", MineraculousBlocks.CAMEMBERT.get(AgeingCheese.Age.FRESH).toStack(), "Smelly Cheese, Smelly Cheese", "What are they feeding you to?")
                .parent(transformCat)
                .trigger("obtain_camembert", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(MineraculousItemTags.CAMEMBERT)))
                .build();

        AdvancementHolder transformButterfly = builder("transform_butterfly", Miraculous.createMiraculousStack(butterfly), "Wings Rise!", "Transform using the Butterfly miraculous")
                .parent(root)
                .experience(10)
                .trigger("transform_butterfly", TransformMiraculousTrigger.TriggerInstance.transformed(butterfly.getKey()))
                .build();

        AdvancementHolder kamikotizeButterfly = builder("kamikotize_butterfly", MineraculousArmors.KAMIKOTIZATION.head().toStack(), "Fly Away My Little Kamiko", "Kamikotize a butterfly")
                .parent(transformButterfly)
                .experience(10)
                .trigger("kamikotize_butterfly", PerformMiraculousActiveAbilityTrigger.TriggerInstance.performedActiveAbility(butterfly.getKey(), EntityAbilityContext.ADVANCEMENT_CONTEXT_LIVING))
                .build();

        AdvancementHolder kamikotizeEntity = builder("kamikotize_entity", MineraculousArmors.KAMIKOTIZATION.head().toStack(), "Sharing the Wealth", "Provide power to another being via kamikotization")
                .parent(kamikotizeButterfly)
                .trigger("kamikotize_entity", KamikotizeEntityTrigger.TriggerInstance.kamikotizedEntity(false))
                .build();

        AdvancementHolder kamikotizeSelf = builder("kamikotize_self", Miraculous.createMiraculousStack(butterfly), "Deception", "Use the butterfly miraculous to kamikotize yourself")
                .parent(kamikotizeEntity)
                .experience(5)
                .trigger("kamikotize_self", TransformKamikotizationTrigger.TriggerInstance.transformed(true))
                .build();

        AdvancementHolder transformKamikotization = builder("transform_kamikotization", MineraculousArmors.KAMIKOTIZATION.head().toStack(), "Powered Up", "Accept a kamikotization from the Butterfly miraculous holder")
                .parent(root)
                .experience(5)
                .trigger("transform_kamikotization", TransformKamikotizationTrigger.TriggerInstance.transformed(false))
                .build();
    }
}
