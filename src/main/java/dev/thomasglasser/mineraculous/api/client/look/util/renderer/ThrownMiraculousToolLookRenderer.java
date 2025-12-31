package dev.thomasglasser.mineraculous.api.client.look.util.renderer;

import dev.thomasglasser.mineraculous.api.client.look.Look;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.MiraculousItemRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.jetbrains.annotations.Nullable;

/// Represents a renderer for a thrown miraculous tool that uses the look system.
public interface ThrownMiraculousToolLookRenderer<T extends AbstractArrow> extends LookRenderer {
    T getAnimatable();

    @Override
    default ResourceLocation getDefaultLookId() {
        return MiraculousToolLookRenderer.getDefaultLookId(getAnimatable().getPickupItemStackOrigin());
    }

    @Override
    default Holder<LookContext> getContext() {
        return LookContexts.MIRACULOUS_TOOL;
    }

    @Override
    default @Nullable Look<?> getLook() {
        return MiraculousItemRenderer.getLook(getAnimatable().getPickupItemStackOrigin(), getContext());
    }
}
