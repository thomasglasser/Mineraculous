package dev.thomasglasser.mineraculous.impl.plugins.jei;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class MineraculousJeiPlugin implements IModPlugin {
    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, MineraculousItems.MIRACULOUS.get(), MiraculousSubtypeInterpreter.INSTANCE);
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, MineraculousItems.KWAMI.get(), MiraculousSubtypeInterpreter.INSTANCE);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return MineraculousConstants.modLoc("jei");
    }
}
