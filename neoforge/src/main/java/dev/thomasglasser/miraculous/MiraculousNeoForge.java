package dev.thomasglasser.miraculous;

import dev.thomasglasser.miraculous.client.MiraculousNeoForgeClientEvents;
import dev.thomasglasser.miraculous.data.MiraculousDataGenerators;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Miraculous.MOD_ID)
public class MiraculousNeoForge
{
    public MiraculousNeoForge(IEventBus bus) {
        Miraculous.init();

        bus.addListener(MiraculousDataGenerators::onGatherData);

        if (TommyLibServices.PLATFORM.isClientSide())
        {
            bus.addListener(MiraculousNeoForgeClientEvents::onRegisterAdditionalModels);
        }
    }
}