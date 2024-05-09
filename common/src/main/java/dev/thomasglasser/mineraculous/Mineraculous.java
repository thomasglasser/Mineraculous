package dev.thomasglasser.mineraculous;

import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.commands.arguments.MineraculousCommandArgumentTypes;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.network.MineraculousPayloads;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.mineraculous.world.item.MineraculousCreativeModeTabs;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmorMaterials;
import dev.thomasglasser.mineraculous.world.item.armor.MineraculousArmors;
import dev.thomasglasser.mineraculous.world.level.block.MineraculousBlocks;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
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
        MineraculousKeyMappings.init();
        MineraculousPayloads.init();
        MineraculousParticleTypes.init();
        MineraculousCommandArgumentTypes.init();
        MineraculousBlocks.init();
        MineraculousArmorMaterials.init();
        MineraculousDataComponents.init();

        registerConfigs();

        TommyLibServices.ENTITY.registerDataSerializers(MOD_ID, Map.of(
                "charged", Kwami.CHARGED
        ));

        if (TommyLibServices.PLATFORM.isClientSide()) MineraculousClientUtils.init();
    }

    private static void registerConfigs()
    {
        // TODO: Update MidnightLib
//        MidnightConfig.init(MOD_ID, MineraculousServerConfig.class);
//        if (TommyLibServices.PLATFORM.isClientSide()) MidnightConfig.init(MOD_ID, MineraculousClientConfig.class);
    }

    public static ResourceLocation modLoc(String s)
    {
        return new ResourceLocation(MOD_ID, s);
    }

    public enum Dependencies
    {
        CURIOS("curios", "trinkets");

        private String neoId;
        private String fabricId;

        Dependencies(String neoId, String fabricId)
        {
            this.neoId = neoId;
            this.fabricId = fabricId;
        }

        Dependencies(String id)
        {
            this(id, id);
        }

        public String getFabricId()
        {
            return fabricId;
        }

        public String getNeoId()
        {
            return neoId;
        }

        public String getId()
        {
            return TommyLibServices.PLATFORM.getEnvironmentName().equals("Fabric") ? fabricId : neoId;
        }

        public ResourceLocation modLoc(String s)
        {
            return new ResourceLocation(getId(), s);
        }

        public ResourceLocation fabricLoc(String s)
        {
            return new ResourceLocation(fabricId, s);
        }

        public ResourceLocation neoLoc(String s)
        {
            return new ResourceLocation(neoId, s);
        }
    }
}