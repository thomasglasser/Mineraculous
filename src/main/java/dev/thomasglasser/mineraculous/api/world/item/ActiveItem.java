package dev.thomasglasser.mineraculous.api.world.item;

import dev.thomasglasser.mineraculous.impl.world.item.component.Active;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ActiveItem {
    void onToggle(ItemStack stack, @Nullable Entity holder, Active active);
}
