package dev.thomasglasser.mineraculous.impl.server;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MineraculousServerConfig {
    private static final MineraculousServerConfig INSTANCE = new MineraculousServerConfig();

    public final ModConfigSpec configSpec;

    // Miraculous
    public static final String MIRACULOUS = "miraculous";
    public final ModConfigSpec.BooleanValue enableCustomization;
    public final ModConfigSpec.EnumValue<PermissionMode> customizationPermissionsMode;
    public final ModConfigSpec.IntValue kwamiSummonTime;
    public final ModConfigSpec.BooleanValue enableMiraculousTimer;
    public final ModConfigSpec.IntValue miraculousTimerDuration;
    public final ModConfigSpec.BooleanValue enableLimitedPower;
    public final ModConfigSpec.BooleanValue enableKamikotizationRejection;
    public final ModConfigSpec.IntValue luckyCharmSummonTimeMin;
    public final ModConfigSpec.IntValue luckyCharmSummonTimeMax;
    public final ModConfigSpec.IntValue maxCatStaffLength;

    // Stealing
    public static final String STEALING = "stealing";
    public final ModConfigSpec.IntValue stealingDuration;
    public final ModConfigSpec.BooleanValue enableUniversalStealing;
    public final ModConfigSpec.BooleanValue enableSleepStealing;
    public final ModConfigSpec.IntValue wakeUpChance;

    public MineraculousServerConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push(MIRACULOUS);
        enableCustomization = builder
                .define("enable_customization", false);
        customizationPermissionsMode = builder
                .defineEnum("customization_permission_mode", PermissionMode.WHITELIST);
        kwamiSummonTime = builder
                .defineInRange("kwami_summon_time", 2, 1, 60);
        enableMiraculousTimer = builder
                .define("enable_miraculous_timer", true);
        miraculousTimerDuration = builder
                .defineInRange("miraculous_timer_duration", 60 * 5, 1, 60 * 10);
        enableLimitedPower = builder
                .define("enable_limited_power", true);
        enableKamikotizationRejection = builder
                .define("enable_kamikotization_rejection", true);
        luckyCharmSummonTimeMin = builder
                .defineInRange("lucky_charm_summon_time_min", 3, 0, Integer.MAX_VALUE);
        luckyCharmSummonTimeMax = builder
                .defineInRange("lucky_charm_summon_time_max", 6, 0, Integer.MAX_VALUE);
        maxCatStaffLength = builder
                .defineInRange("max_cat_staff_length", 64, 32, 128);
        builder.pop();

        builder.push(STEALING);
        stealingDuration = builder
                .defineInRange("stealing_duration", 3, 1, Integer.MAX_VALUE);
        enableUniversalStealing = builder
                .define("enable_universal_stealing", true);
        enableSleepStealing = builder
                .define("enable_sleep_stealing", true);
        wakeUpChance = builder
                .defineInRange("wake_up_chance", 10, 0, 100);
        builder.pop();

        configSpec = builder.build();
    }

    public ModConfigSpec getConfigSpec() {
        return configSpec;
    }

    public static MineraculousServerConfig get() {
        return INSTANCE;
    }

    public enum PermissionMode {
        WHITELIST,
        BLACKLIST
    }
}
