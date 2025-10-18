package dev.thomasglasser.mineraculous.impl.plugins.jade;

import dev.thomasglasser.mineraculous.impl.world.level.block.OvenBlock;
import dev.thomasglasser.mineraculous.impl.world.level.block.entity.OvenBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class MineraculousJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(OvenProvider.INSTANCE, OvenBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(OvenProvider.INSTANCE, OvenBlock.class);
    }
}
