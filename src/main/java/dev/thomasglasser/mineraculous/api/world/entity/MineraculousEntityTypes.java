package dev.thomasglasser.mineraculous.api.world.entity;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import dev.thomasglasser.mineraculous.impl.world.entity.LuckyCharmItemSpawner;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownButterflyCane;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownCatStaff;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousEntityTypes {
    private static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(Mineraculous.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<Kwami>> KWAMI = ENTITY_TYPES.register("kwami", Kwami::new, MobCategory.CREATURE, builder -> builder
            .sized(0.3F, 0.4F));
    public static final DeferredHolder<EntityType<?>, EntityType<Kamiko>> KAMIKO = ENTITY_TYPES.register("kamiko", Kamiko::new, MobCategory.CREATURE, builder -> builder
            .clientTrackingRange(Integer.MAX_VALUE / 16)
            .sized(1.0F, 1.0F));
    public static final DeferredHolder<EntityType<?>, EntityType<LuckyCharmItemSpawner>> LUCKY_CHARM_ITEM_SPAWNER = ENTITY_TYPES.register("lucky_charm_item_spawner", LuckyCharmItemSpawner::new, MobCategory.MISC, builder -> builder
            .sized(0.25F, 0.25F));

    // Projectiles
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownLadybugYoyo>> THROWN_LADYBUG_YOYO = registerThrown("thrown_ladybug_yoyo", ThrownLadybugYoyo::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownCatStaff>> THROWN_CAT_STAFF = registerThrown("thrown_cat_staff", ThrownCatStaff::new);
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownButterflyCane>> THROWN_BUTTERFLY_CANE = registerThrown("thrown_butterfly_cane", ThrownButterflyCane::new);

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> registerThrown(String name, EntityType.EntityFactory<T> factory) {
        return ENTITY_TYPES.register(name, factory, MobCategory.MISC, builder -> builder
                .clientTrackingRange(Integer.MAX_VALUE / 16)
                .sized(0.5f, 0.5f));
    }

    @ApiStatus.Internal
    public static void init() {}
}
