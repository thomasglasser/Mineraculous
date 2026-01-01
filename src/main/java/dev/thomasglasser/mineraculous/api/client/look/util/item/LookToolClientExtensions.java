package dev.thomasglasser.mineraculous.api.client.look.util.item;

import com.google.common.base.Suppliers;
import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.client.look.asset.LookAssetTypes;
import dev.thomasglasser.mineraculous.api.client.look.util.renderer.LookRenderer;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.look.LookData;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * Provides item rendering data for tools using the look system.
 * 
 * @param <T> The renderer of the tool
 */
public abstract class LookToolClientExtensions<T extends BlockEntityWithoutLevelRenderer & LookRenderer> implements IClientItemExtensions {
    private final Supplier<T> renderer;

    public LookToolClientExtensions(Supplier<T> renderer) {
        this.renderer = Suppliers.memoize(renderer::get);
    }

    /**
     * Returns the default look ID for the provided {@link ItemStack}.
     * 
     * @param stack The stack providing the ID
     * @return The default look ID
     */
    public abstract ResourceLocation getDefaultLookId(ItemStack stack);

    /**
     * Returns the {@link LookContext} of the provided {@link ItemStack} to use when scoping.
     * 
     * @param stack The stack providing the context
     * @return The scoping context
     */
    public abstract Holder<LookContext> getScopeContext(ItemStack stack);

    /**
     * Returns the {@link LookData} for the provided {@link Player} and {@link ItemStack}.
     * 
     * @param entity The entity providing the look data
     * @param stack  The stack providing the look data
     * @return The look data
     */
    public abstract LookData getLookData(Entity entity, ItemStack stack);

    @Override
    public T getCustomRenderer() {
        return renderer.get();
    }

    @Override
    public ResourceLocation getScopeOverlayTexture(ItemStack stack) {
        UUID owner = stack.get(MineraculousDataComponents.OWNER);
        Level level = ClientUtils.getLevel();
        Holder<LookContext> context = getScopeContext(stack);
        ResourceLocation texture;
        if (owner != null && level != null) {
            Entity entity = level.getEntities().get(owner);
            if (entity != null) {
                texture = LookManager.getAsset(getLookData(entity, stack), context, LookAssetTypes.SCOPE_TEXTURE);
                if (texture != null)
                    return texture;
            }
        }
        texture = LookManager.getOrThrowBuiltInLook(getDefaultLookId(stack)).getAsset(context, LookAssetTypes.SCOPE_TEXTURE);
        return texture != null ? texture : IClientItemExtensions.super.getScopeOverlayTexture(stack);
    }
}
