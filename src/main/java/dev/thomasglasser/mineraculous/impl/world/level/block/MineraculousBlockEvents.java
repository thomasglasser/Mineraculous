package dev.thomasglasser.mineraculous.impl.world.level.block;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.level.block.MineraculousBlocks;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionBlockData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionItemData;
import dev.thomasglasser.mineraculous.api.world.level.storage.BlockReversionData;
import dev.thomasglasser.mineraculous.api.world.level.storage.ItemReversionData;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

public class MineraculousBlockEvents {
    public static void onBlockDrops(BlockDropsEvent event) {
        if (event.getLevel() instanceof ServerLevel level) {
            UUID recoverer = BlockReversionData.get(level).getCause(level.dimension(), event.getPos());
            if (recoverer != null) {
                for (ItemEntity item : event.getDrops()) {
                    UUID id = UUID.randomUUID();
                    ItemReversionData.get(level).putRemovable(recoverer, id);
                    item.getItem().set(MineraculousDataComponents.REVERTIBLE_ITEM_ID, id);
                }
            }
        }
    }

    public static void onBlockEntityTypeAddBlocksEvent(BlockEntityTypeAddBlocksEvent event) {
        event.modify(BlockEntityType.SIGN, MineraculousBlocks.ALMOND_WOOD_SET.sign().get(), MineraculousBlocks.ALMOND_WOOD_SET.wallSign().get());
        event.modify(BlockEntityType.HANGING_SIGN, MineraculousBlocks.ALMOND_WOOD_SET.hangingSign().get(), MineraculousBlocks.ALMOND_WOOD_SET.wallHangingSign().get());
    }
}
