package dev.thomasglasser.mineraculous.impl.world.level.block;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionBlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MineraculousBlockUtils {
    public static void checkAndTrackLuckyCharmBlock(@Nullable Player player, ServerLevel level, BlockPos pos, ItemStack stack) {
        if (player != null && stack.has(MineraculousDataComponents.LUCKY_CHARM)) {
            AbilityReversionBlockData.get(level).putRevertible(player.getUUID(), level.dimension(), pos, level.getBlockState(pos));
        }
    }
}
