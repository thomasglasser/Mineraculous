package dev.thomasglasser.mineraculous.api.world.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ActiveItem {
    void onToggle(ItemStack stack, @Nullable Entity holder, boolean active);
}
