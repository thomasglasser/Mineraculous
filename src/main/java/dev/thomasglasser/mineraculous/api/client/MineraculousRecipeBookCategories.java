package dev.thomasglasser.mineraculous.api.client;

import com.google.common.collect.ImmutableList;
import java.util.function.Supplier;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.Items;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public class MineraculousRecipeBookCategories {
    public static final EnumProxy<RecipeBookCategories> OVEN_SEARCH = new EnumProxy<>(RecipeBookCategories.class, (Supplier<Object>) () -> ImmutableList.of(Items.COMPASS.getDefaultInstance()));
    public static final EnumProxy<RecipeBookCategories> OVEN_FOOD = new EnumProxy<>(RecipeBookCategories.class, (Supplier<Object>) () -> ImmutableList.of(Items.PORKCHOP.getDefaultInstance()));
}
