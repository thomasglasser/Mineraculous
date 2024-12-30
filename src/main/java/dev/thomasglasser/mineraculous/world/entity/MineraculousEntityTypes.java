package dev.thomasglasser.mineraculous.world.entity;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownCatStaff;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class MineraculousEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, Mineraculous.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<Kwami>> KWAMI = register("kwami",
            EntityType.Builder.of(Kwami::new, MobCategory.CREATURE)
                    .sized(0.6F, 0.8F));
    public static final DeferredHolder<EntityType<?>, EntityType<Kamiko>> KAMIKO = register("kamiko",
            EntityType.Builder.of(Kamiko::new, MobCategory.MISC)
                    .clientTrackingRange(Integer.MAX_VALUE / 16)
                    .sized(0.3F, 0.3F));

    // Projectiles
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownLadybugYoyo>> THROWN_LADYBUG_YOYO = register("thrown_ladybug_yoyo",
            EntityType.Builder.<ThrownLadybugYoyo>of(ThrownLadybugYoyo::new, MobCategory.MISC)
                    .clientTrackingRange(Integer.MAX_VALUE / 16)
                    .sized(0.5F, 0.5F));
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownCatStaff>> THROWN_CAT_STAFF = register("thrown_cat_staff",
            EntityType.Builder.<ThrownCatStaff>of(ThrownCatStaff::new, MobCategory.MISC)
                    .clientTrackingRange(Integer.MAX_VALUE / 16)
                    .sized(0.5F, 0.5F));

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String name, EntityType.Builder<T> builder) {
        return ENTITY_TYPES.register(name, () -> builder.build(name));
    }

    private static ResourceKey<EntityType<?>> key(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Mineraculous.modLoc(name));
    }

    public static Map<EntityType<? extends LivingEntity>, AttributeSupplier> getAllAttributes() {
        Map<EntityType<? extends LivingEntity>, AttributeSupplier> map = new HashMap<>();

        map.put(KWAMI.get(), Kwami.createAttributes().build());
        map.put(KAMIKO.get(), Kamiko.createAttributes().build());

        return map;
    }

    public static void init() {}
}
