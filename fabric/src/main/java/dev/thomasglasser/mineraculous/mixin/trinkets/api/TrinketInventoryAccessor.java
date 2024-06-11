package dev.thomasglasser.mineraculous.mixin.trinkets.api;

import dev.emi.trinkets.api.TrinketInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrinketInventory.class)
public interface TrinketInventoryAccessor
{
	@Accessor()
	NonNullList<ItemStack> getStacks();
}