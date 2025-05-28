package dev.thomasglasser.mineraculous.client;

import dev.thomasglasser.mineraculous.client.renderer.entity.layers.BetaTesterCosmeticOptions;
import net.neoforged.neoforge.common.ModConfigSpec;

public class MineraculousClientConfig {
    private static final MineraculousClientConfig INSTANCE = new MineraculousClientConfig();

    // Cosmetics
    public static final String COSMETICS = "cosmetics";
    public final ModConfigSpec.BooleanValue displayBetaTesterCosmetic;
    public final ModConfigSpec.EnumValue<BetaTesterCosmeticOptions> betaTesterCosmeticChoice;
    public final ModConfigSpec.BooleanValue displayDevTeamCosmetic;
    public final ModConfigSpec.BooleanValue displayLegacyDevTeamCosmetic;

    // Radial Menu
    public static final String RADIAL_MENU = "radial_menu";
    public final ModConfigSpec.IntValue animationSpeed;

    private final ModConfigSpec configSpec;

    public MineraculousClientConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push(COSMETICS);
        displayBetaTesterCosmetic = builder
                .define("display_beta_tester_cosmetic", true);
        betaTesterCosmeticChoice = builder
                .defineEnum("beta_tester_cosmetic_choice", BetaTesterCosmeticOptions.DERBY_HAT);
        displayDevTeamCosmetic = builder
                .define("display_dev_team_cosmetic", true);
        displayLegacyDevTeamCosmetic = builder
                .define("display_legacy_dev_team_cosmetic", true);
        builder.pop();

        builder.push(RADIAL_MENU);
        animationSpeed = builder
                .defineInRange("animation_speed", 10, 1, 20);

        configSpec = builder.build();
    }

    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    public static MineraculousClientConfig get() {
        return INSTANCE;
    }
}
