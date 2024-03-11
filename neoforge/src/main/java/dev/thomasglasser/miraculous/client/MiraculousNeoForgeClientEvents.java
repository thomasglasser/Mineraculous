package dev.thomasglasser.miraculous.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.event.ModelEvent;

import java.util.Map;

public class MiraculousNeoForgeClientEvents
{
	public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event)
	{
		ResourceManager manager = Minecraft.getInstance().getResourceManager();
		Map<ResourceLocation, Resource> map = manager.listResources("models/item", (location -> location.getPath().endsWith(".json") && location.getPath().contains("_miraculous_")));
		for (ResourceLocation rl : map.keySet())
		{
			ResourceLocation stripped = new ResourceLocation(rl.getNamespace(), rl.getPath().substring("models/".length(), rl.getPath().indexOf(".json")));
			event.register(stripped);
		}
	}
}
