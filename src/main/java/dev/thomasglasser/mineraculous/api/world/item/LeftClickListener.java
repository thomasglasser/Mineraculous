package dev.thomasglasser.mineraculous.api.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * An item that listens for an empty left click while being held.
 * Used by {@link dev.thomasglasser.mineraculous.impl.network.ServerboundEmptyLeftClickItemPayload}.
 */
public interface LeftClickListener {
    /**
     * Called when the item is left-clicked while being held.
     *
     * @param stack  The item stack being held
     * @param entity The entity holding the item
     * @return Whether the left click was handled and should consume the interaction
     */
    boolean onLeftClick(ItemStack stack, LivingEntity entity);
}
