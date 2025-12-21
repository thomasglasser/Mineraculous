package dev.thomasglasser.mineraculous.api.client.look.renderer;

import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.impl.client.look.Look;
import dev.thomasglasser.mineraculous.impl.client.renderer.armor.KamikotizationArmorItemRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.jetbrains.annotations.Nullable;

/// Represents a renderer for a thrown kamikotization tool that uses the look system.
public interface ThrownKamikotizationToolLookRenderer<T extends AbstractArrow> extends LookRenderer {
    T getAnimatable();

    @Override
    default ResourceLocation getDefaultLookId() {
        return MiraculousToolLookRenderer.getDefaultLookId(getAnimatable().getPickupItemStackOrigin());
    }

    @Override
    default Holder<LookContext> getContext() {
        return LookContexts.KAMIKOTIZATION_TOOL_THROWN;
    }

    @Override
    default @Nullable Look getLook() {
        return KamikotizationArmorItemRenderer.getLook(getAnimatable().getPickupItemStackOrigin(), getContext());
    }
}
