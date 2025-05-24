package dev.thomasglasser.mineraculous.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

public record KwamiData(UUID uuid, int id, boolean charged) {
    public static final Codec<KwamiData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(KwamiData::uuid),
            Codec.INT.fieldOf("id").forGetter(KwamiData::id),
            Codec.BOOL.fieldOf("charged").forGetter(KwamiData::charged)).apply(instance, KwamiData::new));
}
