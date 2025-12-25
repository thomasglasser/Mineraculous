package dev.thomasglasser.mineraculous.api.data.look;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.thomasglasser.mineraculous.api.client.look.Look;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import dev.thomasglasser.mineraculous.api.core.look.LookUtils;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.look.asset.CountdownTexturesLookAsset;
import dev.thomasglasser.mineraculous.impl.client.look.asset.TransformationTexturesLookAsset;
import dev.thomasglasser.mineraculous.impl.core.look.LookLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public abstract class LookProvider implements DataProvider {
    private final Map<String, Builder> looks = new Object2ObjectOpenHashMap<>();
    private final PackOutput output;
    private final String modId;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    protected LookProvider(PackOutput output, String modId, CompletableFuture<HolderLookup.Provider> lookupProvider) {
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

    /**
     * Creates and registers a {@link Builder} for the provided id.
     * 
     * @param id The id of the look
     * @return The {@link Builder} for the provided id
     */
    protected Builder look(String id, String name) {
        if (looks.containsKey(id))
            throw new RuntimeException("Duplicate look id " + id);
        Builder builder = new Builder(name);
        looks.put(id, builder);
        return builder;
    }

    /**
     * Creates the default look for a {@link Miraculous}.
     *
     * @param miraculous The {@link Miraculous} to create the look for
     * @return The {@link Builder} for the look
     */
    protected Builder miraculous(Holder<Miraculous> miraculous, String name) {
        ResourceKey<Miraculous> key = miraculous.getKey();
        String id = key.location().getPath();
        String suitBase = modString("textures/entity/equipment/humanoid/miraculous/" + id + ".png");
        String poweredBase = modString("textures/item/miraculous/" + id + "/powered.png");
        String miraculousModel = modString("geo/item/miraculous/" + id + ".geo.json");
        String miraculousAnimations = modString("animations/item/miraculous/" + id + ".animation.json");
        String miraculousTransforms = modString("models/item/miraculous/" + id + ".json");
        Builder look = look(LookUtils.getDefaultLookId(key).getPath(), name)
                .add(LookContexts.MIRACULOUS_SUIT, assets()
                        .add(LookAssetTypes.TEXTURE, suitBase)
                        .add(LookAssetTypes.GECKOLIB_MODEL, modString("geo/item/armor/miraculous/" + id + ".geo.json"))
                        .add(LookAssetTypes.GECKOLIB_ANIMATIONS, modString("animations/item/armor/miraculous/" + id + ".animation.json")))
                .add(LookContexts.POWERED_MIRACULOUS, assets()
                        .add(LookAssetTypes.TEXTURE, poweredBase)
                        .add(LookAssetTypes.GECKOLIB_MODEL, miraculousModel)
                        .add(LookAssetTypes.GECKOLIB_ANIMATIONS, miraculousAnimations)
                        .add(LookAssetTypes.ITEM_TRANSFORMS, miraculousTransforms)
                        .add(LookAssetTypes.COUNTDOWN_TEXTURES, new CountdownTexturesLookAsset.CountdownTextures(poweredBase)))
                .add(LookContexts.HIDDEN_MIRACULOUS, assets()
                        .add(LookAssetTypes.TEXTURE, poweredBase.replace("powered", "hidden"))
                        .add(LookAssetTypes.GECKOLIB_MODEL, miraculousModel)
                        .add(LookAssetTypes.GECKOLIB_ANIMATIONS, miraculousAnimations)
                        .add(LookAssetTypes.ITEM_TRANSFORMS, miraculousTransforms));
        miraculous.value().transformationFrames().ifPresent(frames -> look.add(LookContexts.MIRACULOUS_SUIT, LookAssetTypes.TRANSFORMATION_TEXTURES, new TransformationTexturesLookAsset.TransformationTextures(suitBase, frames)));
        return look;
    }

    /**
     * Creates the default look for a {@link Miraculous} without animations.
     *
     * @param miraculous The {@link Miraculous} to create the look for
     * @return The {@link Builder} for the look
     */
    protected Builder miraculousNoAnims(Holder<Miraculous> miraculous, String name) {
        return miraculous(miraculous, name)
                .remove(LookContexts.MIRACULOUS_SUIT, LookAssetTypes.GECKOLIB_ANIMATIONS)
                .remove(LookContexts.POWERED_MIRACULOUS, LookAssetTypes.GECKOLIB_ANIMATIONS)
                .remove(LookContexts.HIDDEN_MIRACULOUS, LookAssetTypes.GECKOLIB_ANIMATIONS);
    }

    /**
     * Creates the default look for a {@link Miraculous} tool.
     * 
     * @param item The {@link Item} to create the look for
     * @return The {@link Builder} for the look
     */
    protected Builder miraculousTool(ResourceKey<Item> item, String name) {
        String id = item.location().getPath();
        AssetsBuilder toolAssets = assets()
                .add(LookAssetTypes.TEXTURE, modString("textures/item/geo/" + id + ".png"))
                .add(LookAssetTypes.GECKOLIB_MODEL, modString("geo/item/" + id + ".geo.json"))
                .add(LookAssetTypes.GECKOLIB_ANIMATIONS, modString("animations/item/" + id + ".animation.json"));
        return look(LookUtils.getDefaultLookId(item).getPath(), name)
                .add(LookContexts.MIRACULOUS_TOOL, toolAssets.copy())
                .add(LookContexts.MIRACULOUS_TOOL_THROWN, toolAssets.copy())
                .add(LookContexts.MIRACULOUS_TOOL_BLOCKING, toolAssets.copy())
                .add(LookContexts.MIRACULOUS_TOOL_PHONE, toolAssets.copy())
                .add(LookContexts.MIRACULOUS_TOOL_SPYGLASS, toolAssets.copy()
                        .add(LookAssetTypes.SCOPE_TEXTURE, modString("textures/misc/" + id + "_spyglass_scope.png")));
    }

    /**
     * Creates the default look for a {@link Kamikotization}.
     * 
     * @param key The {@link Kamikotization} to create the look for
     * @return The {@link Builder} for the look
     */
    protected Builder kamikotizationLook(ResourceKey<Kamikotization> key, String name) {
        String id = key.location().getPath();
        return look(LookUtils.getDefaultLookId(key).getPath(), name)
                .add(LookContexts.KAMIKOTIZATION_SUIT, assets()
                        .add(LookAssetTypes.TEXTURE, modString("textures/entity/equipment/humanoid/kamikotization/" + id + ".png"))
                        .add(LookAssetTypes.GECKOLIB_MODEL, modString("geo/item/armor/kamikotization/" + id + ".geo.json"))
                        .add(LookAssetTypes.GECKOLIB_ANIMATIONS, modString("animations/item/armor/kamikotization/" + id + ".animation.json")));
    }

    /**
     * Creates the default look for a {@link Kamikotization} without animations.
     * 
     * @param key The {@link Kamikotization} to create the look for
     * @return The {@link Builder} for the look
     */
    protected Builder kamikotizationLookNoAnims(ResourceKey<Kamikotization> key, String name) {
        return kamikotizationLook(key, name)
                .remove(LookContexts.KAMIKOTIZATION_SUIT, LookAssetTypes.GECKOLIB_ANIMATIONS);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.lookupProvider.thenCompose(provider -> {
            this.looks.clear();
            this.registerLooks(provider);

            return CompletableFuture.allOf(this.looks.entrySet().stream().map(entry -> {
                String id = entry.getKey();
                Builder builder = entry.getValue();

                if (builder.assets.isEmpty())
                    throw new IllegalStateException("Look " + id + " has no assets");

                JsonObject json = new JsonObject();
                json.addProperty(Look.NAME_KEY, builder.name);
                json.addProperty(Look.AUTHOR_KEY, builder.author);
                // TODO: Add metadata
                JsonObject contexts = new JsonObject();
                Set<Holder<LookContext>> lookContexts = builder.assets.keySet();
                if (lookContexts.isEmpty())
                    throw new IllegalStateException("Look " + id + " has no contexts");
                for (Holder<LookContext> context : lookContexts) {
                    JsonObject assets = new JsonObject();
                    Set<LookAssetType<?, ?>> assetTypes = builder.assets.get(context).assets.keySet();
                    if (assetTypes.isEmpty())
                        throw new IllegalStateException("Look " + id + " has no assets for context " + context.getKey());
                    for (LookAssetType<?, ?> assetType : assetTypes) {
                        if (!context.value().assetTypes().contains(assetType.key()))
                            throw new IllegalStateException("Look " + id + " has invalid asset " + assetType.key() + " for context " + context.getKey());
                        assets.add(assetType.key().toString(), builder.assets.get(context).save(assetType));
                    }
                    contexts.add(context.getKey().location().toString(), assets);
                }
                json.add(Look.ASSETS_KEY, contexts);
                return DataProvider.saveStable(output, json, this.getOutput().getOutputFolder(PackOutput.Target.RESOURCE_PACK).resolve(getModId()).resolve(LookLoader.LOOKS_SUBPATH).resolve(id + ".json"));
            }).toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return getModId() + " Looks";
    }

    protected static class Builder {
        protected Map<Holder<LookContext>, LookProvider.AssetsBuilder> assets = new Object2ObjectOpenHashMap<>();
        protected final String name;
        protected String author = "Unknown";

        public Builder(String name) {
            this.name = name;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        /**
         * Adds the provided assets for the provided context.
         * 
         * @param context The context of the assets
         * @param builder The assets for the look
         * @return The builder
         */
        public Builder add(Holder<LookContext> context, AssetsBuilder builder) {
            this.assets.put(context, builder);
            return this;
        }

        /**
         * Adds the provided asset for the provided context.
         * 
         * @param context   The context of the asset
         * @param assetType The type of the asset
         * @param asset     The asset
         * @return The builder
         * @param <S> The stored type of the asset
         */
        public <S> Builder add(Holder<LookContext> context, LookAssetType<S, ?> assetType, S asset) {
            AssetsBuilder builder = this.assets.getOrDefault(context, new AssetsBuilder());
            builder.add(assetType, asset);
            return this;
        }

        /**
         * Removes the provided asset type from the assets for the provided context.
         * 
         * @param context   The context of the asset
         * @param assetType The asset type to remove
         * @return The builder
         */
        public Builder remove(Holder<LookContext> context, LookAssetType<?, ?> assetType) {
            AssetsBuilder builder = this.assets.get(context);
            if (builder != null)
                builder.remove(assetType);
            return this;
        }
    }

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
