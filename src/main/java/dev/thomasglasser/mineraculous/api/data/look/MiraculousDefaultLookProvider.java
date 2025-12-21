package dev.thomasglasser.mineraculous.api.data.look;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.impl.client.look.asset.CountdownTexturesLookAsset;
import dev.thomasglasser.mineraculous.impl.client.look.asset.TransformationTexturesLookAsset;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.MiraculousItemRenderer;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

public class MiraculousDefaultLookProvider extends DefaultLookProvider {
    @ApiStatus.Internal
    public MiraculousDefaultLookProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this(output, MineraculousConstants.MOD_ID, lookupProvider);
    }

    protected MiraculousDefaultLookProvider(PackOutput output, String modId, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, modId, lookupProvider);
    }

    @Override
    protected void registerLooks(HolderLookup.Provider provider) {
        HolderGetter<Miraculous> miraculouses = provider.lookupOrThrow(MineraculousRegistries.MIRACULOUS);

        miraculousLookNoAnims(miraculouses.getOrThrow(Miraculouses.LADYBUG));
        miraculousLookNoAnims(miraculouses.getOrThrow(Miraculouses.CAT));
        miraculousLookNoAnims(miraculouses.getOrThrow(Miraculouses.BUTTERFLY));
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
                        .add(LookAssetTypes.GECKOLIB_ANIMATIONS, modString("animations/item/armor/miraculous" + name + ".animation.json")))
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
}
