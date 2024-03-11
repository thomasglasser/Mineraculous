package dev.thomasglasser.miraculous;

import dev.thomasglasser.miraculous.world.item.MiraculousCreativeModeTabs;
import dev.thomasglasser.miraculous.world.item.MiraculousItems;
import dev.thomasglasser.miraculous.world.item.armor.MiraculousArmors;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Miraculous
{
    public static final String MOD_ID = "miraculous";
    public static final String MOD_NAME = "Miraculous: Tales of Ladyblock and Craft Noir";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        LOGGER.info("Initializing {} for {} in a {} environment...", MOD_NAME, TommyLibServices.PLATFORM.getPlatformName(), TommyLibServices.PLATFORM.getEnvironmentName());

        MiraculousItems.init();
        MiraculousArmors.init();
        MiraculousCreativeModeTabs.init();
    }

    public static ResourceLocation modLoc(String s)
    {
        return new ResourceLocation(MOD_ID, s);
    }
}