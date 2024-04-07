package dev.thomasglasser.mineraculous.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import net.minecraft.client.KeyMapping;

public class MineraculousKeyMappings
{
	public static final String MIRACULOUS_CATEGORY = "key.categories.mineraculous";

	public static final KeyMapping TRANSFORM = register("transform", InputConstants.KEY_M, MIRACULOUS_CATEGORY);
	public static final KeyMapping ACTIVATE_MAIN_POWER = register("activate_main_power", InputConstants.KEY_I, MIRACULOUS_CATEGORY);
	// TODO: Rename to "ability wheel"
	public static final KeyMapping OPEN_POWER_WHEEL = register("open_power_wheel", InputConstants.KEY_O, MIRACULOUS_CATEGORY);
	public static final KeyMapping ACTIVATE_TOOL = register("activate_tool", InputConstants.KEY_U, MIRACULOUS_CATEGORY);
	public static final KeyMapping ACTIVATE_TRAVELLING = register("activate_travelling", InputConstants.KEY_Y, KeyMapping.CATEGORY_MOVEMENT);

	private static KeyMapping register(String id, int key, String category)
	{
		return ClientUtils.registerKeyMapping(Mineraculous.modLoc(id), key, category);
	}

	public static void init() {}
}
