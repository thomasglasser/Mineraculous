package dev.thomasglasser.mineraculous.api.data.look;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractLookProvider implements DataProvider {
    private final PackOutput output;
    private final String modId;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    protected AbstractLookProvider(PackOutput output, String modId, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output;
        this.modId = modId;
        this.lookupProvider = lookupProvider;
    }

    /**
     * Returns the provider's {@link PackOutput}.
     * 
     * @return The provider's {@link PackOutput}
     */
    public PackOutput getOutput() {
        return output;
    }

    /**
     * Returns the provider's mod id.
     * 
     * @return The provider's mod id
     */
    public String getModId() {
        return modId;
    }

    /**
     * Registers looks for generation.
     * 
     * @param provider The registry provider
     */
    protected abstract void registerLooks(HolderLookup.Provider provider);

    /**
     * Creates a new {@link AssetsBuilder}.
     * 
     * @return A new {@link AssetsBuilder}
     */
    protected AssetsBuilder assets() {
        return new AssetsBuilder();
    }

    /**
     * Creates a {@link ResourceLocation} with the provider's mod id and the provided path.
     * 
     * @param path The path of the {@link ResourceLocation}
     * @return A {@link ResourceLocation} with the provider's mod id and the provided path
     */
    protected ResourceLocation modLoc(String path) {
        return ResourceLocation.fromNamespaceAndPath(modId, path);
    }

    /**
     * Creates a string with the provider's mod id and the provided path.
     * 
     * @param path The path of the {@link ResourceLocation}
     * @return A string with the provider's mod id and the provided path
     */
    protected String modString(String path) {
        return modLoc(path).toString();
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput output) {
        return lookupProvider.thenCompose(provider -> run(output, provider));
    }

    /**
     * Generates the files.
     * 
     * @param output   The output of the provider
     * @param provider The registry provider
     * @return A {@link CompletableFuture} that completes when the files have been generated
     */
    protected abstract CompletableFuture<?> run(CachedOutput output, HolderLookup.Provider provider);

    protected static class AssetsBuilder {
        protected Map<LookAssetType<?, ?>, Object> assets = new Object2ObjectOpenHashMap<>();

        /**
         * Makes a copy of the current {@link AssetsBuilder} so it can be modified separately.
         * 
         * @return A copy of the current {@link AssetsBuilder}
         */
        public AssetsBuilder copy() {
            AssetsBuilder builder = new AssetsBuilder();
            builder.assets.putAll(assets);
            return builder;
        }

        /**
         * Adds an asset to the builder.
         * 
         * @param type  The type of the asset
         * @param asset The asset
         * @return The builder
         * @param <S> The stored type of the asset
         */
        public <S> AssetsBuilder add(LookAssetType<S, ?> type, S asset) {
            this.assets.put(type, asset);
            return this;
        }

        /**
         * Removes an asset from the builder.
         * 
         * @param type The type of the asset
         * @return The builder
         */
        public AssetsBuilder remove(LookAssetType<?, ?> type) {
            this.assets.remove(type);
            return this;
        }

        /**
         * Saves an asset to a {@link JsonElement}.
         * 
         * @param type The type of the asset
         * @return The asset as a {@link JsonElement}
         * @param <S> The stored type of the asset
         */
        protected <S> JsonElement save(LookAssetType<S, ?> type) {
            return type.getCodec().encodeStart(JsonOps.INSTANCE, (S) assets.get(type)).getOrThrow();
        }
    }
}
