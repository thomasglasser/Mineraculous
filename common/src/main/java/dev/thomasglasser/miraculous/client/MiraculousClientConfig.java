package dev.thomasglasser.miraculous.client;

import eu.midnightdust.lib.config.MidnightConfig;

public class MiraculousClientConfig extends MidnightConfig
{
	// Miraculous Client Settings
	@Comment(category = "miraculous", centered = true) public static final String miraculous_comment = "Settings for the Miraculous";
	@Comment(category = "miraculous") public static final String enable_custom_hidden_variants_comment = "Enable resource pack support for custom hidden textures";
	@Entry(category = "miraculous")
	public static boolean enableCustomHiddenVariants = false;
}
