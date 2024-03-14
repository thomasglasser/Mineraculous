package dev.thomasglasser.miraculous;

import dev.thomasglasser.miraculous.client.MiraculousNeoForgeClientEvents;
import dev.thomasglasser.miraculous.data.MiraculousDataGenerators;
import dev.thomasglasser.miraculous.platform.NeoForgeDataHelper;
import dev.thomasglasser.miraculous.world.entity.MiraculousNeoForgeEntityEvents;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Miraculous.MOD_ID)
public class MiraculousNeoForge
{
    public MiraculousNeoForge(IEventBus bus) {
        Miraculous.init();

        NeoForgeDataHelper.ATTACHMENT_TYPES.register(bus);

        bus.addListener(MiraculousDataGenerators::onGatherData);

        addModListeners(bus);

        if (TommyLibServices.PLATFORM.isClientSide())
        {
            bus.addListener(MiraculousNeoForgeClientEvents::onRegisterAdditionalModels);
            bus.addListener(MiraculousNeoForgeClientEvents::onRegisterRenderer);
        }
    }

    private void addModListeners(IEventBus bus)
    {
        bus.addListener(MiraculousNeoForgeEntityEvents::onEntityAttributeCreation);
    }
}