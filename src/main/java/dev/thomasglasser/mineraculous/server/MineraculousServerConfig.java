package dev.thomasglasser.mineraculous.server;

import dev.thomasglasser.mineraculous.world.level.storage.FlattenedLookDataHolder;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.ModConfigSpec;

public class MineraculousServerConfig {
    private static final MineraculousServerConfig INSTANCE = new MineraculousServerConfig();

    public final ModConfigSpec configSpec;

    // Miraculous
    public static final String MIRACULOUS = "miraculous";
    public final ModConfigSpec.BooleanValue enableCustomization;
    public final ModConfigSpec.EnumValue<PermissionMode> customizationPermissionsMode;
    public final ModConfigSpec.BooleanValue enableMiraculousTimer;
    public final ModConfigSpec.BooleanValue enableLimitedPower;
    public final ModConfigSpec.BooleanValue enableKamikotizationRejection;
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
        enableMiraculousTimer = builder
                .define("enable_miraculous_timer", true);
        enableLimitedPower = builder
                .define("enable_limited_power", true);
        enableKamikotizationRejection = builder
                .define("enable_kamikotization_rejection", true);
        maxCatStaffLength = builder
                .defineInRange("max_cat_staff_length", 64, 32, 128);
        builder.pop();

        builder.push(STEALING);
        stealingDuration = builder
                .defineInRange("stealing_duration", 5, 1, Integer.MAX_VALUE);
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

    public static boolean isCustomizationAllowed(Player player) {
        FlattenedLookDataHolder overworld = (FlattenedLookDataHolder) player.getServer().overworld();
        return get().enableCustomization.get() && (get().customizationPermissionsMode.get() == PermissionMode.WHITELIST ? overworld.mineraculous$isPlayerWhitelisted(player) : !overworld.mineraculous$isPlayerBlacklisted(player));
    }

    public enum PermissionMode {
        WHITELIST,
        BLACKLIST
    }
}
