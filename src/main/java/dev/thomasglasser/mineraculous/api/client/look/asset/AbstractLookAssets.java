package dev.thomasglasser.mineraculous.api.client.look.asset;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

public interface AbstractLookAssets {
    <L> L get(LookAssetType<?, L> type);

    boolean has(LookAssetType<?, ?> type);

    boolean isEmpty();

    interface Builder<T extends AbstractLookAssets> {
        <S> Builder<T> add(LookAssetType<S, ?> type, JsonElement asset, ResourceLocation lookId, ResourceLocation contextId) throws IllegalArgumentException;

        T build();
    }
}
