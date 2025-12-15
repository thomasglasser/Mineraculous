package dev.thomasglasser.mineraculous.impl.server;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MineraculousServerConfig {
    private static final MineraculousServerConfig INSTANCE = new MineraculousServerConfig();

    public final ModConfigSpec configSpec;

    // Miraculous
    public static final String MIRACULOUS = "miraculous";
    public final ModConfigSpec.BooleanValue enableBuffsOnTransformation;
    public final ModConfigSpec.IntValue maxToolLength;

    public static final String CUSTOMIZATION = "customization";
    public final ModConfigSpec.BooleanValue enableCustomization;
    public final ModConfigSpec.EnumValue<PermissionMode> customizationPermissionsMode;

    public static final String ABILITIES = "abilities";
    public final ModConfigSpec.BooleanValue enableMiraculousTimer;
    public final ModConfigSpec.IntValue miraculousTimerDuration;
    public final ModConfigSpec.BooleanValue enableLimitedPower;
    public final ModConfigSpec.BooleanValue enableKamikotizationRejection;
    public final ModConfigSpec.BooleanValue enableKamikoReplication;
    public final ModConfigSpec.IntValue maxKamikoReplicas;
    public final ModConfigSpec.BooleanValue forceKamikotizeCreativePlayers;
    public final ModConfigSpec.IntValue luckyCharmSummonTimeMin;
    public final ModConfigSpec.IntValue luckyCharmSummonTimeMax;
    public final ModConfigSpec.EnumValue<MiraculousLadybugReversionMode> miraculousLadybugReversionMode;
    public final ModConfigSpec.IntValue miraculousLadybugSpeed;

    public static final String KWAMIS = "kwamis";
    public final ModConfigSpec.IntValue kwamiSummonTime;
    public final ModConfigSpec.BooleanValue enableKwamiItemCharging;

    // Stealing
    public static final String STEALING = "stealing";
    public final ModConfigSpec.IntValue stealingDuration;
    public final ModConfigSpec.BooleanValue enableUniversalStealing;
    public final ModConfigSpec.BooleanValue enableSleepStealing;
    public final ModConfigSpec.IntValue wakeUpChance;

    public MineraculousServerConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push(MIRACULOUS);
        enableBuffsOnTransformation = builder
                .define("enable_buffs_on_transformation", false);
        maxToolLength = builder
                .defineInRange("max_tool_length", 128, 32, 512);
        builder.push(CUSTOMIZATION);
        enableCustomization = builder
                .define("enable_customization", false);
        customizationPermissionsMode = builder
                .defineEnum("customization_permission_mode", PermissionMode.WHITELIST);
        builder.pop();
        builder.push(ABILITIES);
        enableMiraculousTimer = builder
                .define("enable_miraculous_timer", true);
        miraculousTimerDuration = builder
                .defineInRange("miraculous_timer_duration", 60 * 5, 1, 60 * 10);
        enableLimitedPower = builder
                .define("enable_limited_power", true);
        enableKamikotizationRejection = builder
                .define("enable_kamikotization_rejection", true);
        enableKamikoReplication = builder
                .define("enable_kamiko_replication", true);
        maxKamikoReplicas = builder
                .defineInRange("max_kamiko_replicas", 32, 0, 128);
        forceKamikotizeCreativePlayers = builder
                .define("force_kamikotize_creative_players", false);
        luckyCharmSummonTimeMin = builder
                .defineInRange("lucky_charm_summon_time_min", 3, 0, Integer.MAX_VALUE);
        luckyCharmSummonTimeMax = builder
                .defineInRange("lucky_charm_summon_time_max", 6, 0, Integer.MAX_VALUE);
        miraculousLadybugReversionMode = builder
                .defineEnum("miraculous_ladybug_reversion_mode", MiraculousLadybugReversionMode.CLUSTERED);
        miraculousLadybugSpeed = builder
                .defineInRange("miraculous_ladybug_speed", 70, 60, 100);
        builder.pop();
        builder.push(KWAMIS);
        kwamiSummonTime = builder
                .defineInRange("kwami_summon_time", 3, 2, 6);
        enableKwamiItemCharging = builder
                .define("enable_kwami_item_charging", true);
        builder.pop();
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

    public enum MiraculousLadybugReversionMode {
        INSTANT,
        CLUSTERED,
        INDIVIDUAL
    }
}
