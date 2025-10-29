package dev.thomasglasser.mineraculous.impl.data.models;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.renderer.item.MineraculousItemProperties;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemDisplayContexts;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import dev.thomasglasser.tommylib.api.data.models.ExtendedItemModelProvider;
import dev.thomasglasser.tommylib.api.registration.DeferredBlock;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MineraculousItemModelProvider extends ExtendedItemModelProvider {
    public MineraculousItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MineraculousConstants.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        miraculous(Miraculouses.LADYBUG)
                .transform(MineraculousItemDisplayContexts.CURIOS_LEFT_EARRING.getValue()).rotation(90, 0, 90).translation(-4, -2.65F, -0.5F).scale(0.2F).end()
                .transform(MineraculousItemDisplayContexts.CURIOS_RIGHT_EARRING.getValue()).rotation(90, 0, 90).translation(-4, -2.65F, -0.5F).scale(0.2F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).translation(0, -0.5F, 1).scale(0.2F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).translation(0, -0.5F, 1).scale(0.2F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).translation(4, -0.25F, 0).scale(0.2F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).translation(-4, -0.25F, 0).scale(0.2F).end()
                .transform(ItemDisplayContext.HEAD).translation(0, 5.75F, 0).scale(0.2F).end()
                .transform(ItemDisplayContext.GROUND).translation(0, -4, 0).scale(0.2F).end()
                .transform(ItemDisplayContext.FIXED).rotation(-90, 0, 0).translation(0, 0, 1.5F).scale(0.4F).end()
                .transform(ItemDisplayContext.GUI).rotation(90, 180, 0).translation(0, 0, 0).scale(2).end()
                .end();
        miraculous(Miraculouses.CAT)
                .transform(MineraculousItemDisplayContexts.CURIOS_RIGHT_ARM.getValue()).rotation(90, 0, 270).translation(-0.85F, 9.6F, 0.5F).scale(0.1F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(90, 0, 0).translation(0, 0.3F, 0).scale(0.1F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(90, 0, 0).translation(0, 0.3F, 0).scale(0.1F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(90, 0, 0).translation(4, 1, 0).scale(0.1F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(90, 0, 0).translation(-4, 1, 0).scale(0.1F).end()
                .transform(ItemDisplayContext.HEAD).translation(0, -2, -6.25F).scale(0.1F).end()
                .transform(ItemDisplayContext.GROUND).translation(0, -4, 0).scale(0.1F).end()
                .transform(ItemDisplayContext.FIXED).translation(0, -0.75F, 0).scale(0.2F).end()
                .transform(ItemDisplayContext.GUI).rotation(0, 180, 0).translation(0, -5.25F, 0).scale(1.5F).end()
                .end();
        miraculous(Miraculouses.BUTTERFLY)
                .transform(MineraculousItemDisplayContexts.CURIOS_BODY.getValue()).rotation(0, 0, 180).translation(0, 4, -2.2F).scale(0.2F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(90, 0, 0).translation(0, 0.25F, -1.75F).scale(0.2F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(90, 0, 0).translation(0, 0.25F, -1.75F).scale(0.2F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).translation(4.5F, -1, 0).scale(0.2F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).translation(-4.5F, -1, 0).scale(0.2F).end()
                .transform(ItemDisplayContext.HEAD).rotation(0, 0, 45).translation(6.5F, 3, -6.5F).scale(0.2F).end()
                .transform(ItemDisplayContext.GROUND).translation(0, -5, 0).scale(0.2F).end()
                .transform(ItemDisplayContext.FIXED).translation(0, -4.75F, 0).scale(0.4F).end()
                .transform(ItemDisplayContext.GUI).rotation(0, 180, 0).translation(0, -11, 0).end()
                .end();

        MineraculousArmors.MIRACULOUS.getAll().forEach(item -> singleTexture(item.getId().getPath(), mcItemLoc("generated"), "layer0", modItemLoc("miraculous/armor")));
        MineraculousArmors.KAMIKOTIZATION.getAll().forEach(item -> singleTexture(item.getId().getPath(), mcItemLoc("generated"), "layer0", modItemLoc("kamikotization_armor")));

        withEntityModel(MineraculousItems.MIRACULOUS).guiLight(BlockModel.GuiLight.FRONT);
        withEntityModel(MineraculousItems.KWAMI).guiLight(BlockModel.GuiLight.FRONT);

        basicItem(MineraculousItems.CATACLYSM_DUST);
        basicItem(MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE);
        basicItem(MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE);
        basicItem(MineraculousItems.BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE);
        basicItem(MineraculousItems.RAW_MACARON);
        basicItem(MineraculousItems.MACARON);
        basicItem(MineraculousBlocks.HIBISCUS_BUSH.asItem());

        basicBlockItem(MineraculousBlocks.CATACLYSM_BLOCK);
        basicBlockItem(MineraculousBlocks.OVEN);

        MineraculousBlocks.CHEESE.forEach((age, block) -> withBitesOverrides(block, basicBlockItem(block)));
        MineraculousBlocks.WAXED_CHEESE.forEach((age, block) -> withBitesOverrides(MineraculousBlocks.CHEESE.get(age), withExistingParent(block.getId().getPath(), MineraculousBlocks.CHEESE.get(age).getId().withPrefix("block/"))));
        MineraculousBlocks.CAMEMBERT.values().forEach(block -> withBitesOverrides(block, basicBlockItem(block)));
        MineraculousBlocks.WAXED_CAMEMBERT.forEach((age, block) -> withBitesOverrides(MineraculousBlocks.CAMEMBERT.get(age), withExistingParent(block.getId().getPath(), MineraculousBlocks.CAMEMBERT.get(age).getId().withPrefix("block/"))));

        MineraculousItems.CHEESE.values().forEach(this::basicItem);
        MineraculousItems.WAXED_CHEESE.forEach((age, item) -> withExistingParent(item.getId().getPath(), MineraculousItems.CHEESE.get(age).getId()));
        MineraculousItems.CAMEMBERT.values().forEach(this::basicItem);
        MineraculousItems.WAXED_CAMEMBERT.forEach((age, item) -> withExistingParent(item.getId().getPath(), MineraculousItems.CAMEMBERT.get(age).getId()));

        basicItem(MineraculousBlocks.CHEESE_POT.getId());

        ItemModelBuilder inHandLadybugYoyo = withEntityModel(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_in_hand"))
                .transforms()
                .transform(MineraculousItemDisplayContexts.CURIOS_BODY.getValue()).rotation(-90, 0, 0).translation(-2, 10, 3.7f).scale(0.8f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).translation(0, 0, 2).scale(0.6f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).translation(0, 0, 2).scale(0.6f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).translation(2, 0, 0).scale(0.6f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).translation(2, 0, 0).scale(0.6f).end()
                .transform(ItemDisplayContext.HEAD).translation(0, 6.25f, 0).end()
                .end();
        ItemModelBuilder inventoryLadybugYoyo = basicInventoryItem(MineraculousItems.LADYBUG_YOYO);
        ItemModelBuilder activeLadybugYoyo = withSeparateInventoryModel(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_active"), inHandLadybugYoyo, basicItem(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_active")));
        ItemModelBuilder inHandBlockingLadybugYoyo = withEntityModel(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_blocking_in_hand"))
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).translation(0, -2, 1).scale(0.6f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).translation(0, -2, 1).scale(0.6f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).translation(2, -1, 0).scale(0.6f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).translation(2, -1, 0).scale(0.6f).end()
                .end();
        ItemModelBuilder inHandSpyglassLadybugYoyo = withEntityModel(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_spyglass_in_hand"))
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).translation(0, 0, 2).scale(0.6f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).translation(0, 0, 2).scale(0.6f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).translation(2, 0, 0).scale(0.6f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).translation(2, 0, 0).scale(0.6f).end()
                .transform(ItemDisplayContext.HEAD).rotation(90, 0, 0).translation(0, -0.5f, -12.25f).end()
                .end();
        ItemModelBuilder inHandThrownLadybugYoyo = withEntityModel(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_thrown_in_hand"));
        ItemModelBuilder inventoryThrownLadybugYoyo = basicItem(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_thrown_inventory"), MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_thrown").getPath());
        ItemModelBuilder inventoryLandedLadybugYoyo = basicItem(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_landed_inventory"), MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_landed").getPath());
        generatedModels.remove(inHandThrownLadybugYoyo.getLocation());
        generatedModels.remove(inventoryThrownLadybugYoyo.getLocation());
        generatedModels.remove(inventoryLandedLadybugYoyo.getLocation());
        ItemModelBuilder thrownLadybugYoyo = withSeparateTransforms(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_thrown"))
                .base(inHandThrownLadybugYoyo)
                .perspective(ItemDisplayContext.GUI, inventoryThrownLadybugYoyo)
                .perspective(ItemDisplayContext.FIXED, inventoryThrownLadybugYoyo)
                .perspective(ItemDisplayContext.GROUND, inventoryThrownLadybugYoyo)
                .end();
        ItemModelBuilder landedLadybugYoyo = withSeparateTransforms(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_landed"))
                .base(inHandThrownLadybugYoyo)
                .perspective(ItemDisplayContext.GUI, inventoryLandedLadybugYoyo)
                .perspective(ItemDisplayContext.FIXED, inventoryLandedLadybugYoyo)
                .perspective(ItemDisplayContext.GROUND, inventoryLandedLadybugYoyo)
                .end();
        ItemModelBuilder blockingLadybugYoyo = withSeparateInventoryModel(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_blocking"), inHandBlockingLadybugYoyo, inventoryLandedLadybugYoyo);
        withSeparateInventoryModel(MineraculousItems.LADYBUG_YOYO, inHandLadybugYoyo, inventoryLadybugYoyo)
                .override()
                .predicate(MineraculousItemProperties.ACTIVE, 1)
                .model(activeLadybugYoyo)
                .end()
                .override()
                .predicate(MineraculousItemProperties.ACTIVE, 1)
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(LadybugYoyoItem.Mode.PHONE))
                .model(withSeparateInventoryModel(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_phone"), inHandLadybugYoyo, basicItem(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_phone"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.ACTIVE, 1)
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(LadybugYoyoItem.Mode.PURIFY))
                .model(withSeparateInventoryModel(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_purify"), inHandLadybugYoyo, basicItem(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_purify"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.ACTIVE, 1)
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(LadybugYoyoItem.Mode.SPYGLASS))
                .model(withSeparateInventoryModel(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_spyglass"), inHandSpyglassLadybugYoyo, basicItem(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_spyglass"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.ACTIVE, 1)
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(LadybugYoyoItem.Mode.SPYGLASS) + 1)
                .model(activeLadybugYoyo)
                .end()
                .override()
                .predicate(MineraculousItemProperties.THROWN, 1)
                .model(thrownLadybugYoyo)
                .end()
                .override()
                .predicate(MineraculousItemProperties.THROWN, 2)
                .model(landedLadybugYoyo)
                .end()
                .override()
                .predicate(MineraculousItemProperties.BLOCKING, 1)
                .model(blockingLadybugYoyo)
                .end()
                .override()
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(LadybugYoyoItem.Mode.PURIFY))
                .predicate(MineraculousItemProperties.BLOCKING, 1)
                .model(withSeparateInventoryModel(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_blocking_purify"), inHandBlockingLadybugYoyo, basicItem(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_blocking_purify"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(LadybugYoyoItem.Mode.PURIFY) + 1)
                .predicate(MineraculousItemProperties.BLOCKING, 1)
                .model(blockingLadybugYoyo)
                .end();

        ItemModelBuilder inHandCatStaff = withEntityModel(MineraculousItems.CAT_STAFF.getId().withSuffix("_in_hand"))
                .transforms()
                .transform(MineraculousItemDisplayContexts.CURIOS_BODY.getValue()).rotation(0, 180, 90).translation(-4.2f, 8, 2.7f).scale(0.7f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(0, -180, 0).translation(0, -3.5f, 1.25f).scale(0.7f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(0, -180, 0).translation(0, -3.5f, 1.25f).scale(0.7f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 165, 0).translation(0, 1.5f, 1.25f).scale(0.7f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 165, 0).translation(0, 1.5f, 1.25f).scale(0.7f).end()
                .transform(ItemDisplayContext.HEAD).rotation(-54, 0, 0).translation(0, 1.75f, -2.75f).end()
                .end();
        ItemModelBuilder inventoryCatStaff = basicInventoryItem(MineraculousItems.CAT_STAFF);
        ItemModelBuilder activeCatStaff = withSeparateInventoryModel(MineraculousItems.CAT_STAFF.getId().withSuffix("_active"), inHandCatStaff, basicItem(MineraculousItems.CAT_STAFF.getId().withSuffix("_active")));
        ItemModelBuilder inHandSpyglassCatStaff = withEntityModel(MineraculousItems.CAT_STAFF.getId().withSuffix("_spyglass_in_hand"))
                .transforms()
                .transform(MineraculousItemDisplayContexts.CURIOS_BODY.getValue()).rotation(0, 180, 90).translation(-4.2f, 8, 2.7f).scale(0.7f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(0, -180, 0).translation(0, -3.5f, 1.25f).scale(0.7f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(0, -180, 0).translation(0, -3.5f, 1.25f).scale(0.7f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 165, 0).translation(0, 1.5f, 1.25f).scale(0.7f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 165, 0).translation(0, 1.5f, 1.25f).scale(0.7f).end()
                .transform(ItemDisplayContext.HEAD).rotation(0, 180, 0).translation(0, -15, -8).scale(1.6f).end()
                .end();
        withSeparateInventoryModel(MineraculousItems.CAT_STAFF, inHandCatStaff, inventoryCatStaff)
                .override()
                .predicate(MineraculousItemProperties.ACTIVE, 1)
                .model(activeCatStaff)
                .end()
                .override()
                .predicate(MineraculousItemProperties.ACTIVE, 1)
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(CatStaffItem.Mode.PHONE))
                .model(withSeparateInventoryModel(MineraculousItems.CAT_STAFF.getId().withSuffix("_phone"), inHandCatStaff, basicItem(MineraculousItems.CAT_STAFF.getId().withSuffix("_phone"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.ACTIVE, 1)
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(CatStaffItem.Mode.SPYGLASS))
                .model(withSeparateInventoryModel(MineraculousItems.CAT_STAFF.getId().withSuffix("_spyglass"), inHandSpyglassCatStaff, basicItem(MineraculousItems.CAT_STAFF.getId().withSuffix("_spyglass"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.ACTIVE, 1)
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(CatStaffItem.Mode.SPYGLASS) + 1)
                .model(activeCatStaff)
                .end();

        ItemModelBuilder inHandButterflyCane = withEntityModel(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_in_hand"))
                .transforms()
                .transform(MineraculousItemDisplayContexts.CURIOS_BODY.getValue()).rotation(180, 0, 45).translation(8.67f, 12.5f, 2.3f).scale(0.8f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).translation(0, -18, 2).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).translation(0, -18, 2).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).translation(0, -24, 0).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).translation(0, -24, 0).end()
                .transform(ItemDisplayContext.HEAD).translation(0, -21, 0).end()
                .end();
        ItemModelBuilder inventoryButterflyCane = basicInventoryItem(MineraculousItems.BUTTERFLY_CANE);
        ItemModelBuilder inHandButterflyCaneBlade = withEntityModel(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_blade_in_hand"))
                .transforms()
                .transform(MineraculousItemDisplayContexts.CURIOS_BODY.getValue()).rotation(180, 0, 45).translation(8.67f, 12.5f, 2.3f).scale(0.8f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).translation(0, -2, 2).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).translation(0, -2, 2).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).translation(0, -8, 0).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).translation(0, -8, 0).end()
                .transform(ItemDisplayContext.HEAD).translation(0, 5, 0).end()
                .end();
        ItemModelBuilder inHandSpyglassButterflyCane = withEntityModel(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_spyglass_in_hand"))
                .transforms()
                .transform(MineraculousItemDisplayContexts.CURIOS_BODY.getValue()).rotation(180, 0, 45).translation(8.67f, 12.5f, 2.3f).scale(0.8f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).translation(0, -18, 2).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).translation(0, -18, 2).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).translation(0, -24, 0).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).translation(0, -24, 0).end()
                .transform(ItemDisplayContext.HEAD).translation(0, -47, -10.75f).scale(1.6f).end()
                .end();
        withSeparateInventoryModel(MineraculousItems.BUTTERFLY_CANE, inHandButterflyCane, inventoryButterflyCane)
                .override()
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(ButterflyCaneItem.Mode.BLADE))
                .model(withSeparateInventoryModel(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_blade"), inHandButterflyCaneBlade, basicInventoryItem(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_blade"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(ButterflyCaneItem.Mode.BLADE) + 1)
                .model(withSeparateInventoryModel(MineraculousItems.BUTTERFLY_CANE, inHandButterflyCane, inventoryButterflyCane))
                .end()
                .override()
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(ButterflyCaneItem.Mode.KAMIKO_STORE))
                .predicate(MineraculousItemProperties.STORING, 0)
                .model(withSeparateInventoryModel(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_kamiko_store"), inHandButterflyCane, basicInventoryItem(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_kamiko_store"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(ButterflyCaneItem.Mode.KAMIKO_STORE))
                .predicate(MineraculousItemProperties.STORING, 1)
                .model(withSeparateInventoryModel(MineraculousItems.BUTTERFLY_CANE, inHandButterflyCane, inventoryButterflyCane))
                .end()
                .override()
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(ButterflyCaneItem.Mode.PHONE))
                .model(withSeparateInventoryModel(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_phone"), inHandButterflyCane, basicInventoryItem(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_phone"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(ButterflyCaneItem.Mode.SPYGLASS))
                .model(withSeparateInventoryModel(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_spyglass"), inHandSpyglassButterflyCane, basicInventoryItem(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_spyglass"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.MODE, MineraculousItemProperties.getPropertyForAbility(ButterflyCaneItem.Mode.SPYGLASS) + 1)
                .model(withSeparateInventoryModel(MineraculousItems.BUTTERFLY_CANE, inHandButterflyCane, inventoryButterflyCane))
                .end();
    }

    private ModelBuilder.TransformsBuilder miraculous(ResourceKey<Miraculous> name) {
        return getBuilder("item/miraculous/" + name.location().getPath()).transforms();
    }

    private ItemModelBuilder withBitesOverrides(DeferredBlock<?> block, ItemModelBuilder builder) {
        return builder
                .override()
                .predicate(MineraculousItemProperties.MISSING_PIECES, 1)
                .model(withExistingParent(block.getId().getPath() + "_slice1", block.getId().withPrefix("block/").withSuffix("_slice1")))
                .end()
                .override()
                .predicate(MineraculousItemProperties.MISSING_PIECES, 2)
                .model(withExistingParent(block.getId().getPath() + "_slice2", block.getId().withPrefix("block/").withSuffix("_slice2")))
                .end()
                .override()
                .predicate(MineraculousItemProperties.MISSING_PIECES, 3)
                .model(withExistingParent(block.getId().getPath() + "_slice3", block.getId().withPrefix("block/").withSuffix("_slice3")))
                .end();
    }
}
