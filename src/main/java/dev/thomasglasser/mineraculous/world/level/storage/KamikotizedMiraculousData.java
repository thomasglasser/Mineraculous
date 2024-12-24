package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.network.ClientboundSyncKamikotizedMiraculousDataPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;

public record KamikotizedMiraculousData(Optional<ResourceKey<Miraculous>> miraculous, MiraculousData data) {
    public static final Codec<KamikotizedMiraculousData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(MineraculousRegistries.MIRACULOUS).optionalFieldOf("miraculous").forGetter(KamikotizedMiraculousData::miraculous),
            MiraculousData.CODEC.fieldOf("miraculous_data").forGetter(KamikotizedMiraculousData::data)).apply(instance, KamikotizedMiraculousData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KamikotizedMiraculousData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ResourceKey.streamCodec(MineraculousRegistries.MIRACULOUS)), KamikotizedMiraculousData::miraculous,
            MiraculousData.STREAM_CODEC, KamikotizedMiraculousData::data,
            KamikotizedMiraculousData::new);

    public KamikotizedMiraculousData() {
        this(Optional.empty(), new MiraculousData());
    }

    public void save(LivingEntity livingEntity, boolean syncToClient) {
        livingEntity.setData(MineraculousAttachmentTypes.KAMIKOTIZED_MIRACULOUS, this);
        if (syncToClient) {
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncKamikotizedMiraculousDataPayload(this, livingEntity.getId()), livingEntity.level().getServer());
        }
    }
}
