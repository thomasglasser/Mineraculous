package dev.thomasglasser.mineraculous.api.core.look.context;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.impl.core.look.context.kamikotization.KamikotizationLookContext;
import dev.thomasglasser.mineraculous.impl.core.look.context.kamikotization.SpyglassKamikotizationToolLookContext;
import dev.thomasglasser.mineraculous.impl.core.look.context.miraculous.BlockingMiraculousToolLookContext;
import dev.thomasglasser.mineraculous.impl.core.look.context.miraculous.HiddenMiraculousLookContext;
import dev.thomasglasser.mineraculous.impl.core.look.context.miraculous.MiraculousSuitLookContext;
import dev.thomasglasser.mineraculous.impl.core.look.context.miraculous.MiraculousToolLookContext;
import dev.thomasglasser.mineraculous.impl.core.look.context.miraculous.PoweredMiraculousLookContext;
import dev.thomasglasser.mineraculous.impl.core.look.context.miraculous.SpyglassMiraculousToolLookContext;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.function.Supplier;
import org.jetbrains.annotations.ApiStatus;

public class LookContexts {
    private static final DeferredRegister<LookContext> LOOK_CONTEXTS = DeferredRegister.create(MineraculousRegistries.LOOK_CONTEXT, MineraculousConstants.MOD_ID);

    // Miraculous
    /// Used for rendering {@link dev.thomasglasser.mineraculous.impl.world.item.armor.MiraculousArmorItem}s on the body.
    public static final DeferredHolder<LookContext, MiraculousSuitLookContext> MIRACULOUS_SUIT = register("miraculous_suit", MiraculousSuitLookContext::new);
    /// Used for rendering a {@link dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem} that is powered or active.
    public static final DeferredHolder<LookContext, PoweredMiraculousLookContext> POWERED_MIRACULOUS = register("powered_miraculous", PoweredMiraculousLookContext::new);
    /// Used for rendering a {@link dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem} that is hidden.
    public static final DeferredHolder<LookContext, HiddenMiraculousLookContext> HIDDEN_MIRACULOUS = register("hidden_miraculous", HiddenMiraculousLookContext::new);
    /// Used for rendering a miraculous tool.
    public static final DeferredHolder<LookContext, MiraculousToolLookContext> MIRACULOUS_TOOL = register("miraculous_tool", MiraculousToolLookContext::new);
    /// Used for rendering a blocking miraculous tool.
    public static final DeferredHolder<LookContext, BlockingMiraculousToolLookContext> BLOCKING_MIRACULOUS_TOOL = register("blocking_miraculous_tool", BlockingMiraculousToolLookContext::new);
    /// Used for rendering a miraculous tool phone.
    public static final DeferredHolder<LookContext, MiraculousToolLookContext> PHONE_MIRACULOUS_TOOL = register("phone_miraculous_tool", MiraculousToolLookContext::new);
    /// Used for rendering a miraculous tool spyglass.
    public static final DeferredHolder<LookContext, SpyglassMiraculousToolLookContext> SPYGLASS_MIRACULOUS_TOOL = register("spyglass_miraculous_tool", SpyglassMiraculousToolLookContext::new);

    // Kamikotization
    /// Used for rendering {@link dev.thomasglasser.mineraculous.impl.world.item.armor.KamikotizationArmorItem}s on the body.
    public static final DeferredHolder<LookContext, KamikotizationLookContext> KAMIKOTIZATION_SUIT = register("kamikotization_suit", KamikotizationLookContext::new);
    /// Used for rendering a kamikotization tool.
    public static final DeferredHolder<LookContext, KamikotizationLookContext> KAMIKOTIZATION_TOOL = register("kamikotization_tool", KamikotizationLookContext::new);
    /// Used for rendering a kamikotization tool spyglass.
    public static final DeferredHolder<LookContext, SpyglassKamikotizationToolLookContext> SPYGLASS_KAMIKOTIZATION_TOOL = register("spyglass_kamikotization_tool", SpyglassKamikotizationToolLookContext::new);

    private static <T extends LookContext> DeferredHolder<LookContext, T> register(String name, Supplier<T> supplier) {
        return LOOK_CONTEXTS.register(name, supplier);
    }

    @ApiStatus.Internal
    public static void init() {}
}
