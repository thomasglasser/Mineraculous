package dev.thomasglasser.mineraculous.server;

import eu.midnightdust.lib.config.MidnightConfig;

public class MineraculousServerConfig extends MidnightConfig {
    @Comment(category = "stealing", centered = true)
    public static final String stealing_comment = "Settings for item stealing";
    @Comment(category = "stealing")
    public static final String stealing_duration_comment = "Duration in seconds that the key must be held to steal an item";
    @Entry(category = "stealing")
    public static int stealingDuration = 5;
    @Comment(category = "stealing")
    public static final String enable_universal_stealing_comment = "Enable item stealing from all players all the time";
    @Entry(category = "stealing")
    public static boolean enableUniversalStealing = true;
    @Comment(category = "stealing")
    public static final String enable_sleep_stealing_comment = "Enable item stealing from players while they sleep";
    @Entry(category = "stealing")
    public static boolean enableSleepStealing = true;
    @Comment(category = "stealing")
    public static final String wake_up_chance_comment = "Percent chance that a player will wake up while being stolen from";
    @Entry(category = "stealing")
    public static int wakeUpChance = 10;
}
