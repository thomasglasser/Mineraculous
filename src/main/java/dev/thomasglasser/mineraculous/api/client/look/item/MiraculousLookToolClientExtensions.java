package dev.thomasglasser.mineraculous.api.client.look.item;

import dev.thomasglasser.mineraculous.api.client.look.renderer.LookRenderer;
import dev.thomasglasser.mineraculous.api.client.look.renderer.MiraculousToolLookRenderer;
import dev.thomasglasser.mineraculous.api.core.look.LookData;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContexts;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.MiraculousItemRenderer;
import java.util.function.Supplier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Provides item rendering data for miraculous tools.
 * 
 * @param <T> The renderer of the tool
 */
public class MiraculousLookToolClientExtensions<T extends BlockEntityWithoutLevelRenderer & LookRenderer> extends LookToolClientExtensions<T> {
    public MiraculousLookToolClientExtensions(Supplier<T> renderer) {
        super(renderer);
    }

    @Override
    public ResourceLocation getDefaultLookId(ItemStack stack) {
        return MiraculousToolLookRenderer.getDefaultLookId(stack);
    }

    @Override
    public Holder<LookContext> getScopeContext(ItemStack stack) {
        return LookContexts.MIRACULOUS_TOOL_SPYGLASS;
    }

    @Override
    public LookData getLookData(Player player, ItemStack stack) {
        return player.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(MiraculousItemRenderer.getMiraculousOrDefault(stack)).lookData();
    }
}
