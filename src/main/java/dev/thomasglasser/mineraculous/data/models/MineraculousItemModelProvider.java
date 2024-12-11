package dev.thomasglasser.mineraculous.data.models;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.models.ExtendedItemModelProvider;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
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
        List<String> symboledMiraculous = Stream.of(
                MineraculousMiraculous.CAT,
                MineraculousMiraculous.LADYBUG).map(key -> key.location().getPath()).toList();
        for (String key : symboledMiraculous) {
            if (existingFileHelper.exists(Mineraculous.modLoc("models/item/miraculous/" + key + "/base.json"), PackType.CLIENT_RESOURCES)) {
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
        }

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

        withEntityModel(MineraculousItems.MIRACULOUS).guiLight(BlockModel.GuiLight.FRONT);
        MineraculousArmors.MIRACULOUS.getAll().forEach(item -> withEntityModel(item).guiLight(BlockModel.GuiLight.FRONT));

        ItemModelBuilder inHandCatStaff = withEntityModel(MineraculousItems.CAT_STAFF.getId().withSuffix("_in_hand"))
                .transforms()
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
