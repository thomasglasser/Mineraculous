package dev.thomasglasser.mineraculous.impl.client;

import dev.thomasglasser.mineraculous.impl.client.renderer.entity.layers.BetaTesterCosmeticOptions;
import net.neoforged.neoforge.common.ModConfigSpec;

public class MineraculousClientConfig {
    private static final MineraculousClientConfig INSTANCE = new MineraculousClientConfig();

    // Cosmetics
    public static final String COSMETICS = "cosmetics";
    public static final String SELF = "self";
    public final ModConfigSpec.BooleanValue displaySelfBetaTesterCosmetic;
    public final ModConfigSpec.EnumValue<BetaTesterCosmeticOptions> selfBetaTesterCosmeticChoice;
    public final ModConfigSpec.BooleanValue displaySelfDevTeamCosmetic;
    public final ModConfigSpec.BooleanValue displaySelfLegacyDevTeamCosmetic;
    public static final String OTHERS = "others";
    public final ModConfigSpec.BooleanValue displayOthersBetaTesterCosmetic;
    public final ModConfigSpec.BooleanValue displayOthersDevTeamCosmetic;
    public final ModConfigSpec.BooleanValue displayOthersLegacyDevTeamCosmetic;

    // Radial Menu
    public static final String RADIAL_MENU = "radial_menu";
    public final ModConfigSpec.IntValue animationSpeed;

    private final ModConfigSpec configSpec;

    public MineraculousClientConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push(COSMETICS);
        builder.push(SELF);
        displaySelfBetaTesterCosmetic = builder
                .define("display_self_beta_tester_cosmetic", true);
        selfBetaTesterCosmeticChoice = builder
                .defineEnum("self_beta_tester_cosmetic_choice", BetaTesterCosmeticOptions.DERBY_HAT);
        displaySelfDevTeamCosmetic = builder
                .define("display_self_dev_team_cosmetic", true);
        displaySelfLegacyDevTeamCosmetic = builder
                .define("display_self_legacy_dev_team_cosmetic", true);
        builder.pop();
        builder.push(OTHERS);
        displayOthersBetaTesterCosmetic = builder
                .define("display_others_beta_tester_cosmetic", true);
        displayOthersDevTeamCosmetic = builder
                .define("display_others_dev_team_cosmetic", true);
        displayOthersLegacyDevTeamCosmetic = builder
                .define("display_others_legacy_dev_team_cosmetic", true);
        builder.pop();
        builder.pop();

        builder.push(RADIAL_MENU);
        animationSpeed = builder
                .defineInRange("animation_speed", 10, 1, 20);
        builder.pop();

        configSpec = builder.build();
    }

    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    public static MineraculousClientConfig get() {
        return INSTANCE;
    }
}
