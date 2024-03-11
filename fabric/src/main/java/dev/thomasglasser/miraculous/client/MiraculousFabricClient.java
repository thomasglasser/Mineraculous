package dev.thomasglasser.miraculous.client;

import dev.thomasglasser.miraculous.client.renderer.MiraculousBlockEntityWithoutLevelRenderer;
import dev.thomasglasser.miraculous.world.item.MiraculousItems;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MiraculousFabricClient implements ClientModInitializer
{

	@Override
	public void onInitializeClient()
	{
		MiraculousBlockEntityWithoutLevelRenderer bewlr = new MiraculousBlockEntityWithoutLevelRenderer();

		for (RegistryObject<Item> item : MiraculousItems.ITEMS.getEntries())
		{
			if (item.get() instanceof ModeledItem)
			{
				BuiltinItemRendererRegistry.INSTANCE.register(item.get(), (bewlr::renderByItem));
			}
		}

		PreparableModelLoadingPlugin.register(((resourceManager, executor) ->
		{
			Map<ResourceLocation, Resource> map = resourceManager.listResources("models/item", (location -> location.getPath().endsWith(".json") && location.getPath().contains("_miraculous_")));
			List<ModelResourceLocation> rls = new ArrayList<>();
			for (ResourceLocation rl : map.keySet())
			{
				ResourceLocation stripped = new ResourceLocation(rl.getNamespace(), rl.getPath().substring("models/item/".length(), rl.getPath().indexOf(".json")));
				rls.add(new ModelResourceLocation(stripped, "inventory"));
			}
			return CompletableFuture.supplyAsync(() -> rls, executor);
		}), (data, pluginContext) ->
				pluginContext.addModels(data));
	}
}
