package dev.thomasglasser.mineraculous.world.level.storage;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.network.ClientboundSyncMiraculousDataSetPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.tommylib.api.network.NetworkUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MiraculousDataSet
{
	public static final MapCodec<Map<MiraculousType, MiraculousData>> MAP_CODEC = Codec.simpleMap(MiraculousType.CODEC, MiraculousData.CODEC, StringRepresentable.keys(MiraculousType.values())).xmap(map -> map.isEmpty() ? new EnumMap<>(MiraculousType.class) : new EnumMap<>(map), Function.identity());
	public static final Codec<MiraculousDataSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			MAP_CODEC.fieldOf("map").forGetter(set -> set.map)
	).apply(instance, MiraculousDataSet::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousDataSet> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.map(
					Maps::newHashMapWithExpectedSize,
					NetworkUtils.enumCodec(MiraculousType.class),
					MiraculousData.STREAM_CODEC
			),
			set -> set.map,
			MiraculousDataSet::new
	);

	private final Map<MiraculousType, MiraculousData> map;

	public MiraculousDataSet()
	{
		this.map = new EnumMap<>(MiraculousType.class);
	}

	public MiraculousDataSet(Map<MiraculousType, MiraculousData> map)
	{
		this.map = map;
	}

	public MiraculousData get(MiraculousType key)
	{
		return map.getOrDefault(key, new MiraculousData());
	}

	public MiraculousData put(LivingEntity entity, MiraculousType key, MiraculousData value, boolean syncToClient)
	{
		MiraculousData data = map.put(key, value);
		save(entity, syncToClient);
		return data;
	}

	public List<MiraculousType> keySet()
	{
		return List.copyOf(map.keySet());
	}

	public List<MiraculousData> values()
	{
		return List.copyOf(map.values());
	}

	public List<MiraculousType> getTransformed()
	{
		return map.entrySet().stream().filter(entry -> entry.getValue().transformed()).map(Map.Entry::getKey).toList();
	}

	public boolean isTransformed()
	{
		return map.values().stream().anyMatch(MiraculousData::transformed);
	}

	public void save(LivingEntity entity, boolean syncToClient)
	{
		entity.setData(MineraculousAttachmentTypes.MIRACULOUS.get(), this);
		if (syncToClient) TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncMiraculousDataSetPayload(this, entity.getId()), entity.level().getServer());
	}
}