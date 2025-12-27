package dev.thomasglasser.mineraculous.api.client.look.util.item;

import dev.thomasglasser.mineraculous.api.client.look.util.renderer.LookRenderer;
import dev.thomasglasser.mineraculous.api.client.look.util.renderer.MiraculousToolLookRenderer;
import dev.thomasglasser.mineraculous.api.core.look.LookData;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import java.util.function.Supplier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/**
 * Provides item rendering data for kamikotization tools.
 * 
 * @param <T> The renderer of the tool
 */
public class KamikotizationLookToolClientExtensions<T extends BlockEntityWithoutLevelRenderer & LookRenderer> extends LookToolClientExtensions<T> {
    public KamikotizationLookToolClientExtensions(Supplier<T> renderer) {
        super(renderer);
    }

    @Override
    public ResourceLocation getDefaultLookId(ItemStack stack) {
        return MiraculousToolLookRenderer.getDefaultLookId(stack);
    }

    @Override
    public Holder<LookContext> getScopeContext(ItemStack stack) {
        return LookContexts.SPYGLASS_KAMIKOTIZATION_TOOL;
    }

    @Override
    public LookData getLookData(Entity entity, ItemStack stack) {
        return entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).map(KamikotizationData::lookData).orElse(LookData.DEFAULT);
    }
}
