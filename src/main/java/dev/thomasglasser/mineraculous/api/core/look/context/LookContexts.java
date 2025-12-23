package dev.thomasglasser.mineraculous.api.core.look.context;

import com.google.common.collect.ImmutableSet;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.look.asset.LookAssetTypeKeys;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

public class LookContexts {
    private static final DeferredRegister<LookContext> LOOK_CONTEXTS = DeferredRegister.create(MineraculousRegistries.LOOK_CONTEXT, MineraculousConstants.MOD_ID);

    // Miraculous
    /// Used for rendering {@link dev.thomasglasser.mineraculous.impl.world.item.armor.MiraculousArmorItem}s on the body.
    public static final DeferredHolder<LookContext, LookContext> MIRACULOUS_SUIT = register("miraculous_suit", LookAssetTypeKeys.TEXTURE, LookAssetTypeKeys.GECKOLIB_MODEL, LookAssetTypeKeys.GECKOLIB_ANIMATIONS, LookAssetTypeKeys.TRANSFORMATION_TEXTURES);
    /// Used for rendering a {@link dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem} that is powered or active.
    public static final DeferredHolder<LookContext, LookContext> POWERED_MIRACULOUS = register("powered_miraculous", LookAssetTypeKeys.TEXTURE, LookAssetTypeKeys.GECKOLIB_MODEL, LookAssetTypeKeys.GECKOLIB_ANIMATIONS, LookAssetTypeKeys.ITEM_TRANSFORMS, LookAssetTypeKeys.COUNTDOWN_TEXTURES);
    /// Used for rendering a {@link dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem} that is hidden.
    public static final DeferredHolder<LookContext, LookContext> HIDDEN_MIRACULOUS = register("hidden_miraculous", LookAssetTypeKeys.TEXTURE, LookAssetTypeKeys.GECKOLIB_MODEL, LookAssetTypeKeys.GECKOLIB_ANIMATIONS, LookAssetTypeKeys.ITEM_TRANSFORMS);
    /// Used for rendering a miraculous tool.
    public static final DeferredHolder<LookContext, LookContext> MIRACULOUS_TOOL = register("miraculous_tool", LookAssetTypeKeys.TEXTURE, LookAssetTypeKeys.GECKOLIB_MODEL, LookAssetTypeKeys.GECKOLIB_ANIMATIONS);
    /// Used for rendering a thrown miraculous tool.
    public static final DeferredHolder<LookContext, LookContext> MIRACULOUS_TOOL_THROWN = register("miraculous_tool_thrown", LookAssetTypeKeys.TEXTURE, LookAssetTypeKeys.GECKOLIB_MODEL, LookAssetTypeKeys.GECKOLIB_ANIMATIONS);
    /// Used for rendering a blocking miraculous tool.
    public static final DeferredHolder<LookContext, LookContext> MIRACULOUS_TOOL_BLOCKING = register("miraculous_tool_blocking", LookAssetTypeKeys.TEXTURE, LookAssetTypeKeys.GECKOLIB_MODEL, LookAssetTypeKeys.GECKOLIB_ANIMATIONS);
    /// Used for rendering a miraculous tool phone.
    public static final DeferredHolder<LookContext, LookContext> MIRACULOUS_TOOL_PHONE = register("miraculous_tool_phone", LookAssetTypeKeys.TEXTURE, LookAssetTypeKeys.GECKOLIB_MODEL, LookAssetTypeKeys.GECKOLIB_ANIMATIONS);
    /// Used for rendering a miraculous tool spyglass.
    public static final DeferredHolder<LookContext, LookContext> MIRACULOUS_TOOL_SPYGLASS = register("miraculous_tool_spyglass", LookAssetTypeKeys.TEXTURE, LookAssetTypeKeys.GECKOLIB_MODEL, LookAssetTypeKeys.GECKOLIB_ANIMATIONS, LookAssetTypeKeys.SCOPE_TEXTURE);

    // Kamikotization
    /// Used for rendering {@link dev.thomasglasser.mineraculous.impl.world.item.armor.KamikotizationArmorItem}s on the body.
    public static final DeferredHolder<LookContext, LookContext> KAMIKOTIZATION_SUIT = register("kamikotization_suit", LookAssetTypeKeys.TEXTURE, LookAssetTypeKeys.GECKOLIB_MODEL, LookAssetTypeKeys.GECKOLIB_ANIMATIONS);
    /// Used for rendering a kamikotization tool.
    public static final DeferredHolder<LookContext, LookContext> KAMIKOTIZATION_TOOL = register("kamikotization_tool", LookAssetTypeKeys.TEXTURE, LookAssetTypeKeys.GECKOLIB_MODEL, LookAssetTypeKeys.GECKOLIB_ANIMATIONS);
    /// Used for rendering a thrown kamikotization tool.
    public static final DeferredHolder<LookContext, LookContext> KAMIKOTIZATION_TOOL_THROWN = register("kamikotization_tool_thrown", LookAssetTypeKeys.TEXTURE, LookAssetTypeKeys.GECKOLIB_MODEL, LookAssetTypeKeys.GECKOLIB_ANIMATIONS);
    /// Used for rendering a kamikotization tool spyglass.
    public static final DeferredHolder<LookContext, LookContext> KAMIKOTIZATION_TOOL_SPYGLASS = register("kamikotization_tool_spyglass", LookAssetTypeKeys.TEXTURE, LookAssetTypeKeys.GECKOLIB_MODEL, LookAssetTypeKeys.GECKOLIB_ANIMATIONS, LookAssetTypeKeys.SCOPE_TEXTURE);

    private static DeferredHolder<LookContext, LookContext> register(String name, ResourceLocation... assetTypes) {
        return LOOK_CONTEXTS.register(name, () -> new LookContext(ImmutableSet.copyOf(assetTypes)));
    }

    @ApiStatus.Internal
    public static void init() {}
}
