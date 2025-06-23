package dev.thomasglasser.mineraculous.impl.plugins.jei;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MiraculousSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
    public static final MiraculousSubtypeInterpreter INSTANCE = new MiraculousSubtypeInterpreter();

    private MiraculousSubtypeInterpreter() {}

    @Override
    @Nullable
    public Object getSubtypeData(ItemStack ingredient, UidContext context) {
        return ingredient.get(MineraculousDataComponents.MIRACULOUS);
    }

    @Override
    public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
        return "";
    }
}
