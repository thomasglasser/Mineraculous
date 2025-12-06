package dev.thomasglasser.mineraculous.api.world.item;

import dev.thomasglasser.mineraculous.impl.world.item.component.Active;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/// An item with an {@link Active} component for special behavior handling.
public interface ActiveItem {
    /**
     * Called when the active state of the item changes.
     *
     * @param stack  The stack being toggled
     * @param holder The entity holding the stack, or null if not held by an entity
     * @param active The new active state of the item
     */
    void onToggle(ItemStack stack, @Nullable Entity holder, Active active);
}
