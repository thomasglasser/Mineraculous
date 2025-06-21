package dev.thomasglasser.mineraculous.api.world.entity;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousEntityDataSerializers {
    private static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, Mineraculous.MOD_ID);

    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<ResourceKey<Miraculous>>> MIRACULOUS = ENTITY_DATA_SERIALIZERS.register("miraculous", () -> EntityDataSerializer.forValueType(ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS)));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Optional<LadybugYoyoItem.Ability>>> OPTIONAL_LADYBUG_YOYO_ABILITY = ENTITY_DATA_SERIALIZERS.register("optional_ladybug_yoyo_ability", () -> EntityDataSerializer.forValueType(ByteBufCodecs.optional(LadybugYoyoItem.Ability.STREAM_CODEC)));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Optional<ResourceLocation>>> OPTIONAL_RESOURCE_LOCATION = ENTITY_DATA_SERIALIZERS.register("optional_resource_location", () -> EntityDataSerializer.forValueType(TommyLibExtraStreamCodecs.OPTIONAL_RESOURCE_LOCATION));

    @ApiStatus.Internal
    public static void init() {}
}
