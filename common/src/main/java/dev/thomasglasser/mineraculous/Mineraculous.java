package dev.thomasglasser.mineraculous;

import dev.thomasglasser.mineraculous.client.MineraculousClientConfig;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.mineraculous.world.item.MineraculousCreativeModeTabs;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import eu.midnightdust.lib.config.MidnightConfig;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Mineraculous
{
    public static final String MOD_ID = "mineraculous";
    public static final String MOD_NAME = "Mineraculous";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        LOGGER.info("Initializing {} for {} in a {} environment...", MOD_NAME, TommyLibServices.PLATFORM.getPlatformName(), TommyLibServices.PLATFORM.getEnvironmentName());

        MineraculousItems.init();
        MineraculousArmors.init();
        MineraculousCreativeModeTabs.init();
        MineraculousEntityTypes.init();

        registerConfigs();

        TommyLibServices.ENTITY.registerDataSerializers(MOD_ID, Map.of(
                "has_voice", Kwami.HAS_VOICE,
                "charged", Kwami.CHARGED
        ));
    }

    private static void registerConfigs()
    {
        MidnightConfig.init(MOD_ID, MineraculousServerConfig.class);
        if (TommyLibServices.PLATFORM.isClientSide()) MidnightConfig.init(MOD_ID, MineraculousClientConfig.class);
    }

    public static ResourceLocation modLoc(String s)
    {
        return new ResourceLocation(MOD_ID, s);
    }
}