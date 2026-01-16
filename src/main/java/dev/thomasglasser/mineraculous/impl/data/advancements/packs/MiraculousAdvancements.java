package dev.thomasglasser.mineraculous.impl.data.advancements.packs;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.advancements.critereon.ItemLuckyCharmPredicate;
import dev.thomasglasser.mineraculous.api.advancements.critereon.KamikotizedEntityTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.MineraculousItemSubPredicates;
import dev.thomasglasser.mineraculous.api.advancements.critereon.PerformedMiraculousActiveAbilityTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.TransformedKamikotizationTrigger;
import dev.thomasglasser.mineraculous.api.advancements.critereon.TransformedMiraculousTrigger;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.tags.MineraculousDamageTypeTags;
import dev.thomasglasser.mineraculous.api.world.ability.context.BlockAbilityContext;
import dev.thomasglasser.mineraculous.api.world.ability.context.EntityAbilityContext;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.world.item.component.LuckyCharm;
import dev.thomasglasser.tommylib.api.data.advancements.ExtendedAdvancementGenerator;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.Util;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MiraculousAdvancements extends ExtendedAdvancementGenerator {
    public MiraculousAdvancements(BiConsumer<String, String> lang) {
        super(MineraculousConstants.MOD_ID, "miraculous", lang);
    }

    @Override
    public void generate(HolderLookup.Provider provider) {
        HolderGetter<Miraculous> miraculouses = provider.lookupOrThrow(MineraculousRegistries.MIRACULOUS);
        Holder<Miraculous> ladybug = miraculouses.getOrThrow(Miraculouses.LADYBUG);
        Holder<Miraculous> cat = miraculouses.getOrThrow(Miraculouses.CAT);
        Holder<Miraculous> butterfly = miraculouses.getOrThrow(Miraculouses.BUTTERFLY);

        AdvancementHolder root = builder("root", Miraculous.createMiraculousStack(ladybug) /*TODO: Replace with Miraculous box*/, "Miraculous", "Just a normal player, with a normal life")
                .background(MineraculousConstants.modLoc("textures/gui/advancements/backgrounds/miraculous.png"))
                .toast(false)
                .announce(false)
                .trigger("get_miraculous", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(MineraculousItems.MIRACULOUS)))
                .build();

        AdvancementHolder transformLadybug = builder("transform_ladybug", Miraculous.createMiraculousStack(ladybug), "Spots On!", "Transform using the Ladybug miraculous")
                .parent(root)
                .experience(10)
                .trigger("transform_ladybug", TransformedMiraculousTrigger.TriggerInstance.transformed(ladybug.getKey()))
                .build();

        ItemStack luckyCharmStack = Items.PRIZE_POTTERY_SHERD.getDefaultInstance();
        LuckyCharm luckyCharm = new LuckyCharm(Optional.empty(), Util.NIL_UUID, 0);
        luckyCharmStack.set(MineraculousDataComponents.LUCKY_CHARM, luckyCharm);
        AdvancementHolder obtainLuckyCharm = builder("obtain_lucky_charm", luckyCharmStack, "The Missing Piece", "Obtain a Lucky Charm")
                .parent(transformLadybug)
                .trigger("obtain_lucky_charm", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().withSubPredicate(MineraculousItemSubPredicates.LUCKY_CHARM.get(), new ItemLuckyCharmPredicate(Optional.of(false)))))
                .build();

        luckyCharmStack = Items.CHERRY_SIGN.getDefaultInstance();
        luckyCharmStack.set(MineraculousDataComponents.LUCKY_CHARM, luckyCharm);
        AdvancementHolder obtainSelfLuckyCharm = builder("obtain_self_lucky_charm", luckyCharmStack, "Am I the Drama?", "It's me, hi, I'm the problem, it's me")
                .parent(obtainLuckyCharm)
                .hidden(true)
                .experience(20)
                .trigger("obtain_self_lucky_charm", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().withSubPredicate(MineraculousItemSubPredicates.LUCKY_CHARM.get(), new ItemLuckyCharmPredicate(Optional.of(true)))))
                .build();

        AdvancementHolder transformCat = builder("transform_cat", Miraculous.createMiraculousStack(cat), "Claws Out!", "Transform using the Cat miraculous")
                .parent(root)
                .experience(10)
                .trigger("transform_cat", TransformedMiraculousTrigger.TriggerInstance.transformed(cat.getKey()))
                .build();

        AdvancementHolder cataclysmBlock = builder("cataclysm_block", MineraculousBlocks.CATACLYSM_BLOCK.toStack(), "My Castle Crumbled Overnight", "Cataclysm a block and watch it spread")
                .parent(transformCat)
                .trigger("cataclysm_block", PerformedMiraculousActiveAbilityTrigger.TriggerInstance.performedActiveAbility(cat.getKey(), BlockAbilityContext.ADVANCEMENT_CONTEXT))
                .build();

        AdvancementHolder cataclysmLivingEntity = builder("cataclysm_living_entity", MineraculousItems.CATACLYSM_DUST.toStack(), "What Have I Done?!", "Cataclysm a living entity")
                .parent(transformCat)
                .trigger("cataclysm_entity", PerformedMiraculousActiveAbilityTrigger.TriggerInstance.performedActiveAbility(cat.getKey(), EntityAbilityContext.ADVANCEMENT_CONTEXT_LIVING))
                .build();

        AdvancementHolder cataclysmKillLivingEntity = builder("cataclysm_kill_living_entity", MineraculousItems.CATACLYSM_DUST.toStack(), "Dead and Gone and Buried", "Cataclysm an entity that dies before it is cured")
                .parent(cataclysmLivingEntity)
                .trigger("cataclysm_kill_living_entity", KilledTrigger.TriggerInstance.playerKilledEntity(Optional.empty(), DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(MineraculousDamageTypeTags.IS_CATACLYSM))))
                .build();

        AdvancementHolder transformButterfly = builder("transform_butterfly", Miraculous.createMiraculousStack(butterfly), "Wings Rise!", "Transform using the Butterfly miraculous")
                .parent(root)
                .experience(10)
                .trigger("transform_butterfly", TransformedMiraculousTrigger.TriggerInstance.transformed(butterfly.getKey()))
                .build();

        AdvancementHolder kamikotizeButterfly = builder("kamikotize_butterfly", MineraculousArmors.KAMIKOTIZATION.head().toStack(), "Fly Away My Little Kamiko", "Kamikotize a butterfly")
                .parent(transformButterfly)
                .trigger("kamikotize_butterfly", PerformedMiraculousActiveAbilityTrigger.TriggerInstance.performedActiveAbility(butterfly.getKey(), EntityAbilityContext.ADVANCEMENT_CONTEXT_LIVING))
                .build();

        AdvancementHolder kamikotizeEntity = builder("kamikotize_entity", MineraculousArmors.KAMIKOTIZATION.head().toStack(), "Sharing the Wealth", "Provide power to another being via kamikotization")
                .parent(kamikotizeButterfly)
                .trigger("kamikotize_entity", KamikotizedEntityTrigger.TriggerInstance.kamikotizedEntity(false))
                .build();

        AdvancementHolder kamikotizeSelf = builder("kamikotize_self", Miraculous.createMiraculousStack(butterfly), "Deception", "Use the butterfly miraculous to kamikotize yourself")
                .parent(kamikotizeEntity)
                .hidden(true)
                .experience(20)
                .trigger("kamikotize_self", TransformedKamikotizationTrigger.TriggerInstance.transformed(true))
                .build();

        AdvancementHolder transformKamikotization = builder("transform_kamikotization", MineraculousArmors.KAMIKOTIZATION.head().toStack(), "Powered Up", "Accept a kamikotization from the Butterfly miraculous holder")
                .parent(root)
                .experience(10)
                .trigger("transform_kamikotization", TransformedKamikotizationTrigger.TriggerInstance.transformed(false))
                .build();
    }
}
