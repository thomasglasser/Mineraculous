package dev.thomasglasser.mineraculous.api.core.look.context;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;

public class LookContextSets {
    /// Used for {@link dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData}.
    public static final ImmutableSet<Holder<LookContext>> MIRACULOUS = ImmutableSet.of(
            LookContexts.MIRACULOUS_SUIT,
            LookContexts.POWERED_MIRACULOUS,
            LookContexts.HIDDEN_MIRACULOUS,
            LookContexts.MIRACULOUS_TOOL,
            LookContexts.BLOCKING_MIRACULOUS_TOOL,
            LookContexts.PHONE_MIRACULOUS_TOOL,
            LookContexts.SPYGLASS_MIRACULOUS_TOOL);

    /// Used for {@link dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData}.
    public static final ImmutableSet<Holder<LookContext>> KAMIKOTIZATION = ImmutableSet.of(
            LookContexts.KAMIKOTIZATION_SUIT,
            LookContexts.KAMIKOTIZATION_TOOL,
            LookContexts.SPYGLASS_KAMIKOTIZATION_TOOL);
}
