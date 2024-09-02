package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MineraculousEntityDataSerializers {
    public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, Mineraculous.MOD_ID);

    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<ResourceKey<Miraculous>>> MIRACULOUS = ENTITY_DATA_SERIALIZERS.register("miraculous", () -> EntityDataSerializer.forValueType(ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS)));

    public static void init() {}
}
