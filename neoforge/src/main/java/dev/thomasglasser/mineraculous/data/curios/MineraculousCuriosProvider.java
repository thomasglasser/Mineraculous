package dev.thomasglasser.mineraculous.data.curios;

import dev.thomasglasser.mineraculous.Mineraculous;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

public class MineraculousCuriosProvider extends CuriosDataProvider
{
	public MineraculousCuriosProvider(PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries)
	{
		super(Mineraculous.MOD_ID, output, fileHelper, registries);
	}

	// Must add slots in trinkets file in Fabric project by hand
	@Override
	public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper)
	{
		createEntities("miraculous_holders")
				.addPlayer()
				.addSlots("ring");
	}
}
