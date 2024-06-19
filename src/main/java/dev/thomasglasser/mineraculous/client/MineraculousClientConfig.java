package dev.thomasglasser.mineraculous.client;

import eu.midnightdust.lib.config.MidnightConfig;

public class MineraculousClientConfig extends MidnightConfig
{
	@Comment(category = "miraculous", centered = true) public static final String miraculous_comment = "Settings for the Miraculous";
	@Comment(category = "miraculous") public static final String enable_per_player_customization_comment = "Enable resource pack support for per-player customization of miraculous items";
	@Entry(category = "miraculous")
	public static boolean enablePerPlayerCustomization = false;
}
