package dev.thomasglasser.mineraculous.data.models;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.data.models.ExtendedItemModelProvider;
import java.util.List;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MineraculousItemModels extends ExtendedItemModelProvider {
    public MineraculousItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Mineraculous.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        List<String> miraculous = List.of(
                "cat");
        for (String key : miraculous) {
            if (existingFileHelper.exists(Mineraculous.modLoc("models/item/miraculous/" + key + "_miraculous_base.json"), PackType.CLIENT_RESOURCES)) {
                ResourceLocation base = Mineraculous.modLoc("item/miraculous/" + key + "_miraculous_base");
                existingFileHelper.trackGenerated(base, ModelProvider.MODEL);
                withExistingParent("item/miraculous/" + key + "_miraculous_hidden", base).texture("base", Mineraculous.modLoc("item/miraculous/" + key + "_miraculous_hidden")).texture("symbol", Mineraculous.modLoc("item/empty"));
                withExistingParent("item/miraculous/" + key + "_miraculous_powered", base).texture("base", Mineraculous.modLoc("item/miraculous/" + key + "_miraculous_powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + key + "_symbol"));
                withExistingParent("item/miraculous/" + key + "_miraculous_powered_0", base).texture("base", Mineraculous.modLoc("item/miraculous/" + key + "_miraculous_powered")).texture("symbol", Mineraculous.modLoc("item/empty"));
                withExistingParent("item/miraculous/" + key + "_miraculous_powered_1", base).texture("base", Mineraculous.modLoc("item/miraculous/" + key + "_miraculous_powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + key + "_symbol_1"));
                withExistingParent("item/miraculous/" + key + "_miraculous_powered_2", base).texture("base", Mineraculous.modLoc("item/miraculous/" + key + "_miraculous_powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + key + "_symbol_2"));
                withExistingParent("item/miraculous/" + key + "_miraculous_powered_3", base).texture("base", Mineraculous.modLoc("item/miraculous/" + key + "_miraculous_powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + key + "_symbol_3"));
                withExistingParent("item/miraculous/" + key + "_miraculous_powered_4", base).texture("base", Mineraculous.modLoc("item/miraculous/" + key + "_miraculous_powered")).texture("symbol", Mineraculous.modLoc("item/miraculous/" + key + "_symbol_4"));
            }
        }

        MineraculousArmors.MIRACULOUS_SETS.forEach(armorSet -> armorSet.getAll().forEach(item -> {
            String nameForSlot = switch (armorSet.getForItem(item.get())) {
                case FEET -> "boots";
                case LEGS -> "leggings";
                case CHEST -> "chestplate";
                case HEAD -> "mask";
                default -> null;
            };

            basicItem(modLoc(armorSet.getName() + "_" + nameForSlot));
        }));

        spawnEgg(MineraculousItems.TIKKI_SPAWN_EGG);
        spawnEgg(MineraculousItems.PLAGG_SPAWN_EGG);
        spawnEgg(MineraculousItems.KAMIKO_SPAWN_EGG);

        basicItem(MineraculousItems.CATACLYSM_DUST.get());

        // TODO: Cheese wedges
//		for (CheeseBlock.Age age: CheeseBlock.Age.values()) {
//			basicBlockItem(MineraculousItems.CHEESE_WEDGES.get(age).get());
//			basicBlockItem(MineraculousItems.CAMEMBERT_WEDGES.get(age).get());
//		}

        basicBlockItem(MineraculousBlocks.CATACLYSM_BLOCK);

        withEntityModel(MineraculousItems.CAT_MIRACULOUS).guiLight(BlockModel.GuiLight.FRONT);

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
