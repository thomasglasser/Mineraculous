package dev.thomasglasser.mineraculous.api.core.look.metadata;

import com.mojang.serialization.Codec;

/**
 * Holds a codec for serializing look metadata.
 * @param codec The codec for serializing look metadata
 * @param <T> The type of the metadata
 */
public record LookMetadataType<T>(Codec<T> codec) {}
