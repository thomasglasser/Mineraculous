package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MineraculousEntityDataSerializers
{
	public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, Mineraculous.MOD_ID);

	public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Boolean>> CHARGED = ENTITY_DATA_SERIALIZERS.register("charged", () -> Kwami.CHARGED);

	public static void init() {}
}
