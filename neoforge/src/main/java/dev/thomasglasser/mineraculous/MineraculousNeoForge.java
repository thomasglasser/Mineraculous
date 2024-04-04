package dev.thomasglasser.mineraculous;

import dev.thomasglasser.mineraculous.client.MineraculousNeoForgeClientEvents;
import dev.thomasglasser.mineraculous.commands.MineraculousNeoForgeCommandEvents;
import dev.thomasglasser.mineraculous.core.MineraculousNeoForgeCoreEvents;
import dev.thomasglasser.mineraculous.data.MineraculousDataGenerators;
import dev.thomasglasser.mineraculous.platform.NeoForgeDataHelper;
import dev.thomasglasser.mineraculous.world.entity.MineraculousNeoForgeEntityEvents;
import dev.thomasglasser.mineraculous.world.level.storage.loot.modifier.MineraculousLootModifiers;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Mineraculous.MOD_ID)
public class MineraculousNeoForge
{
    public MineraculousNeoForge(IEventBus bus) {
        Mineraculous.init();

        NeoForgeDataHelper.ATTACHMENT_TYPES.register(bus);
        MineraculousLootModifiers.LOOT_MODIFIERS.register(bus);

        bus.addListener(MineraculousDataGenerators::onGatherData);

        addModListeners(bus);

        NeoForge.EVENT_BUS.addListener(MineraculousNeoForgeEntityEvents::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(MineraculousNeoForgeEntityEvents::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(MineraculousNeoForgeEntityEvents::onBlockInteract);
        NeoForge.EVENT_BUS.addListener(MineraculousNeoForgeEntityEvents::onAttackEntity);
        NeoForge.EVENT_BUS.addListener(MineraculousNeoForgeEntityEvents::onLivingAttack);
        NeoForge.EVENT_BUS.addListener(MineraculousNeoForgeEntityEvents::onBlockLeftClick);
        NeoForge.EVENT_BUS.addListener(MineraculousNeoForgeEntityEvents::onEffectRemoved);
        NeoForge.EVENT_BUS.addListener(MineraculousNeoForgeCommandEvents::onCommandsRegister);
        NeoForge.EVENT_BUS.addListener(MineraculousNeoForgeEntityEvents::onLivingHeal);

        if (TommyLibServices.PLATFORM.isClientSide())
        {
            bus.addListener(MineraculousNeoForgeClientEvents::onRegisterAdditionalModels);
            bus.addListener(MineraculousNeoForgeClientEvents::onRegisterRenderer);
            bus.addListener(MineraculousNeoForgeClientEvents::onFMLClientSetup);
            bus.addListener(MineraculousNeoForgeClientEvents::onRegisterParticleProviders);

            NeoForge.EVENT_BUS.addListener(MineraculousNeoForgeClientEvents::onEntityJoinLevel);
        }
    }

    private void addModListeners(IEventBus bus)
    {
        bus.addListener(MineraculousNeoForgeEntityEvents::onEntityAttributeCreation);
        bus.addListener(MineraculousNeoForgeCoreEvents::onRegisterPackets);
        bus.addListener(MineraculousNeoForgeCoreEvents::onFMLCommonSetup);
    }
}