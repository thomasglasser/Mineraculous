package dev.thomasglasser.miraculous.world.entity;

import dev.thomasglasser.miraculous.Miraculous;
import dev.thomasglasser.miraculous.world.entity.kwami.DestructionKwami;
import dev.thomasglasser.miraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.tommylib.api.registration.RegistrationProvider;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.HashMap;
import java.util.Map;

public class MiraculousEntityTypes
{
	public static final RegistrationProvider<EntityType<?>> ENTITY_TYPES = RegistrationProvider.get(Registries.ENTITY_TYPE, Miraculous.MOD_ID);

	public static final RegistryObject<EntityType<DestructionKwami>> PLAGG = register("plagg",
			EntityType.Builder.of(DestructionKwami::new, MobCategory.CREATURE)
			// TODO: Figure out size of model
			.sized(0.6F, 0.9F));


	private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> builder)
	{
		return ENTITY_TYPES.register(name, () -> builder.build(name));
	}

	public static Map<EntityType<? extends LivingEntity>, AttributeSupplier> getAllAttributes() {
		Map<EntityType<? extends LivingEntity>, AttributeSupplier> map = new HashMap<>();

		map.put(PLAGG.get(), Kwami.createKwamiAttributes().build());

		return map;
	}

	public static void init() {}
}
