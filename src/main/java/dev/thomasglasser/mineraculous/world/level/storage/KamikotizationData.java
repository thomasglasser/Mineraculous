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
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record KamikotizationData(ResourceKey<Kamikotization> kamikotization, ItemStack kamikotizedStack, Either<Integer, CuriosData> slotInfo, KamikoData kamikoData, String name) {

    public static final Codec<KamikotizationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).fieldOf("kamikotization").forGetter(KamikotizationData::kamikotization),
            ItemStack.OPTIONAL_CODEC.fieldOf("kamikotized_stack").forGetter(KamikotizationData::kamikotizedStack),
            Codec.either(Codec.INT, CuriosData.CODEC).fieldOf("slot_info").forGetter(KamikotizationData::slotInfo),
            KamikoData.CODEC.fieldOf("kamiko_data").forGetter(KamikotizationData::kamikoData),
            Codec.STRING.optionalFieldOf("name", "").forGetter(KamikotizationData::name)).apply(instance, KamikotizationData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KamikotizationData> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.KAMIKOTIZATION), KamikotizationData::kamikotization,
            ItemStack.OPTIONAL_STREAM_CODEC, KamikotizationData::kamikotizedStack,
            ByteBufCodecs.either(ByteBufCodecs.INT, CuriosData.STREAM_CODEC), KamikotizationData::slotInfo,
            KamikoData.STREAM_CODEC, KamikotizationData::kamikoData,
            ByteBufCodecs.STRING_UTF8, KamikotizationData::name,
            KamikotizationData::new);
    public void save(LivingEntity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.KAMIKOTIZATION, Optional.of(this));
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncKamikotizationDataPayload(this, entity.getId()), entity.getServer());
    }

    public static void remove(LivingEntity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.KAMIKOTIZATION, Optional.empty());
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncKamikotizationDataPayload(entity.getId()), entity.getServer());
    }
}
