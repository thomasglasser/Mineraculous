package dev.thomasglasser.mineraculous.api.world.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface LuckyCharmSummoningItem {
    @Nullable Vec3 luckyCharmSpawnPosition(ServerLevel level, LivingEntity performer);
}
