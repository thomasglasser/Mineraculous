package dev.thomasglasser.mineraculous.impl.world.inventory;

import net.minecraft.world.inventory.RecipeBookType;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public class MineraculousRecipeBookTypes {
    public static final EnumProxy<RecipeBookType> OVEN = new EnumProxy<>(RecipeBookType.class);
}
