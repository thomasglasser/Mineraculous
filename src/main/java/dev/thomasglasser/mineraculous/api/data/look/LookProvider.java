package dev.thomasglasser.mineraculous.api.data.look;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.look.LookLoader;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

public abstract class LookProvider extends AbstractLookProvider {
    private final Map<String, Builder> looks = new Object2ObjectOpenHashMap<>();

    protected LookProvider(PackOutput output, String modId, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, modId, lookupProvider);
    }

    /**
     * Creates and registers a {@link Builder} for the provided id.
     * 
     * @param id   The id of the look
     * @param name The name of the look
     * @return The {@link Builder} for the provided id
     */
    protected Builder look(String id, String name) {
        Builder builder = new Builder(name);
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

            JsonObject json = new JsonObject();
            json.addProperty(LookLoader.NAME_KEY, builder.name);
            if (builder.author != null) json.addProperty(LookLoader.AUTHOR_KEY, builder.author);
            if (!builder.validMiraculouses.isEmpty()) {
                JsonArray array = new JsonArray();
                for (ResourceKey<Miraculous> key : builder.validMiraculouses) {
                    array.add(ResourceKey.codec(MineraculousRegistries.MIRACULOUS).encodeStart(JsonOps.INSTANCE, key).getOrThrow());
                }
                json.add(LookLoader.VALID_MIRACULOUSES_KEY, array);
            }
            if (builder.assets.isEmpty())
                throw new RuntimeException("Look " + id + " has no assets");
            JsonObject assets = new JsonObject();
            for (ResourceKey<LookContext> context : builder.assets.keySet()) {
                JsonObject contextAssets = new JsonObject();
                AssetsBuilder assetsBuilder = builder.assets.get(context);
                if (assetsBuilder.assets.isEmpty())
                    throw new RuntimeException("Look " + id + " has no assets for context " + context.location());
                for (Map.Entry<LookAssetType<?, ?>, Object> asset : assetsBuilder.assets.entrySet()) {
                    LookAssetType<?, ?> assetType = asset.getKey();
                    contextAssets.add(assetType.key().toString(), assetsBuilder.save(assetType));
                }
                assets.add(context.location().toString(), contextAssets);
            }
            json.add(LookLoader.ASSETS_KEY, assets);
            return DataProvider.saveStable(output, json, this.getOutput().getOutputFolder().resolve(ServerLookManager.LOOKS_SUBPATH).resolve(id).resolve(LookLoader.JSON_NAME));
        }).toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return getModId() + " Looks";
    }

    protected static class Builder {
        protected final String name;
        protected @Nullable String author = null;
        protected Set<ResourceKey<Miraculous>> validMiraculouses = new ObjectOpenHashSet<>();
        protected Map<ResourceKey<LookContext>, AssetsBuilder> assets = new Object2ObjectOpenHashMap<>();

        protected Builder(String name) {
            this.name = name;
        }

        protected Builder author(String author) {
            this.author = author;
            return this;
        }

        @SafeVarargs
        protected final Builder supports(ResourceKey<Miraculous>... miraculouses) {
            Collections.addAll(this.validMiraculouses, miraculouses);
            return this;
        }

        protected Builder assets(ResourceKey<LookContext> context, AssetsBuilder builder) {
            this.assets.put(context, builder);
            return this;
        }

        protected Builder assets(Holder<LookContext> context, AssetsBuilder builder) {
            return assets(context.getKey(), builder);
        }
    }
}
