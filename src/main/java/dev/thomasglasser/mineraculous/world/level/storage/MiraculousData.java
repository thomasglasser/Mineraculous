package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.tommylib.api.network.NetworkUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record MiraculousData(boolean transformed, ItemStack miraculousItem, CuriosData curiosData, ItemStack tool, int powerLevel, boolean mainPowerActivated, boolean mainPowerActive, String name)
{
	public static final Codec<MiraculousData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.BOOL.fieldOf("transformed").forGetter(MiraculousData::transformed),
			ItemStack.OPTIONAL_CODEC.fieldOf("miraculous_item").forGetter(MiraculousData::miraculousItem),
			CuriosData.CODEC.fieldOf("curios_data").forGetter(MiraculousData::curiosData),
			ItemStack.OPTIONAL_CODEC.fieldOf("tool").forGetter(MiraculousData::tool),
			Codec.INT.fieldOf("power_level").forGetter(MiraculousData::powerLevel),
			Codec.BOOL.fieldOf("main_power_activated").forGetter(MiraculousData::mainPowerActivated),
			Codec.BOOL.fieldOf("main_power_active").forGetter(MiraculousData::mainPowerActive),
			Codec.STRING.optionalFieldOf("name", "").forGetter(MiraculousData::name)
	).apply(instance, MiraculousData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousData> STREAM_CODEC = NetworkUtils.composite(
			ByteBufCodecs.BOOL, MiraculousData::transformed,
			ItemStack.OPTIONAL_STREAM_CODEC, MiraculousData::miraculousItem,
			CuriosData.STREAM_CODEC, MiraculousData::curiosData,
			ItemStack.OPTIONAL_STREAM_CODEC, MiraculousData::tool,
			ByteBufCodecs.INT, MiraculousData::powerLevel,
			ByteBufCodecs.BOOL, MiraculousData::mainPowerActivated,
			ByteBufCodecs.BOOL, MiraculousData::mainPowerActive,
			ByteBufCodecs.STRING_UTF8, MiraculousData::name,
			MiraculousData::new
	);

	public static final String NAME_NOT_SET = "miraculous_data.name.not_set";

	public MiraculousData()
	{
		this(false, ItemStack.EMPTY, new CuriosData(), ItemStack.EMPTY, 0, false, false, "");
	}
}
