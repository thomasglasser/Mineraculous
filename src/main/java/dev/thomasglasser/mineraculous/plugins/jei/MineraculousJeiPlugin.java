package dev.thomasglasser.mineraculous.plugins.jei;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
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
    }

    @Override
    public ResourceLocation getPluginUid() {
        return Mineraculous.modLoc("jei");
    }
}
