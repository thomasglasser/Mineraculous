package dev.thomasglasser.mineraculous.api.client.look.metadata;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.thomasglasser.mineraculous.api.core.look.metadata.LookMetadataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/// Holds a collection of metadata associated with {@link LookMetadataType}s.
public class LookMetadata {
    private final ImmutableMap<LookMetadataType<?>, ?> metadata;

    private LookMetadata(ImmutableMap<LookMetadataType<?>, ?> metadata) {
        this.metadata = metadata;
    }

    /**
     * Retrieves the metadata for the provided type.
     * @param type The type to retrieve the metadata for
     * @return The metadata for the provided type
     * @param <T> The type of the metadata
     */
    public <T> @Nullable T get(LookMetadataType<T> type) {
        return (T) metadata.get(type);
    }

    @ApiStatus.Internal
    public static class Builder {
        private final ImmutableMap.Builder<LookMetadataType<?>, Object> metadata = new ImmutableMap.Builder<>();

        public <T> Builder add(LookMetadataType<T> type, JsonElement element) throws IllegalArgumentException {
            metadata.put(type, type.codec().parse(JsonOps.INSTANCE, element).getOrThrow());
            return this;
        }

        public LookMetadata build() {
            return new LookMetadata(metadata.build());
        }
    }
}
