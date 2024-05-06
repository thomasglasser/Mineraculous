package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.kwami.Plagg;
import dev.thomasglasser.mineraculous.world.entity.kwami.Tikki;
import dev.thomasglasser.tommylib.api.registration.RegistrationProvider;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ambient.Bat;

import java.util.HashMap;
import java.util.Map;

public class MineraculousEntityTypes
{
	public static final RegistrationProvider<EntityType<?>> ENTITY_TYPES = RegistrationProvider.get(Registries.ENTITY_TYPE, Mineraculous.MOD_ID);

	public static final RegistryObject<EntityType<Tikki>> TIKKI = register("tikki",
			EntityType.Builder.of(Tikki::new, MobCategory.CREATURE)
			// TODO: Figure out size of model
			.sized(0.6F, 0.9F));
	public static final RegistryObject<EntityType<Plagg>> PLAGG = register("plagg",
			EntityType.Builder.of(Plagg::new, MobCategory.CREATURE)
			// TODO: Figure out size of model
			.sized(0.6F, 0.9F));
	public static final RegistryObject<EntityType<Kamiko>> KAMIKO = register("kamiko",
			EntityType.Builder.of(Kamiko::new, MobCategory.MISC)
					.sized(0.2F,0.2F)
	);

	private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, EntityType.Builder<T> builder)
	{
		return ENTITY_TYPES.register(name, () -> builder.build(name));
	}

	public static Map<EntityType<? extends LivingEntity>, AttributeSupplier> getAllAttributes() {
		Map<EntityType<? extends LivingEntity>, AttributeSupplier> map = new HashMap<>();

		// Kwamis
		map.put(TIKKI.get(), Tikki.createKwamiAttributes().build());
		map.put(PLAGG.get(), Plagg.createKwamiAttributes().build());

		map.put(KAMIKO.get(), Kamiko.createAttributes().build());

		return map;
	}

	public static void init() {}
}
