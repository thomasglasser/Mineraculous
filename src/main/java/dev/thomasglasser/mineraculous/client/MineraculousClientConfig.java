package dev.thomasglasser.mineraculous.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MineraculousClientConfig {
    public static final MineraculousClientConfig INSTANCE = new MineraculousClientConfig();

    private final ModConfigSpec configSpec;

    // Miraculous
    public static final String MIRACULOUS = "miraculous";
    public final ModConfigSpec.BooleanValue enablePerPlayerCustomization;

    public MineraculousClientConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push(MIRACULOUS);
        enablePerPlayerCustomization = builder
                .define("enable_per_player_customization", false);
        builder.pop();

        configSpec = builder.build();
    }

    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }
}
