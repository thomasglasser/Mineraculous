package dev.thomasglasser.mineraculous.core.component;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

public class MineraculousDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Mineraculous.MOD_ID);

    // Shared
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> POWERED = register("powered", Codec.unit(Unit.INSTANCE), StreamCodec.unit(Unit.INSTANCE), false);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<KwamiData>> KWAMI_DATA = register("kwami_data", KwamiData.CODEC, null, true);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TOOL_ID = register("tool_id", Codec.INT, ByteBufCodecs.INT, false);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> HIDE_ENCHANTMENTS = register("hide_enchantments", Codec.unit(Unit.INSTANCE), StreamCodec.unit(Unit.INSTANCE), false);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceKey<Miraculous>>> MIRACULOUS = register("miraculous", ResourceKey.codec(MineraculousRegistries.MIRACULOUS), ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), false);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceKey<Kamikotization>>> KAMIKOTIZATION = register("kamikotization", ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION), ResourceKey.streamCodec(MineraculousRegistries.KAMIKOTIZATION), false);

    // Miraculous Item
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TRANSFORMATION_FRAMES = register("transformation_frames", Codec.INT, null, false);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> DETRANSFORMATION_FRAMES = register("detransformation_frames", Codec.INT, null, false);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> REMAINING_TICKS = register("remaining_ticks", Codec.INT, ByteBufCodecs.INT, false);

    // Miraculous Tools
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> ACTIVE = register("active", Codec.unit(Unit.INSTANCE), StreamCodec.unit(Unit.INSTANCE), false);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<CompoundTag>>> STORED_ENTITIES = register("stored_entity", Codec.list(CompoundTag.CODEC), ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list()), false);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LadybugYoyoItem.Ability>> LADYBUG_YOYO_ABILITY = register("ladybug_yoyo_ability", LadybugYoyoItem.Ability.CODEC, LadybugYoyoItem.Ability.STREAM_CODEC, true);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CatStaffItem.Ability>> CAT_STAFF_ABILITY = register("cat_staff_ability", CatStaffItem.Ability.CODEC, CatStaffItem.Ability.STREAM_CODEC, true);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ButterflyCaneItem.Ability>> BUTTERFLY_CANE_ABILITY = register("butterfly_cane_ability", ButterflyCaneItem.Ability.CODEC, ButterflyCaneItem.Ability.STREAM_CODEC, true);

    // Kamikotization
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<KamikoData>> KAMIKO_DATA = register("kamiko_data", KamikoData.CODEC, null, true);

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, @Nullable Codec<T> diskCodec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> networkCodec, boolean cache) {
        Supplier<DataComponentType<T>> component = () -> {
            DataComponentType.Builder<T> builder = DataComponentType.builder();
            if (diskCodec != null) {
                builder.persistent(diskCodec);
            }
            if (networkCodec != null) {
                builder.networkSynchronized(networkCodec);
            }
            if (cache) {
                builder.cacheEncoding();
            }
            return builder.build();
        };
        return DATA_COMPONENTS.register(name, component);
    }

    public static void init() {}
}
