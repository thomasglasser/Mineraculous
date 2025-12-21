package dev.thomasglasser.mineraculous.api.core.look.context;

import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetType;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

public class LookContexts {
    private static final DeferredRegister<LookContext> LOOK_CONTEXTS = DeferredRegister.create(MineraculousRegistries.LOOK_CONTEXT, MineraculousConstants.MOD_ID);

    // Miraculous
    /// Used for rendering {@link dev.thomasglasser.mineraculous.impl.world.item.armor.MiraculousArmorItem}s on the body.
    public static final DeferredHolder<LookContext, LookContext> MIRACULOUS_SUIT = register("miraculous_suit", LookAssetTypes.TEXTURE, LookAssetTypes.GECKOLIB_MODEL, LookAssetTypes.GECKOLIB_ANIMATIONS, LookAssetTypes.TRANSFORMATION_TEXTURES);
    /// Used for rendering a {@link dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem} that is powered.
    public static final DeferredHolder<LookContext, LookContext> POWERED_MIRACULOUS = register("powered_miraculous", LookAssetTypes.TEXTURE, LookAssetTypes.GECKOLIB_MODEL, LookAssetTypes.GECKOLIB_ANIMATIONS, LookAssetTypes.ITEM_TRANSFORMS, LookAssetTypes.COUNTDOWN_TEXTURES);
    /**
     * Used for rendering a {@link dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem} that is not powered or hidden.
     * Only applies to the texture,
     * {@link LookContexts#POWERED_MIRACULOUS} is used for other asset types.
     */
    public static final DeferredHolder<LookContext, LookContext> ACTIVE_MIRACULOUS = register("active_miraculous", LookAssetTypes.TEXTURE);
    /// Used for rendering a {@link dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem} that is hidden.
    public static final DeferredHolder<LookContext, LookContext> HIDDEN_MIRACULOUS = register("hidden_miraculous", LookAssetTypes.TEXTURE, LookAssetTypes.GECKOLIB_MODEL, LookAssetTypes.GECKOLIB_ANIMATIONS, LookAssetTypes.ITEM_TRANSFORMS);

    // Kamikotization
    /// Used for rendering {@link dev.thomasglasser.mineraculous.impl.world.item.armor.KamikotizationArmorItem}s on the body.
    public static final DeferredHolder<LookContext, LookContext> KAMIKOTIZATION_SUIT = register("kamikotization_suit", LookAssetTypes.TEXTURE, LookAssetTypes.GECKOLIB_MODEL, LookAssetTypes.GECKOLIB_ANIMATIONS);

    private static DeferredHolder<LookContext, LookContext> register(String name, LookAssetType<?, ?>... assetTypes) {
        return LOOK_CONTEXTS.register(name, () -> new LookContext(ImmutableSet.copyOf(assetTypes)));
    }

    @ApiStatus.Internal
    public static void init() {}
}
