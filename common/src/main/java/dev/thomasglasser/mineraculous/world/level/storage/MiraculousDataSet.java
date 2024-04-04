package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MiraculousDataSet
{
	public static final MapCodec<EnumMap<MiraculousType, MiraculousData>> MAP_CODEC = Codec.simpleMap(MiraculousType.CODEC, MiraculousData.CODEC, StringRepresentable.keys(MiraculousType.values())).xmap(map -> map.isEmpty() ? new EnumMap<>(MiraculousType.class) : new EnumMap<>(map), Function.identity());
	public static final Codec<MiraculousDataSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			MAP_CODEC.fieldOf("map").forGetter(set -> set.map)
	).apply(instance, MiraculousDataSet::new));

	private final EnumMap<MiraculousType, MiraculousData> map;

	public MiraculousDataSet()
	{
		this.map = new EnumMap<>(MiraculousType.class);
	}

	public MiraculousDataSet(EnumMap<MiraculousType, MiraculousData> map)
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
		Services.DATA.setMiraculousDataSet(entity, this, syncToClient);
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
}
