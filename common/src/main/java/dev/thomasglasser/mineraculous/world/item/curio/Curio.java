package dev.thomasglasser.mineraculous.world.item.curio;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface Curio
{
	default void tick(ItemStack stack, CuriosData curiosData, LivingEntity entity) {}

	default void onEquip(ItemStack stack, CuriosData curiosData, LivingEntity entity) {}

	default void onUnequip(ItemStack oldStack, ItemStack newStack, CuriosData curiosData, LivingEntity entity) {}
}
