package dev.thomasglasser.mineraculous.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MineraculousClientConfig {
    public static final MineraculousClientConfig INSTANCE = new MineraculousClientConfig();

    private final ModConfigSpec configSpec;

    // Miraculous
    public final ModConfigSpec.BooleanValue enablePerPlayerCustomization;

    public MineraculousClientConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Settings for the Miraculous");
        builder.push("miraculous");
        enablePerPlayerCustomization = builder
                .comment("Enable resource pack support for per-player customization of miraculous items")
                .define("enable_per_player_customization", false);
        builder.pop();

        configSpec = builder.build();
    }

    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }
}
