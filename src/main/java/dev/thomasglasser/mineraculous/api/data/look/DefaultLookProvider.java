package dev.thomasglasser.mineraculous.api.data.look;

import com.google.gson.JsonObject;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public abstract class DefaultLookProvider extends AbstractLookProvider {
    private final Map<String, Builder> looks = new Object2ObjectOpenHashMap<>();

    protected DefaultLookProvider(PackOutput output, String modId, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, modId, lookupProvider);
    }

    /**
     * Creates and registers a {@link Builder} for the provided id.
     * 
     * @param id The id of the look
     * @return The {@link Builder} for the provided id
     */
    protected Builder look(String id) {
        Builder builder = new Builder();
        if (looks.containsKey(id))
            throw new RuntimeException("Duplicate look id " + id);
        looks.put(id, builder);
        return builder;
    }

    @Override
    protected CompletableFuture<?> run(CachedOutput output, HolderLookup.Provider provider) {
        this.looks.clear();
        this.registerLooks(provider);

        return CompletableFuture.allOf(this.looks.entrySet().stream().map(entry -> {
            String id = entry.getKey();
            Builder builder = entry.getValue();

            if (builder.assets.isEmpty())
                throw new RuntimeException("Look " + id + " has no assets");
            JsonObject assets = new JsonObject();
            for (Holder<LookContext> context : builder.assets.keySet()) {
                ResourceLocation contextKey = context.getKey().location();
                JsonObject contextAssets = new JsonObject();
                LookProvider.AssetsBuilder assetsBuilder = builder.assets.get(context);
                if (assetsBuilder.assets.isEmpty())
                    throw new RuntimeException("Look " + id + " has no assets for context " + contextKey);
                for (LookAssetType<?, ?> assetType : context.value().assetTypes()) {
                    if (!assetType.isOptional() && !assetsBuilder.assets.containsKey(assetType))
                        throw new RuntimeException("Look " + id + " has no asset for context " + contextKey + " and asset type " + assetType.key());
                }
                for (Map.Entry<LookAssetType<?, ?>, Object> asset : assetsBuilder.assets.entrySet()) {
                    LookAssetType<?, ?> assetType = asset.getKey();
                    contextAssets.add(assetType.key().toString(), assetsBuilder.save(assetType));
                }
                assets.add(contextKey.toString(), contextAssets);
            }
            return DataProvider.saveStable(output, assets, this.getOutput().getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(getModId()).resolve(ServerLookManager.LOOKS_SUBPATH).resolve(id + ".json"));
        }).toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return getModId() + " Default Looks";
    }

    protected static class Builder {
        protected Map<Holder<LookContext>, LookProvider.AssetsBuilder> assets = new Object2ObjectOpenHashMap<>();

        protected AssetsBuilder get(Holder<LookContext> context) {
            return this.assets.getOrDefault(context, new LookProvider.AssetsBuilder());
        }

        protected Builder add(Holder<LookContext> context, AssetsBuilder builder) {
            this.assets.put(context, builder);
            return this;
        }
    }
}
