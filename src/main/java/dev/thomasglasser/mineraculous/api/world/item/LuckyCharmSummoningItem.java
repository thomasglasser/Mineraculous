package dev.thomasglasser.mineraculous.api.world.item;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public interface LuckyCharmSummoningItem {
    @Nullable
    Optional<Vec3> getSummonPosition(ServerLevel level, LivingEntity performer, ItemStack stack);
}
