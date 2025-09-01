package dev.thomasglasser.mineraculous.impl.world.level.block;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionBlockData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionItemData;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public class MineraculousBlockEvents {
    public static void onBlockDrops(BlockDropsEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            UUID recoverer = AbilityReversionBlockData.get(level).getCause(level.dimension(), event.getPos());
            if (recoverer != null) {
                for (ItemEntity item : event.getDrops()) {
                    UUID id = UUID.randomUUID();
                    AbilityReversionItemData.get(level).putRemovable(recoverer, id);
                    item.getItem().set(MineraculousDataComponents.REVERTIBLE_ITEM_ID, id);
                }
            }
        }
    }
}
