package dev.thomasglasser.mineraculous.impl.world.level.block;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.level.storage.BlockReversionData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MineraculousBlockUtils {
    public static void checkAndTrackLuckyCharmBlock(@Nullable Player player, ServerLevel level, BlockPos pos, ItemStack stack) {
        if (player != null && stack.has(MineraculousDataComponents.LUCKY_CHARM)) {
            BlockReversionData.get(level).putRevertible(player.getUUID(), level.dimension(), pos, level.getBlockState(pos));
        }
    }

    public static void spawnParticlesAtBlock(ServerLevel level, BlockPos pos, SimpleParticleType type, int particleCount) {
        Vec3 center = pos.getCenter();
        double startX = center.x;
        double startY = center.y;
        double startZ = center.z;

        level.sendParticles(
                type,
                startX,
                startY,
                startZ,
                particleCount,
                0.2,
                0.2,
                0.2,
                0.2);
    }
}
