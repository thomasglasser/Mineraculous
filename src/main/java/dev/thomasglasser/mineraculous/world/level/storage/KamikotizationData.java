package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

public record KamikotizationData(ResourceKey<Kamikotization> kamikotization, ItemStack kamikotizedStack, Either<Integer, CuriosData> slotInfo, String name) {

    public static final Codec<KamikotizationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).fieldOf("kamikotization").forGetter(KamikotizationData::kamikotization),
            ItemStack.OPTIONAL_CODEC.fieldOf("kamikotized_stack").forGetter(KamikotizationData::kamikotizedStack),
            Codec.either(Codec.INT, CuriosData.CODEC).fieldOf("slot_info").forGetter(KamikotizationData::slotInfo),
            Codec.STRING.optionalFieldOf("name", "").forGetter(KamikotizationData::name)).apply(instance, KamikotizationData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KamikotizationData> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.KAMIKOTIZATION), KamikotizationData::kamikotization,
            ItemStack.OPTIONAL_STREAM_CODEC, KamikotizationData::kamikotizedStack,
            ByteBufCodecs.either(ByteBufCodecs.INT, CuriosData.STREAM_CODEC), KamikotizationData::slotInfo,
            ByteBufCodecs.STRING_UTF8, KamikotizationData::name,
            KamikotizationData::new);
    public KamikotizationData() {
        this(null, ItemStack.EMPTY, Either.left(-1), "");
    }
}
