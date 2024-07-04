package dev.thomasglasser.mineraculous.server;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MineraculousServerConfig {
    public static final MineraculousServerConfig INSTANCE = new MineraculousServerConfig();

    public final ModConfigSpec configSpec;

    // Stealing
    public final ModConfigSpec.IntValue stealingDuration;
    public final ModConfigSpec.BooleanValue enableUniversalStealing;
    public final ModConfigSpec.BooleanValue enableSleepStealing;
    public final ModConfigSpec.IntValue wakeUpChance;

    public MineraculousServerConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Settings for item stealing");
        builder.push("stealing");
        stealingDuration = builder
                .comment("Duration in seconds that the key must be held to steal an item")
                .defineInRange("stealing_duration", 5, 1, Integer.MAX_VALUE);
        enableUniversalStealing = builder
                .comment("Enable item stealing from all players all the time")
                .define("enable_universal_stealing", true);
        enableSleepStealing = builder
                .comment("Enable item stealing from players while they sleep")
                .define("enable_sleep_stealing", true);
        wakeUpChance = builder
                .comment("Percent chance that a player will wake up while being stolen from")
                .defineInRange("wake_up_chance", 10, 0, 100);
        builder.pop();

        configSpec = builder.build();
    }

    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }
}
