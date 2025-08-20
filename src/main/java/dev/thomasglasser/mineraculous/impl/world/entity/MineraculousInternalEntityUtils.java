package dev.thomasglasser.mineraculous.impl.world.entity;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MineraculousInternalEntityUtils {
    public static void checkAndTrackLuckyCharmEntity(@Nullable Player player, @Nullable Entity entity, ServerLevel level, ItemStack stack) {
        if (player != null && entity != null && stack.has(MineraculousDataComponents.LUCKY_CHARM)) {
            AbilityReversionEntityData.get(level).putRemovable(player.getUUID(), entity);
        }
    }
}
