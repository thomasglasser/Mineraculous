package dev.thomasglasser.mineraculous.api.data.look;

import com.google.gson.JsonObject;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.look.asset.CountdownTexturesLookAsset;
import dev.thomasglasser.mineraculous.impl.client.look.asset.TransformationTexturesLookAsset;
import dev.thomasglasser.mineraculous.impl.client.renderer.armor.KamikotizationArmorItemRenderer;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.MiraculousItemRenderer;
import dev.thomasglasser.mineraculous.impl.server.look.ServerLookManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
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

    /**
     * Creates the default look for a {@link Miraculous}.
     *
     * @param miraculous The {@link Miraculous} to create the look for
     * @return The {@link Builder} for the look
     */
    protected Builder miraculousLook(Holder<Miraculous> miraculous) {
        ResourceKey<Miraculous> key = miraculous.getKey();
        String name = key.location().getPath();
        String suitBase = modString("textures/entity/equipment/humanoid/miraculous/" + name + ".png");
        String poweredBase = modString("textures/item/miraculous/" + name + "/powered.png");
        String miraculousModel = modString("geo/item/miraculous/" + name + ".geo.json");
        String miraculousAnimations = modString("animations/item/miraculous/" + name + ".animation.json");
        String miraculousTransforms = modString("models/item/miraculous/" + name + ".json");
        Builder look = look(MiraculousItemRenderer.getDefaultLookId(key).getPath())
                .add(LookContexts.MIRACULOUS_SUIT, assets()
                        .add(LookAssetTypes.TEXTURE, suitBase)
                        .add(LookAssetTypes.GECKOLIB_MODEL, modString("geo/item/armor/miraculous/" + name + ".geo.json"))
                        .add(LookAssetTypes.GECKOLIB_ANIMATIONS, modString("animations/item/armor/miraculous/" + name + ".animation.json")))
                .add(LookContexts.POWERED_MIRACULOUS, assets()
                        .add(LookAssetTypes.TEXTURE, poweredBase)
                        .add(LookAssetTypes.GECKOLIB_MODEL, miraculousModel)
                        .add(LookAssetTypes.GECKOLIB_ANIMATIONS, miraculousAnimations)
                        .add(LookAssetTypes.ITEM_TRANSFORMS, miraculousTransforms)
                        .add(LookAssetTypes.COUNTDOWN_TEXTURES, new CountdownTexturesLookAsset.CountdownTextures(poweredBase)))
                .add(LookContexts.ACTIVE_MIRACULOUS, assets()
                        .add(LookAssetTypes.TEXTURE, poweredBase.replace("powered", "active")))
                .add(LookContexts.HIDDEN_MIRACULOUS, assets()
                        .add(LookAssetTypes.TEXTURE, poweredBase.replace("powered", "hidden"))
                        .add(LookAssetTypes.GECKOLIB_MODEL, miraculousModel)
                        .add(LookAssetTypes.GECKOLIB_ANIMATIONS, miraculousAnimations)
                        .add(LookAssetTypes.ITEM_TRANSFORMS, miraculousTransforms));
        miraculous.value().transformationFrames().ifPresent(frames -> look.get(LookContexts.MIRACULOUS_SUIT).add(LookAssetTypes.TRANSFORMATION_TEXTURES, new TransformationTexturesLookAsset.TransformationTextures(suitBase, frames)));
        return look;
    }

    /**
     * Creates the default look for a {@link Miraculous} without animations.
     *
     * @param miraculous The {@link Miraculous} to create the look for
     * @return The {@link Builder} for the look
     */
    protected Builder miraculousLookNoAnims(Holder<Miraculous> miraculous) {
        Builder look = miraculousLook(miraculous);
        look.get(LookContexts.MIRACULOUS_SUIT)
                .remove(LookAssetTypes.GECKOLIB_ANIMATIONS);
        look.get(LookContexts.POWERED_MIRACULOUS)
                .remove(LookAssetTypes.GECKOLIB_ANIMATIONS);
        look.get(LookContexts.HIDDEN_MIRACULOUS)
                .remove(LookAssetTypes.GECKOLIB_ANIMATIONS);
        return look;
    }

    /**
     * Creates the default look for a {@link Kamikotization}.
     * 
     * @param key The {@link Kamikotization} to create the look for
     * @return The {@link Builder} for the look
     */
    protected Builder kamikotizationLook(ResourceKey<Kamikotization> key) {
        String name = key.location().getPath();
        return look(KamikotizationArmorItemRenderer.getDefaultLookId(key).getPath())
                .add(LookContexts.KAMIKOTIZATION_SUIT, assets()
                        .add(LookAssetTypes.TEXTURE, modString("textures/entity/equipment/humanoid/kamikotization/" + name + ".png"))
                        .add(LookAssetTypes.GECKOLIB_MODEL, modString("geo/item/armor/kamikotization/" + name + ".geo.json"))
                        .add(LookAssetTypes.GECKOLIB_ANIMATIONS, modString("animations/item/armor/kamikotization/" + name + ".animation.json")));
    }

    /**
     * Creates the default look for a {@link Kamikotization} without animations.
     * 
     * @param key The {@link Kamikotization} to create the look for
     * @return The {@link Builder} for the look
     */
    protected Builder kamikotizationLookNoAnims(ResourceKey<Kamikotization> key) {
        Builder look = kamikotizationLook(key);
        look.get(LookContexts.KAMIKOTIZATION_SUIT)
                .remove(LookAssetTypes.GECKOLIB_ANIMATIONS);
        return look;
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
