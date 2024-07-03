package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.kwami.Plagg;
import dev.thomasglasser.mineraculous.world.entity.kwami.Tikki;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownCatStaff;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class MineraculousEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Mineraculous.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<Tikki>> TIKKI = register("tikki",
            EntityType.Builder.of(Tikki::new, MobCategory.CREATURE)
                    // TODO: Figure out size of model
                    .sized(0.6F, 0.9F));
    public static final DeferredHolder<EntityType<?>, EntityType<Plagg>> PLAGG = register("plagg",
            EntityType.Builder.of(Plagg::new, MobCategory.CREATURE)
                    // TODO: Figure out size of model
                    .sized(0.6F, 0.9F));
    public static final DeferredHolder<EntityType<?>, EntityType<Kamiko>> KAMIKO = register("kamiko",
            EntityType.Builder.of(Kamiko::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F));

    // Projectiles
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownCatStaff>> THROWN_CAT_STAFF = register("thrown_cat_staff",
            EntityType.Builder.<ThrownCatStaff>of(ThrownCatStaff::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F));

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String name, EntityType.Builder<T> builder) {
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
