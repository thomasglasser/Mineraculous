package dev.thomasglasser.mineraculous.api.world.entity.animal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.ApiStatus;

/// Stores a texture to use when a {@link dev.thomasglasser.mineraculous.impl.world.entity.animal.Butterfly} is spawned in the provided biomes.
public final class ButterflyVariant {
    public static final Codec<ButterflyVariant> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("texture").forGetter(variant -> variant.texture),
            RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biomes", HolderSet.empty()).forGetter(ButterflyVariant::biomes))
            .apply(instance, ButterflyVariant::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ButterflyVariant> DIRECT_STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            ButterflyVariant::texture,
            ByteBufCodecs.holderSet(Registries.BIOME),
            ButterflyVariant::biomes,
            ButterflyVariant::new);
    public static final Codec<Holder<ButterflyVariant>> CODEC = RegistryFileCodec.create(MineraculousRegistries.BUTTERFLY_VARIANT, DIRECT_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<ButterflyVariant>> STREAM_CODEC = ByteBufCodecs.holder(
            MineraculousRegistries.BUTTERFLY_VARIANT, DIRECT_STREAM_CODEC);
    private final ResourceLocation texture;
    private final ResourceLocation textureFull;
    private final HolderSet<Biome> biomes;

    public ButterflyVariant(ResourceLocation texture, HolderSet<Biome> biomes) {
        this.texture = texture;
        this.textureFull = fullTextureId(texture);
        this.biomes = biomes;
    }

    private static ResourceLocation fullTextureId(ResourceLocation texture) {
        return texture.withPath(path -> "textures/" + path + ".png");
    }

    @ApiStatus.Internal
    public ResourceLocation texture() {
        return this.textureFull;
    }

    @ApiStatus.Internal
    public HolderSet<Biome> biomes() {
        return this.biomes;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else {
            return other instanceof ButterflyVariant butterflyVariant
                    && Objects.equals(this.texture, butterflyVariant.texture)
                    && Objects.equals(this.biomes, butterflyVariant.biomes);
        }
    }

    @Override
    public int hashCode() {
        int i = 1;
        i = 31 * i + this.texture.hashCode();
        return 31 * i + this.biomes.hashCode();
    }
}
