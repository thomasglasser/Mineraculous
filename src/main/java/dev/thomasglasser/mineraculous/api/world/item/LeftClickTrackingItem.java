package dev.thomasglasser.mineraculous.api.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface LeftClickTrackingItem {
    boolean onLeftClick(ItemStack stack, LivingEntity entity);
}
