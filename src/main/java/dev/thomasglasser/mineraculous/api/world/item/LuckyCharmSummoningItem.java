package dev.thomasglasser.mineraculous.api.world.item;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/// Allows special handling for the {@link dev.thomasglasser.mineraculous.api.world.ability.SummonTargetDependentLuckyCharmAbility} active tool.
public interface LuckyCharmSummoningItem {
    /**
     * Returns the position to summon the lucky charm at.
     *
     * @param level     The level the ability is being performed in
     * @param performer The performer of the ability
     * @param stack     The stack used to summon the lucky charm
     * @return The position to summon the lucky charm at, {@link Optional#empty()} if the default position should be used, or {@code null} to cancel the ability
     */
    @Nullable
    Optional<Vec3> getSummonPosition(ServerLevel level, LivingEntity performer, ItemStack stack);
}
