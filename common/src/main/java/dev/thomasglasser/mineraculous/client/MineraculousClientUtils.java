package dev.thomasglasser.mineraculous.client;

import dev.thomasglasser.mineraculous.client.renderer.MineraculousBlockEntityWithoutLevelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class MineraculousClientUtils
{
	private static final MineraculousBlockEntityWithoutLevelRenderer bewlr = new MineraculousBlockEntityWithoutLevelRenderer();

	public static MineraculousBlockEntityWithoutLevelRenderer getBewlr()
	{
		return bewlr;
	}

	public static void setShader(@Nullable ResourceLocation location)
	{
		if (location != null)
			Minecraft.getInstance().gameRenderer.loadEffect(location);
		else
			Minecraft.getInstance().gameRenderer.checkEntityPostEffect(null);
	}

	public static void init() {}
}
