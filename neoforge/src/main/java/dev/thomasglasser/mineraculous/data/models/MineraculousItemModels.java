package dev.thomasglasser.mineraculous.data.models;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.tommylib.api.data.models.ExtendedItemModelProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MineraculousItemModels extends ExtendedItemModelProvider
{
	public MineraculousItemModels(PackOutput output, ExistingFileHelper existingFileHelper)
	{
		super(output, Mineraculous.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels()
	{
		if (existingFileHelper.exists(Mineraculous.modLoc("models/item/cat_miraculous_base.json"), PackType.CLIENT_RESOURCES))
		{
			ResourceLocation catBase = Mineraculous.modLoc("item/cat_miraculous_base");
			existingFileHelper.trackGenerated(catBase, ModelProvider.MODEL);
			String ringPath = MineraculousItems.CAT_MIRACULOUS.getId().getPath();
			withExistingParent(ringPath + "_default", catBase)
					.texture("base", Mineraculous.modLoc("item/" + ringPath + "_default"))
					.texture("symbol", Mineraculous.modLoc("item/empty"));
			withExistingParent(ringPath + "_powered", catBase)
					.texture("base", Mineraculous.modLoc("item/" + ringPath + "_powered"))
					.texture("symbol", Mineraculous.modLoc("item/cat_symbol"));

			MineraculousArmors.MIRACULOUS_SETS.forEach(armorSet -> armorSet.getAll().forEach(item ->
			{
				String nameForSlot = switch (armorSet.getForItem(item.get())) {
					case FEET -> "boots";
					case LEGS -> "leggings";
					case CHEST -> "chestplate";
					case HEAD -> "mask";
					default -> null;
				};

				singleTexture(item.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/" + armorSet.getName() + "_" + nameForSlot));
			}));
		}

		spawnEgg(MineraculousItems.PLAGG_SPAWN_EGG);
	}
}
