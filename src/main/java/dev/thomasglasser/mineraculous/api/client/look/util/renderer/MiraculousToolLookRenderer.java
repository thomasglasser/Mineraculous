package dev.thomasglasser.mineraculous.api.client.look.util.renderer;

import dev.thomasglasser.mineraculous.api.client.look.Look;
import dev.thomasglasser.mineraculous.api.core.look.LookUtils;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.MiraculousItemRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/// Represents a renderer for a miraculous tool that uses the look system.
public interface MiraculousToolLookRenderer extends LookRenderer {
    ItemStack getCurrentItemStack();

    static ResourceLocation getDefaultLookId(ItemStack stack) {
        return LookUtils.getDefaultLookId(stack.getItemHolder().getKey());
    }

    @Override
    default ResourceLocation getDefaultLookId() {
        return getDefaultLookId(getCurrentItemStack());
    }

    @Override
    default Holder<LookContext> getContext() {
        return LookContexts.MIRACULOUS_TOOL;
    }

    @Override
    default @Nullable Look<?> getLook() {
        return MiraculousItemRenderer.getLook(getCurrentItemStack(), getContext());
    }
}
