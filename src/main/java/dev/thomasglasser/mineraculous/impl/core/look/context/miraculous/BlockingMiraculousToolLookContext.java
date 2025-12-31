package dev.thomasglasser.mineraculous.impl.core.look.context.miraculous;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;

public class BlockingMiraculousToolLookContext extends MiraculousToolLookContext {
    @Override
    public void preparePreview(Player player, Holder<?> selected) {
        super.preparePreview(player, selected);
        player.getMainHandItem().set(MineraculousDataComponents.BLOCKING, Unit.INSTANCE);
    }
}
