package dev.thomasglasser.mineraculous.core.component;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.tommylib.api.registration.RegistrationProvider;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class MineraculousDataComponents
{
	public static final RegistrationProvider<DataComponentType<?>> DATA_COMPONENTS = RegistrationProvider.get(Registries.DATA_COMPONENT_TYPE, Mineraculous.MOD_ID);

	// Shared
	public static final RegistryObject<DataComponentType<Boolean>> POWERED = register("powered", Codec.BOOL, ByteBufCodecs.BOOL, false);
	public static final RegistryObject<DataComponentType<KwamiData>> KWAMI_DATA = register("kwami_data", KwamiData.CODEC, null, true);
	public static final RegistryObject<DataComponentType<Unit>> HIDE_ENCHANTMENTS = register("hide_enchantments", Codec.unit(Unit.INSTANCE), StreamCodec.unit(Unit.INSTANCE), false);

	// Miraculous Item
	public static final RegistryObject<DataComponentType<Boolean>> RECALLED = register("recalled", Codec.BOOL, null, false);
	public static final RegistryObject<DataComponentType<Integer>> TRANSFORMING_ANIM_TICKS = register("transforming_anim_ticks", Codec.INT, null, false);
	public static final RegistryObject<DataComponentType<Integer>> DETRANSFORMING_ANIM_TICKS = register("detransforming_anim_ticks", Codec.INT, null, false);
	public static final RegistryObject<DataComponentType<Integer>> REMAINING_TICKS = register("remaining_ticks", Codec.INT, ByteBufCodecs.INT, false);

	// Cat Staff
	public static final RegistryObject<DataComponentType<Boolean>> TRAVELING = register("traveling", Codec.BOOL, ByteBufCodecs.BOOL, false);

	private static <T> RegistryObject<DataComponentType<T>> register(String name, @Nullable Codec<T> diskCodec, @Nullable StreamCodec<? super RegistryFriendlyByteBuf, T> networkCodec, boolean cache)
	{
		Supplier<DataComponentType<T>> component = () ->
		{
			DataComponentType.Builder<T> builder = DataComponentType.builder();
			if (diskCodec != null)
			{
				builder.persistent(diskCodec);
			}
			if (networkCodec != null)
			{
				builder.networkSynchronized(networkCodec);
			}
			return builder.cacheEncoding().build();
		};
		return DATA_COMPONENTS.register(name, component);
	}

	public static void init() {}
}
