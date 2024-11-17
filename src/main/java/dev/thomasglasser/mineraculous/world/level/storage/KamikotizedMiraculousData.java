package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;

public record KamikotizedMiraculousData(ResourceKey<Miraculous> miraculous, MiraculousData data) {

    public static final Codec<KamikotizedMiraculousData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(MineraculousRegistries.MIRACULOUS).fieldOf("miraculous").forGetter(KamikotizedMiraculousData::miraculous),
            MiraculousData.CODEC.fieldOf("miraculous_data").forGetter(KamikotizedMiraculousData::data)).apply(instance, KamikotizedMiraculousData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KamikotizedMiraculousData> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS), KamikotizedMiraculousData::miraculous,
            MiraculousData.STREAM_CODEC, KamikotizedMiraculousData::data,
            KamikotizedMiraculousData::new);
    public KamikotizedMiraculousData() {
        this(ResourceKey.create(MineraculousRegistries.MIRACULOUS, Mineraculous.modLoc("")), new MiraculousData());
    }

    public void save(LivingEntity livingEntity, boolean syncToClient) {
        livingEntity.setData(MineraculousAttachmentTypes.KAMIKOTIZED_MIRACULOUS, this);
        if (syncToClient) {

        }
    }
}
