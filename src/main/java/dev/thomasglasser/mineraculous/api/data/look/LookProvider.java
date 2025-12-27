package dev.thomasglasser.mineraculous.api.data.look;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.thomasglasser.mineraculous.api.client.look.Look;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import dev.thomasglasser.mineraculous.api.core.look.LookUtils;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.api.core.look.metadata.LookMetadataType;
import dev.thomasglasser.mineraculous.api.core.look.metadata.LookMetadataTypes;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousBuiltInRegistries;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.look.asset.CountdownTexturesLookAsset;
import dev.thomasglasser.mineraculous.impl.client.look.asset.TransformationTexturesLookAsset;
import dev.thomasglasser.mineraculous.impl.core.look.LookLoader;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

public abstract class LookProvider implements DataProvider {
    private final Map<String, Builder> looks = new Object2ObjectOpenHashMap<>();
    private final PackOutput output;
    private final String modId;
    private final String defaultAuthor;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    @Nullable
    private DynamicOps<JsonElement> ops;

    protected LookProvider(PackOutput output, String modId, String defaultAuthor, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output;
        this.modId = modId;
        this.defaultAuthor = defaultAuthor;
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
                .metadata(LookMetadataTypes.VALID_MIRACULOUSES, ObjectOpenHashSet.of(miraculous.getKey()))
                .assets(LookContexts.MIRACULOUS_SUIT, assets()
                        .add(LookAssetTypes.TEXTURE, suitBase)
                        .add(LookAssetTypes.GECKOLIB_MODEL, modString("geo/item/armor/miraculous/" + id + ".geo.json"))
                        .add(LookAssetTypes.GECKOLIB_ANIMATIONS, modString("animations/item/armor/miraculous/" + id + ".animation.json")))
                .assets(LookContexts.POWERED_MIRACULOUS, assets()
                        .add(LookAssetTypes.TEXTURE, poweredBase)
                        .add(LookAssetTypes.GECKOLIB_MODEL, miraculousModel)
                        .add(LookAssetTypes.GECKOLIB_ANIMATIONS, miraculousAnimations)
                        .add(LookAssetTypes.ITEM_TRANSFORMS, miraculousTransforms)
                        .add(LookAssetTypes.COUNTDOWN_TEXTURES, new CountdownTexturesLookAsset.CountdownTextures(poweredBase)))
                .assets(LookContexts.HIDDEN_MIRACULOUS, assets()
                        .add(LookAssetTypes.TEXTURE, poweredBase.replace("powered", "hidden"))
                        .add(LookAssetTypes.GECKOLIB_MODEL, miraculousModel)
                        .add(LookAssetTypes.GECKOLIB_ANIMATIONS, miraculousAnimations)
                        .add(LookAssetTypes.ITEM_TRANSFORMS, miraculousTransforms));
        miraculous.value().transformationFrames().ifPresent(frames -> look.asset(LookContexts.MIRACULOUS_SUIT, LookAssetTypes.TRANSFORMATION_TEXTURES, new TransformationTexturesLookAsset.TransformationTextures(suitBase, frames)));
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
                .assets(LookContexts.MIRACULOUS_TOOL, toolAssets.copy())
                .assets(LookContexts.BLOCKING_MIRACULOUS_TOOL, toolAssets.copy())
                .assets(LookContexts.PHONE_MIRACULOUS_TOOL, toolAssets.copy())
                .assets(LookContexts.SPYGLASS_MIRACULOUS_TOOL, toolAssets.copy()
                        .add(LookAssetTypes.SCOPE_TEXTURE, modString("textures/misc/" + id + "_spyglass_scope.png")));
    }

    /**
     * Creates the default look for a {@link Kamikotization}.
     * 
     * @param kamikotization The {@link Kamikotization} to create the look for
     * @return The {@link Builder} for the look
     */
    protected Builder kamikotizationLook(ResourceKey<Kamikotization> kamikotization, String name) {
        String id = kamikotization.location().getPath();
        return look(LookUtils.getDefaultLookId(kamikotization).getPath(), name)
                .metadata(LookMetadataTypes.VALID_KAMIKOTIZATIONS, ObjectOpenHashSet.of(kamikotization))
                .assets(LookContexts.KAMIKOTIZATION_SUIT, assets()
                        .add(LookAssetTypes.TEXTURE, modString("textures/entity/equipment/humanoid/kamikotization/" + id + ".png"))
                        .add(LookAssetTypes.GECKOLIB_MODEL, modString("geo/item/armor/kamikotization/" + id + ".geo.json"))
                        .add(LookAssetTypes.GECKOLIB_ANIMATIONS, modString("animations/item/armor/kamikotization/" + id + ".animation.json")));
    }

    /**
     * Creates the default look for a {@link Kamikotization} without animations.
     *
     * @param kamikotization The {@link Kamikotization} to create the look for
     * @return The {@link Builder} for the look
     */
    protected Builder kamikotizationLookNoAnims(ResourceKey<Kamikotization> kamikotization, String name) {
        return kamikotizationLook(kamikotization, name)
                .remove(LookContexts.KAMIKOTIZATION_SUIT, LookAssetTypes.GECKOLIB_ANIMATIONS);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return this.lookupProvider.thenCompose(provider -> {
            this.looks.clear();
            this.ops = provider.createSerializationContext(JsonOps.INSTANCE);
            this.registerLooks(provider);

            return CompletableFuture.allOf(this.looks.entrySet().stream().map(entry -> {
                String id = entry.getKey();
                Builder builder = entry.getValue();

                if (builder.assets.isEmpty())
                    throw new IllegalStateException("Look " + id + " has no assets");

                JsonObject json = new JsonObject();
                json.addProperty(Look.NAME_KEY, builder.name);
                json.addProperty(Look.AUTHOR_KEY, builder.author);
                if (!builder.metadata.metadata.isEmpty()) {
                    JsonObject metadata = new JsonObject();
                    for (LookMetadataType<?> type : builder.metadata.metadata.keySet()) {
                        ResourceKey<LookMetadataType<?>> key = MineraculousBuiltInRegistries.LOOK_METADATA_TYPE.getResourceKey(type).orElseThrow(() -> new IllegalArgumentException("Invalid look metadata type passed to LookProvider"));
                        metadata.add(key.location().toString(), builder.metadata.save(type));
                    }
                    json.add(Look.METADATA_KEY, metadata);
                }
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

    protected class Builder {
        protected Map<Holder<LookContext>, LookProvider.AssetsBuilder> assets = new Object2ObjectOpenHashMap<>();
        protected final String name;
        protected String author = defaultAuthor;
        protected MetadataBuilder metadata = new MetadataBuilder();

        public Builder(String name) {
            this.name = name;
        }

        /**
         * Sets the author of the look to the provided string.
         * 
         * @param author The author of the look
         * @return The builder
         */
        public Builder author(String author) {
            this.author = author;
            return this;
        }

        /**
         * Adds the provided metadata for the provided type.
         * 
         * @param type The metadata type
         * @param data The metadata to add
         * @return The builder
         * @param <T> The type of the data
         */
        public <T> Builder metadata(LookMetadataType<T> type, T data) {
            this.metadata.add(type, data);
            return this;
        }

        public <T> Builder metadata(Supplier<LookMetadataType<T>> type, T data) {
            return metadata(type.get(), data);
        }

        /**
         * Adds the provided assets for the provided context.
         * 
         * @param context The context of the assets
         * @param builder The assets for the look
         * @return The builder
         */
        public Builder assets(Holder<LookContext> context, AssetsBuilder builder) {
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
        public <S> Builder asset(Holder<LookContext> context, LookAssetType<S, ?> assetType, S asset) {
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

    protected class MetadataBuilder {
        protected Map<LookMetadataType<?>, Object> metadata = new Object2ObjectOpenHashMap<>();

        /**
         * Makes a copy of the current {@link MetadataBuilder} so it can be modified separately.
         *
         * @return A copy of the current {@link MetadataBuilder}
         */
        public MetadataBuilder copy() {
            MetadataBuilder builder = new MetadataBuilder();
            builder.metadata.putAll(metadata);
            return builder;
        }

        /**
         * Adds metadata to the builder.
         *
         * @param type The metadata type
         * @param data The metadata to add
         * @return The builder
         * @param <T> The type of the data
         */
        public <T> MetadataBuilder add(LookMetadataType<T> type, T data) {
            this.metadata.put(type, data);
            return this;
        }

        /**
         * Removes metadata from the builder.
         *
         * @param type The type of the metadata
         * @return The builder
         */
        public MetadataBuilder remove(LookMetadataType<?> type) {
            this.metadata.remove(type);
            return this;
        }

        /**
         * Saves metadata to a {@link JsonElement}.
         *
         * @param type The metadata type
         * @return The metadata as a {@link JsonElement}
         * @param <T> The type of the data
         */
        public <T> JsonElement save(LookMetadataType<T> type) {
            Codec<T> codec = type.codec();
            return codec.encodeStart(ops, (T) metadata.get(type)).getOrThrow();
        }
    }

    protected class AssetsBuilder {
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
            return type.getCodec().encodeStart(ops, (S) assets.get(type)).getOrThrow();
        }
    }
}
