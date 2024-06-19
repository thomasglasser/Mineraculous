package dev.thomasglasser.mineraculous.core;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.network.MineraculousPayloads;
import dev.thomasglasser.tommylib.api.network.NeoForgeNetworkUtils;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class MineraculousCoreEvents
{
	public static void onRegisterPackets(RegisterPayloadHandlersEvent event)
	{
		PayloadRegistrar registrar = event.registrar(Mineraculous.MOD_ID);
		MineraculousPayloads.PAYLOADS.forEach((info) -> NeoForgeNetworkUtils.register(registrar, info));
	}
}
