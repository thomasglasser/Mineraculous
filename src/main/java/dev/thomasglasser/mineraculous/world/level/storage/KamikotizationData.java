package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

public record KamikotizationData(ResourceKey<Kamikotization> kamikotization, ItemStack kamikotizedStack, boolean mainPowerActive, String name, String look) {

    public static final Codec<KamikotizationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).fieldOf("kamikotization").forGetter(KamikotizationData::kamikotization),
            ItemStack.OPTIONAL_CODEC.fieldOf("kamikotized_stack").forGetter(KamikotizationData::kamikotizedStack),
            Codec.BOOL.fieldOf("main_power_active").forGetter(KamikotizationData::mainPowerActive),
            Codec.STRING.optionalFieldOf("name", "").forGetter(KamikotizationData::name),
            Codec.STRING.optionalFieldOf("look", "").forGetter(KamikotizationData::look)).apply(instance, KamikotizationData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, KamikotizationData> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(MineraculousRegistries.KAMIKOTIZATION), KamikotizationData::kamikotization,
            ItemStack.OPTIONAL_STREAM_CODEC, KamikotizationData::kamikotizedStack,
            ByteBufCodecs.BOOL, KamikotizationData::mainPowerActive,
            ByteBufCodecs.STRING_UTF8, KamikotizationData::name,
            ByteBufCodecs.STRING_UTF8, KamikotizationData::look,
            KamikotizationData::new);
    public KamikotizationData() {
        this(null, ItemStack.EMPTY, false, "", "");
    }
}
