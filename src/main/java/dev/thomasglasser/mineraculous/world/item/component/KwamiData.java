package dev.thomasglasser.mineraculous.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.UUID;

public record KwamiData(UUID uuid, boolean charged)
{
	public static final Codec<KwamiData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("uuid").forGetter(data -> data.uuid.toString()),
			Codec.BOOL.fieldOf("charged").forGetter(KwamiData::charged)
	).apply(instance, (uuid, charged) -> new KwamiData(UUID.fromString(uuid), charged)));
}
