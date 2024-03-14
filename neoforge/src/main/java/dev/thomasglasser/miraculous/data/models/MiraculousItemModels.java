package dev.thomasglasser.miraculous.data.models;

import dev.thomasglasser.miraculous.Miraculous;
import dev.thomasglasser.miraculous.world.item.MiraculousItems;
import dev.thomasglasser.miraculous.world.item.armor.MiraculousArmors;
import dev.thomasglasser.tommylib.api.data.models.ExtendedItemModelProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MiraculousItemModels extends ExtendedItemModelProvider
{
	public MiraculousItemModels(PackOutput output, ExistingFileHelper existingFileHelper)
	{
		super(output, Miraculous.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels()
	{
		if (existingFileHelper.exists(Miraculous.modLoc("models/item/cat_miraculous_base.json"), PackType.CLIENT_RESOURCES))
		{
			ResourceLocation catBase = Miraculous.modLoc("item/cat_miraculous_base");
			existingFileHelper.trackGenerated(catBase, ModelProvider.MODEL);
			String ringPath = MiraculousItems.CAT_MIRACULOUS.getId().getPath();
			withExistingParent(ringPath + "_default", catBase)
					.texture("base", Miraculous.modLoc("item/" + ringPath + "_default"))
					.texture("symbol", Miraculous.modLoc("item/empty"));
			withExistingParent(ringPath + "_powered", catBase)
					.texture("base", Miraculous.modLoc("item/" + ringPath + "_powered"))
					.texture("symbol", Miraculous.modLoc("item/cat_symbol"));

			MiraculousArmors.MIRACULOUS_SETS.forEach(armorSet -> armorSet.getAll().forEach(item ->
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

		spawnEgg(MiraculousItems.PLAGG_SPAWN_EGG);
	}
}
