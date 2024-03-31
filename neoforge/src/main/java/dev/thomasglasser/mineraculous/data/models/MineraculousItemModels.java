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

import java.util.List;

public class MineraculousItemModels extends ExtendedItemModelProvider
{
	public MineraculousItemModels(PackOutput output, ExistingFileHelper existingFileHelper)
	{
		super(output, Mineraculous.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels()
	{
		List<String> miraculous = List.of(
				"cat"
		);
		for (String key : miraculous)
		{
			if (existingFileHelper.exists(Mineraculous.modLoc("models/item/miraculous/" + key + "_miraculous_base.json"), PackType.CLIENT_RESOURCES))
			{
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

		MineraculousArmors.MIRACULOUS_SETS.forEach(armorSet -> armorSet.getAll().forEach(item ->
		{
			String nameForSlot = switch (armorSet.getForItem(item.get())) {
				case FEET -> "boots";
				case LEGS -> "leggings";
				case CHEST -> "chestplate";
				case HEAD -> "mask";
				default -> null;
			};

			basicItem(modLoc(armorSet.getName() + "_" + nameForSlot));
		}));

		spawnEgg(MineraculousItems.PLAGG_SPAWN_EGG);

		basicItem(MineraculousItems.CATACLYSM_DUST.get());
	}
}
