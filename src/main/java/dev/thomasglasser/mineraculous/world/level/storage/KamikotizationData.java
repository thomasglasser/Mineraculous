package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.network.ClientboundSyncKamikotizationDataPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.tommylib.api.network.NetworkUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;

public record KamikotizationData(ResourceKey<Kamikotization> kamikotization, int stackCount, Either<Integer, CuriosData> slotInfo, KamikoData kamikoData, boolean mainPowerActive, Either<Integer, Integer> transformationFrames, String name) {

    public static final Codec<KamikotizationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).fieldOf("kamikotization").forGetter(KamikotizationData::kamikotization),
            Codec.INT.fieldOf("stack_count").forGetter(KamikotizationData::stackCount),
            Codec.either(Codec.INT, CuriosData.CODEC).fieldOf("slot_info").forGetter(KamikotizationData::slotInfo),
            KamikoData.CODEC.fieldOf("kamiko_data").forGetter(KamikotizationData::kamikoData),
            Codec.BOOL.fieldOf("main_power_active").forGetter(KamikotizationData::mainPowerActive),
            Codec.either(Codec.INT, Codec.INT).fieldOf("transformation_frames").forGetter(KamikotizationData::transformationFrames),
            Codec.STRING.optionalFieldOf("name", "").forGetter(KamikotizationData::name)).apply(instance, KamikotizationData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KamikotizationData> STREAM_CODEC = NetworkUtils.composite(
            ResourceKey.streamCodec(MineraculousRegistries.KAMIKOTIZATION), KamikotizationData::kamikotization,
            ByteBufCodecs.INT, KamikotizationData::stackCount,
            ByteBufCodecs.either(ByteBufCodecs.INT, CuriosData.STREAM_CODEC), KamikotizationData::slotInfo,
            KamikoData.STREAM_CODEC, KamikotizationData::kamikoData,
            ByteBufCodecs.BOOL, KamikotizationData::mainPowerActive,
            ByteBufCodecs.either(ByteBufCodecs.INT, ByteBufCodecs.INT), KamikotizationData::transformationFrames,
            ByteBufCodecs.STRING_UTF8, KamikotizationData::name,
            KamikotizationData::new);
    public KamikotizationData decrementStackCount() {
        return new KamikotizationData(kamikotization, stackCount - 1, slotInfo, kamikoData, mainPowerActive, transformationFrames, name);
    }

    public KamikotizationData withMainPowerActive(boolean active) {
        return new KamikotizationData(kamikotization, stackCount, slotInfo, kamikoData, active, transformationFrames, name);
    }

    public KamikotizationData withTransformationFrames(int frames) {
        return new KamikotizationData(kamikotization, stackCount, slotInfo, kamikoData, mainPowerActive, Either.left(frames), name);
    }

    public KamikotizationData withDetransformationFrames(int frames) {
        return new KamikotizationData(kamikotization, stackCount, slotInfo, kamikoData, mainPowerActive, Either.right(frames), name);
    }

    public void save(LivingEntity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.KAMIKOTIZATION, Optional.of(this));
        if (entity.getData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION).isPresent())
            entity.setData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION, Optional.empty());
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncKamikotizationDataPayload(this, entity.getId()), entity.getServer());
    }

    public static void remove(LivingEntity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.OLD_KAMIKOTIZATION, entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).map(KamikotizationData::kamikotization));
        entity.setData(MineraculousAttachmentTypes.KAMIKOTIZATION, Optional.empty());
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncKamikotizationDataPayload(entity.getId()), entity.getServer());
    }
}
