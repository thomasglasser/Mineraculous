package dev.thomasglasser.mineraculous.data.models;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousItemDisplayContexts;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.models.ExtendedItemModelProvider;
import java.util.stream.Stream;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MineraculousItemModelProvider extends ExtendedItemModelProvider {
    public MineraculousItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Mineraculous.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (String key : Stream.of(
                MineraculousMiraculous.CAT).map(key -> key.location().getPath()).toList()) {
            ResourceLocation base = Mineraculous.modLoc("item/miraculous/" + key + "/base");
            existingFileHelper.trackGenerated(base, ModelProvider.MODEL);
            withExistingParent("item/miraculous/" + key + "/hidden", base).texture("base", Mineraculous.modLoc("item/miraculous/" + key + "/hidden")).texture("symbol", Mineraculous.modLoc("item/empty"));
            withExistingParent("item/miraculous/" + key + "/powered", base).texture("base", Mineraculous.modLoc("item/miraculous/" + key + "/powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + key + "/symbol"));
            withExistingParent("item/miraculous/" + key + "/powered_0", base).texture("base", Mineraculous.modLoc("item/miraculous/" + key + "/powered")).texture("symbol", Mineraculous.modLoc("item/empty"));
            withExistingParent("item/miraculous/" + key + "/powered_1", base).texture("base", Mineraculous.modLoc("item/miraculous/" + key + "/powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + key + "/symbol_1"));
            withExistingParent("item/miraculous/" + key + "/powered_2", base).texture("base", Mineraculous.modLoc("item/miraculous/" + key + "/powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + key + "/symbol_2"));
            withExistingParent("item/miraculous/" + key + "/powered_3", base).texture("base", Mineraculous.modLoc("item/miraculous/" + key + "/powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + key + "/symbol_3"));
            withExistingParent("item/miraculous/" + key + "/powered_4", base).texture("base", Mineraculous.modLoc("item/miraculous/" + key + "/powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + key + "/symbol_4"));
        }

        String ladybugKey = "ladybug";
        ResourceLocation ladybugBase = Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/base");
        existingFileHelper.trackGenerated(ladybugBase, ModelProvider.MODEL);
        ItemModelBuilder hiddenDoubleBase = withExistingParent("item/miraculous/" + ladybugKey + "/hidden_double", ladybugBase).texture("base", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/hidden")).texture("symbol", Mineraculous.modLoc("item/empty")).texture("base_right", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/hidden")).texture("symbol_right", Mineraculous.modLoc("item/empty"));
        ItemModelBuilder hiddenSingleBase = withExistingParent("item/miraculous/" + ladybugKey + "/hidden_single", ladybugBase).texture("base", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/hidden")).texture("symbol", Mineraculous.modLoc("item/empty")).texture("base_right", Mineraculous.modLoc("item/empty")).texture("symbol_right", Mineraculous.modLoc("item/empty"));
        ItemModelBuilder poweredDoubleBase = withExistingParent("item/miraculous/" + ladybugKey + "/powered_double", ladybugBase).texture("base", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/symbol")).texture("base_right", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol_right", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/symbol"));
        ItemModelBuilder poweredSingleBase = withExistingParent("item/miraculous/" + ladybugKey + "/powered_single", ladybugBase).texture("base", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/symbol")).texture("base_right", Mineraculous.modLoc("item/empty")).texture("symbol_right", Mineraculous.modLoc("item/empty"));
        ItemModelBuilder powered0DoubleBase = withExistingParent("item/miraculous/" + ladybugKey + "/powered_0_double", ladybugBase).texture("base", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol", Mineraculous.modLoc("item/empty")).texture("base_right", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol_right", Mineraculous.modLoc("item/empty"));
        ItemModelBuilder powered0SingleBase = withExistingParent("item/miraculous/" + ladybugKey + "/powered_0_single", ladybugBase).texture("base", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol", Mineraculous.modLoc("item/empty")).texture("base_right", Mineraculous.modLoc("item/empty")).texture("symbol_right", Mineraculous.modLoc("item/empty"));
        ItemModelBuilder powered1DoubleBase = withExistingParent("item/miraculous/" + ladybugKey + "/powered_1_double", ladybugBase).texture("base", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/symbol_1")).texture("base_right", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol_right", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/symbol_1"));
        ItemModelBuilder powered1SingleBase = withExistingParent("item/miraculous/" + ladybugKey + "/powered_1_single", ladybugBase).texture("base", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/symbol_1")).texture("base_right", Mineraculous.modLoc("item/empty")).texture("symbol_right", Mineraculous.modLoc("item/empty"));
        ItemModelBuilder powered2DoubleBase = withExistingParent("item/miraculous/" + ladybugKey + "/powered_2_double", ladybugBase).texture("base", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/symbol_2")).texture("base_right", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol_right", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/symbol_2"));
        ItemModelBuilder powered2SingleBase = withExistingParent("item/miraculous/" + ladybugKey + "/powered_2_single", ladybugBase).texture("base", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/symbol_2")).texture("base_right", Mineraculous.modLoc("item/empty")).texture("symbol_right", Mineraculous.modLoc("item/empty"));
        ItemModelBuilder powered3DoubleBase = withExistingParent("item/miraculous/" + ladybugKey + "/powered_3_double", ladybugBase).texture("base", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/symbol_3")).texture("base_right", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol_right", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/symbol_3"));
        ItemModelBuilder powered3SingleBase = withExistingParent("item/miraculous/" + ladybugKey + "/powered_3_single", ladybugBase).texture("base", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/symbol_3")).texture("base_right", Mineraculous.modLoc("item/empty")).texture("symbol_right", Mineraculous.modLoc("item/empty"));
        ItemModelBuilder powered4DoubleBase = withExistingParent("item/miraculous/" + ladybugKey + "/powered_4_double", ladybugBase).texture("base", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/symbol_4")).texture("base_right", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol_right", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/symbol_4"));
        ItemModelBuilder powered4SingleBase = withExistingParent("item/miraculous/" + ladybugKey + "/powered_4_single", ladybugBase).texture("base", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/symbol_4")).texture("base_right", Mineraculous.modLoc("item/empty")).texture("symbol_right", Mineraculous.modLoc("item/empty"));
        generatedModels.remove(hiddenDoubleBase.getLocation());
        generatedModels.remove(hiddenSingleBase.getLocation());
        generatedModels.remove(poweredDoubleBase.getLocation());
        generatedModels.remove(poweredSingleBase.getLocation());
        generatedModels.remove(powered0DoubleBase.getLocation());
        generatedModels.remove(powered0SingleBase.getLocation());
        generatedModels.remove(powered1DoubleBase.getLocation());
        generatedModels.remove(powered1SingleBase.getLocation());
        generatedModels.remove(powered2DoubleBase.getLocation());
        generatedModels.remove(powered2SingleBase.getLocation());
        generatedModels.remove(powered3DoubleBase.getLocation());
        generatedModels.remove(powered3SingleBase.getLocation());
        generatedModels.remove(powered4DoubleBase.getLocation());
        generatedModels.remove(powered4SingleBase.getLocation());
        withSeparateTransforms(Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/hidden"))
                .base(hiddenDoubleBase)
                .perspective(MineraculousItemDisplayContexts.CURIOS_EARRINGS.getValue(), hiddenSingleBase)
                .end();
        withSeparateTransforms(Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered"))
                .base(poweredDoubleBase)
                .perspective(MineraculousItemDisplayContexts.CURIOS_EARRINGS.getValue(), poweredSingleBase)
                .end();
        withSeparateTransforms(Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered_0"))
                .base(powered0DoubleBase)
                .perspective(MineraculousItemDisplayContexts.CURIOS_EARRINGS.getValue(), powered0SingleBase)
                .end();
        withSeparateTransforms(Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered_1"))
                .base(powered1DoubleBase)
                .perspective(MineraculousItemDisplayContexts.CURIOS_EARRINGS.getValue(), powered1SingleBase)
                .end();
        withSeparateTransforms(Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered_2"))
                .base(powered2DoubleBase)
                .perspective(MineraculousItemDisplayContexts.CURIOS_EARRINGS.getValue(), powered2SingleBase)
                .end();
        withSeparateTransforms(Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered_3"))
                .base(powered3DoubleBase)
                .perspective(MineraculousItemDisplayContexts.CURIOS_EARRINGS.getValue(), powered3SingleBase)
                .end();
        withSeparateTransforms(Mineraculous.modLoc("item/miraculous/" + ladybugKey + "/powered_4"))
                .base(powered4DoubleBase)
                .perspective(MineraculousItemDisplayContexts.CURIOS_EARRINGS.getValue(), powered4SingleBase)
                .end();

        String butterflyKey = "butterfly";
        ResourceLocation butterflyBase = Mineraculous.modLoc("item/miraculous/" + butterflyKey + "/base");
        existingFileHelper.trackGenerated(butterflyBase, ModelProvider.MODEL);
        withExistingParent("item/miraculous/" + butterflyKey + "/hidden", butterflyBase).texture("texture", Mineraculous.modLoc("item/miraculous/" + butterflyKey + "/hidden"));
        withExistingParent("item/miraculous/" + butterflyKey + "/powered", butterflyBase).texture("texture", Mineraculous.modLoc("item/miraculous/" + butterflyKey + "/powered"));
        withExistingParent("item/miraculous/" + butterflyKey + "/powered_0", butterflyBase).texture("texture", Mineraculous.modLoc("item/miraculous/" + butterflyKey + "/powered_0"));
        withExistingParent("item/miraculous/" + butterflyKey + "/powered_1", butterflyBase).texture("texture", Mineraculous.modLoc("item/miraculous/" + butterflyKey + "/powered_1"));
        withExistingParent("item/miraculous/" + butterflyKey + "/powered_2", butterflyBase).texture("texture", Mineraculous.modLoc("item/miraculous/" + butterflyKey + "/powered_2"));
        withExistingParent("item/miraculous/" + butterflyKey + "/powered_3", butterflyBase).texture("texture", Mineraculous.modLoc("item/miraculous/" + butterflyKey + "/powered_3"));
        withExistingParent("item/miraculous/" + butterflyKey + "/powered_4", butterflyBase).texture("texture", Mineraculous.modLoc("item/miraculous/" + butterflyKey + "/powered_4"));

        withExistingParent("item/miraculous/armor", "item/generated").texture("layer0", Mineraculous.modLoc("item/miraculous/armor"));
        withExistingParent("item/kamikotization/armor", "item/generated").texture("layer0", Mineraculous.modLoc("item/kamikotization/armor"));

        withEntityModel(MineraculousItems.MIRACULOUS).guiLight(BlockModel.GuiLight.FRONT);
        MineraculousArmors.MIRACULOUS.getAll().forEach(item -> withEntityModel(item).guiLight(BlockModel.GuiLight.FRONT));

        MineraculousArmors.KAMIKOTIZATION.getAll().forEach(item -> withEntityModel(item).guiLight(BlockModel.GuiLight.FRONT));

        spawnEggItem(MineraculousItems.KAMIKO_SPAWN_EGG);

        basicItem(MineraculousItems.CATACLYSM_DUST.get());
        basicItem(MineraculousItems.LADYBUG_ARMOR_TRIM_SMITHING_TEMPLATE);
        basicItem(MineraculousItems.CAT_ARMOR_TRIM_SMITHING_TEMPLATE);
        basicItem(MineraculousItems.BUTTERFLY_ARMOR_TRIM_SMITHING_TEMPLATE);
        basicItem(MineraculousBlocks.HIBISCUS_BUSH.asItem());

        // TODO: Cheese wedges
//		for (CheeseBlock.Age age: CheeseBlock.Age.values()) {
//			basicBlockItem(MineraculousItems.CHEESE_WEDGES.get(age).get());
//			basicBlockItem(MineraculousItems.CAMEMBERT_WEDGES.get(age).get());
//		}

        basicBlockItem(MineraculousBlocks.CATACLYSM_BLOCK);

        ItemModelBuilder inHandLadybugYoyo = withEntityModel(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_in_hand"))
                .transforms()
                .transform(MineraculousItemDisplayContexts.CURIOS_BELT.getValue()).rotation(-90, 0, 0).translation(-2, 10, 3.7f).scale(0.8f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).translation(0, 0, 2).scale(0.6f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).translation(0, 0, 2).scale(0.6f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).translation(2, 0, 1).scale(0.6f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).translation(2, 0, 1).scale(0.6f).end()
                .transform(ItemDisplayContext.HEAD).translation(0, 6.25f, 0).end()
                .end();
        ItemModelBuilder inventoryLadybugYoyo = basicInventoryItem(MineraculousItems.LADYBUG_YOYO);
        ItemModelBuilder inHandThrownLadybugYoyo = basicItem(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_thrown_in_hand"), "empty");
        ItemModelBuilder inventoryThrownLadybugYoyo = basicItem(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_thrown_inventory"), MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_thrown").getPath());
        generatedModels.remove(inHandThrownLadybugYoyo.getLocation());
        generatedModels.remove(inventoryThrownLadybugYoyo.getLocation());
        ItemModelBuilder thrownLadybugYoyo = withSeparateTransforms(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_thrown"))
                .base(inHandThrownLadybugYoyo)
                .perspective(ItemDisplayContext.GUI, inventoryThrownLadybugYoyo)
                .perspective(ItemDisplayContext.FIXED, inventoryThrownLadybugYoyo)
                .perspective(ItemDisplayContext.GROUND, inventoryThrownLadybugYoyo)
                .end();
        withEntityModelInHand(MineraculousItems.LADYBUG_YOYO, inHandLadybugYoyo, inventoryLadybugYoyo)
                .override()
                .predicate(LadybugYoyoItem.EXTENDED_PROPERTY_ID, 1)
                .model(withEntityModelInHand(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_extended"), inHandLadybugYoyo, basicItem(MineraculousItems.LADYBUG_YOYO.getId().withSuffix("_extended"))))
                .end()
                .override()
                .predicate(LadybugYoyoItem.THROWN_PROPERTY_ID, 1)
                .model(thrownLadybugYoyo)
                .end();

        ItemModelBuilder inHandCatStaff = withEntityModel(MineraculousItems.CAT_STAFF.getId().withSuffix("_in_hand"))
                .transforms()
                .transform(MineraculousItemDisplayContexts.CURIOS_BELT.getValue()).rotation(0, 180, 90).translation(-4.2f, 8, 2.7f).scale(0.7f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(0, -180, 0).translation(0, -3.5f, 1.25f).scale(0.7f).end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(0, -180, 0).translation(0, -3.5f, 1.25f).scale(0.7f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 165, 0).translation(0, 1.5f, 1.25f).scale(0.7f).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 165, 0).translation(0, 1.5f, 1.25f).scale(0.7f).end()
                .transform(ItemDisplayContext.HEAD).rotation(-54, 0, 0).translation(0, 1.75f, -2.75f).end()
                .end();
        ItemModelBuilder inventoryCatStaff = basicInventoryItem(MineraculousItems.CAT_STAFF);
        withEntityModelInHand(MineraculousItems.CAT_STAFF, inHandCatStaff, inventoryCatStaff)
                .override()
                .predicate(CatStaffItem.EXTENDED_PROPERTY_ID, 1)
                .model(withEntityModelInHand(MineraculousItems.CAT_STAFF.getId().withSuffix("_extended"), inHandCatStaff, basicItem(MineraculousItems.CAT_STAFF.getId().withSuffix("_extended"))))
                .end();
    }
}
