package dev.thomasglasser.mineraculous.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MineraculousClientConfig {
    private static final MineraculousClientConfig INSTANCE = new MineraculousClientConfig();

    public final ModConfigSpec configSpec;

    // Cosmetics
    public static final String COSMETICS = "cosmetics";
    public final ModConfigSpec.BooleanValue displaySnapshotTesterCosmetic;
//    public final ModConfigSpec.EnumValue<SnapshotTesterCosmeticOptions> snapshotTesterCosmeticChoice;
    public final ModConfigSpec.BooleanValue displayDevTeamCosmetic;
    public final ModConfigSpec.BooleanValue displayLegacyDevTeamCosmetic;

    // Tool Wheel
    public static final String TOOL_WHEEL = "tool_wheel";
    public final ModConfigSpec.IntValue animationSpeed;

    public MineraculousClientConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push(COSMETICS);
        displaySnapshotTesterCosmetic = builder
                .define("display_snapshot_tester_cosmetic", true);
        // TODO: Implement
//        snapshotTesterCosmeticChoice = builder
//                .defineEnum("snapshot_tester_cosmetic_choice", SnapshotTesterCosmeticOptions.BAMBOO_HAT);
        displayDevTeamCosmetic = builder
                .define("display_dev_team_cosmetic", true);
        displayLegacyDevTeamCosmetic = builder
                .define("display_legacy_dev_team_cosmetic", true);
        builder.pop();

        builder.push(TOOL_WHEEL);
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
