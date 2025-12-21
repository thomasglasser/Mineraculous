package dev.thomasglasser.mineraculous.api.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * A block position in a dimension.
 *
 * @param dimension The dimension the block position is in
 * @param pos       The block position
 */
public record BlockLocation(ResourceKey<Level> dimension, BlockPos pos) {
    public static final Codec<BlockLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(BlockLocation::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(BlockLocation::pos)).apply(instance, BlockLocation::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockLocation> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), BlockLocation::dimension,
            BlockPos.STREAM_CODEC, BlockLocation::pos,
            BlockLocation::new);
}
