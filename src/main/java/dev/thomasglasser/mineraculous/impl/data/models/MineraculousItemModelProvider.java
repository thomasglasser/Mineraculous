package dev.thomasglasser.mineraculous.impl.data.models;

import dev.thomasglasser.mineraculous.api.client.renderer.item.MineraculousItemProperties;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemDisplayContexts;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.item.ButterflyCaneItem;
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
        super(output, Mineraculous.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        miraculous(Miraculouses.LADYBUG)
                .transform(MineraculousItemDisplayContexts.CURIOS_EARRINGS.getValue()).rotation(90, 0, 90).translation(-4, -2.65F, -0.5F).scale(0.3f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).translation(0.5F, 0, 1.75F).scale(0.5F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).translation(-0.5F, 0, 1.75F).scale(0.5F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).translation(0.5F, 1, 1.75F).scale(0.5F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).translation(0.5F, 1, 1.75F).scale(0.5F).end()
                .transform(ItemDisplayContext.HEAD).rotation(-90, 0, 0).translation(0.5F, -1, -6.5F).scale(0.5F).end()
                .transform(ItemDisplayContext.GROUND).translation(1.25F, -1.5F, 1).scale(0.86F).end()
                .transform(ItemDisplayContext.FIXED).rotation(-90, 0, 0).translation(1, 1, 0).end()
                .transform(ItemDisplayContext.GUI).rotation(90, 180, 0).translation(-2.5F, 2.5F, 0).scale(2.5F).end()
                .end();
        miraculous(Miraculouses.CAT)
                .transform(MineraculousItemDisplayContexts.CURIOS_RIGHT_ARM.getValue()).rotation(90, 0, 270).translation(-1.45F, 9.4F, 0.5F).scale(0.3F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).translation(0, 0, 0.5F).scale(0.3F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).translation(0, 0, 0.5F).scale(0.3F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).translation(4F, 0, 0).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).translation(4F, 0, 0).end()
                .transform(ItemDisplayContext.GROUND).scale(0.5F).end()
                .transform(ItemDisplayContext.GUI).rotation(0, 180, 0).translation(-0, -4.5F, 0).scale(3F).end()
                .transform(ItemDisplayContext.HEAD).translation(0, -2F, -6F).scale(0.5F).end()
                .transform(ItemDisplayContext.FIXED).translation(0, -2.5F, 3F).scale(2F).end()
                .end();
        miraculous(Miraculouses.BUTTERFLY)
                .transform(MineraculousItemDisplayContexts.CURIOS_BODY.getValue()).translation(0.1F, 4, -2.2F).rotation(0, 0, 180).scale(0.2F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(90, 0, 0).translation(0, 0.5F, -5).scale(0.5F, 0.5F, 0.5F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(90, 0, 0).translation(0, 0.5F, -5).scale(0.5F, 0.5F, 0.5F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).translation(5, -5, 0).scale(0.5F, 0.5F, 0.5F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).translation(5, -5, 0).scale(0.5F, 0.5F, 0.5F).end()
                .transform(ItemDisplayContext.GROUND).translation(0, -13, 0).end()
                .transform(ItemDisplayContext.GUI).rotation(0, 180, 0).translation(0, -11, 0).end()
                .transform(ItemDisplayContext.HEAD).translation(0, -7.5F, -6.5F).end()
                .transform(ItemDisplayContext.FIXED).translation(0, -10.5F, 0).end()
                .end();

        MineraculousArmors.MIRACULOUS.getAll().forEach(item -> singleTexture(item.getId().getPath(), mcItemLoc("generated"), "layer0", modItemLoc("miraculous/armor")));
        MineraculousArmors.KAMIKOTIZATION.getAll().forEach(item -> singleTexture(item.getId().getPath(), mcItemLoc("generated"), "layer0", modItemLoc("kamikotization_armor")));

        withEntityModel(MineraculousItems.MIRACULOUS).guiLight(BlockModel.GuiLight.FRONT);

        basicItem(MineraculousItems.CATACLYSM_DUST.get());
        basicItem(MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE);
        basicItem(MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE);
        basicItem(MineraculousItems.BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE);
        basicItem(MineraculousBlocks.HIBISCUS_BUSH.asItem());

        basicBlockItem(MineraculousBlocks.CATACLYSM_BLOCK);

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
        withSeparateInventoryModel(MineraculousItems.LADYBUG_YOYO, inHandLadybugYoyo, inventoryLadybugYoyo)
                .override()
                .predicate(MineraculousItemProperties.ACTIVE, 1)
                .model(activeLadybugYoyo)
                .end()
                .override()
                .predicate(MineraculousItemProperties.ACTIVE, 1)
                .predicate(MineraculousItemProperties.ABILITY, MineraculousItemProperties.getPropertyForAbility(LadybugYoyoItem.Ability.PHONE))
                .model(withSeparateInventoryModel(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_phone"), inHandLadybugYoyo, basicItem(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_phone"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.ACTIVE, 1)
                .predicate(MineraculousItemProperties.ABILITY, MineraculousItemProperties.getPropertyForAbility(LadybugYoyoItem.Ability.PURIFY))
                .model(withSeparateInventoryModel(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_purify"), inHandLadybugYoyo, basicItem(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_purify"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.ACTIVE, 1)
                .predicate(MineraculousItemProperties.ABILITY, MineraculousItemProperties.getPropertyForAbility(LadybugYoyoItem.Ability.SPYGLASS))
                .model(withSeparateInventoryModel(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_spyglass"), inHandLadybugYoyo, basicItem(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_spyglass"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.ACTIVE, 1)
                .predicate(MineraculousItemProperties.ABILITY, MineraculousItemProperties.getPropertyForAbility(LadybugYoyoItem.Ability.SPYGLASS) + 1)
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
                .model(withSeparateInventoryModel(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_blocking"), inHandBlockingLadybugYoyo, inventoryLandedLadybugYoyo))
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
        withSeparateInventoryModel(MineraculousItems.CAT_STAFF, inHandCatStaff, inventoryCatStaff)
                .override()
                .predicate(MineraculousItemProperties.ACTIVE, 1)
                .model(withSeparateInventoryModel(MineraculousItems.CAT_STAFF.getId().withSuffix("_active"), inHandCatStaff, basicItem(MineraculousItems.CAT_STAFF.getId().withSuffix("_active"))))
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
        ItemModelBuilder bladeButterflyCane = withSeparateInventoryModel(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_blade"), inHandButterflyCaneBlade, basicInventoryItem(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_blade")));
        withSeparateInventoryModel(MineraculousItems.BUTTERFLY_CANE, inHandButterflyCane, inventoryButterflyCane)
                .override()
                .predicate(MineraculousItemProperties.ABILITY, MineraculousItemProperties.getPropertyForAbility(ButterflyCaneItem.Ability.BLADE))
                .model(bladeButterflyCane)
                .end()
                .override()
                .predicate(MineraculousItemProperties.ABILITY, MineraculousItemProperties.getPropertyForAbility(ButterflyCaneItem.Ability.BLADE) + 1)
                .model(withSeparateInventoryModel(MineraculousItems.BUTTERFLY_CANE, inHandButterflyCane, inventoryButterflyCane))
                .end()
                .override()
                .predicate(MineraculousItemProperties.ABILITY, MineraculousItemProperties.getPropertyForAbility(ButterflyCaneItem.Ability.KAMIKO_STORE))
                .predicate(MineraculousItemProperties.STORING, 0)
                // TODO: Custom item texture
                .model(withSeparateInventoryModel(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_kamiko_store"), inHandButterflyCane, basicInventoryItem(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_phone"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.ABILITY, MineraculousItemProperties.getPropertyForAbility(ButterflyCaneItem.Ability.KAMIKO_STORE))
                .predicate(MineraculousItemProperties.STORING, 1)
                .model(withSeparateInventoryModel(MineraculousItems.BUTTERFLY_CANE, inHandButterflyCane, inventoryButterflyCane))
                .end()
                .override()
                .predicate(MineraculousItemProperties.ABILITY, MineraculousItemProperties.getPropertyForAbility(ButterflyCaneItem.Ability.PHONE))
                .model(withSeparateInventoryModel(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_phone"), inHandButterflyCane, basicInventoryItem(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_phone"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.ABILITY, MineraculousItemProperties.getPropertyForAbility(ButterflyCaneItem.Ability.SPYGLASS))
                // TODO: Custom item texture
                .model(withSeparateInventoryModel(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_spyglass"), inHandButterflyCane, basicInventoryItem(MineraculousItems.BUTTERFLY_CANE.getId().withSuffix("_phone"))))
                .end()
                .override()
                .predicate(MineraculousItemProperties.ABILITY, MineraculousItemProperties.getPropertyForAbility(ButterflyCaneItem.Ability.SPYGLASS) + 1)
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
