package dev.thomasglasser.mineraculous.core;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousBuiltInRegistries;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.network.MineraculousPayloads;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.Ability;
import dev.thomasglasser.tommylib.api.network.NeoForgeNetworkUtils;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

public class MineraculousCoreEvents {
    public static void onRegisterPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Mineraculous.MOD_ID);
        MineraculousPayloads.PAYLOADS.forEach((info) -> NeoForgeNetworkUtils.register(registrar, info));
    }

    public static void onNewDataPackRegistry(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(MineraculousRegistries.MIRACULOUS, Miraculous.CODEC, Miraculous.CODEC);
        event.dataPackRegistry(MineraculousRegistries.ABILITY, Ability.DIRECT_CODEC, Ability.DIRECT_CODEC);
    }

    public static void onNewRegistry(NewRegistryEvent event) {
        event.register(MineraculousBuiltInRegistries.ABILITY_SERIALIZER);
    }
}
