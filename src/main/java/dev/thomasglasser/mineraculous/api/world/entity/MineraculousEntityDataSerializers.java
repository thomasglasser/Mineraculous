package dev.thomasglasser.mineraculous.api.world.entity;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.look.LookData;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.impl.world.kamikotization.MinionKamikotizationData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.MiraculousLadybugTargetData;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousEntityDataSerializers {
    private static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, MineraculousConstants.MOD_ID);

    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<UUID>> UUID = ENTITY_DATA_SERIALIZERS.register("uuid", () -> EntityDataSerializer.forValueType(UUIDUtil.STREAM_CODEC));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Holder<Miraculous>>> MIRACULOUS = ENTITY_DATA_SERIALIZERS.register("miraculous", () -> EntityDataSerializer.forValueType(Miraculous.STREAM_CODEC));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Optional<LadybugYoyoItem.Mode>>> OPTIONAL_LADYBUG_YOYO_MODE = ENTITY_DATA_SERIALIZERS.register("optional_ladybug_yoyo_mode", () -> EntityDataSerializer.forValueType(ByteBufCodecs.optional(LadybugYoyoItem.Mode.STREAM_CODEC)));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Optional<ResourceLocation>>> OPTIONAL_RESOURCE_LOCATION = ENTITY_DATA_SERIALIZERS.register("optional_resource_location", () -> EntityDataSerializer.forValueType(TommyLibExtraStreamCodecs.OPTIONAL_RESOURCE_LOCATION));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<MiraculousLadybugTargetData>> MIRACULOUS_LADYBUG_TARGET_DATA = ENTITY_DATA_SERIALIZERS.register("miraculous_ladybug_target_data", () -> EntityDataSerializer.forValueType(MiraculousLadybugTargetData.STREAM_CODEC));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Optional<Holder<Kamikotization>>>> OPTIONAL_KAMIKOTIZATION = ENTITY_DATA_SERIALIZERS.register("optional_kamikotization", () -> EntityDataSerializer.forValueType(ByteBufCodecs.optional(Kamikotization.STREAM_CODEC)));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<Optional<MinionKamikotizationData>>> MINION_KAMIKOTIZATION_DATA = ENTITY_DATA_SERIALIZERS.register("minion_kamikotization_data", () -> EntityDataSerializer.forValueType(ByteBufCodecs.optional(MinionKamikotizationData.STREAM_CODEC)));
    public static final DeferredHolder<EntityDataSerializer<?>, EntityDataSerializer<LookData>> LOOK_DATA = ENTITY_DATA_SERIALIZERS.register("look_data", () -> EntityDataSerializer.forValueType(LookData.STREAM_CODEC));

    @ApiStatus.Internal
    public static void init() {}
}
