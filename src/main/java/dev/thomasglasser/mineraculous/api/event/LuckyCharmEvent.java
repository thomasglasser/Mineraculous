package dev.thomasglasser.mineraculous.api.event;

import dev.thomasglasser.mineraculous.api.datamaps.LuckyCharms;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.Nullable;

public abstract class LuckyCharmEvent extends LivingEvent {
    @Nullable
    private final ItemStack tool;

    public LuckyCharmEvent(LivingEntity entity, @Nullable ItemStack tool) {
        super(entity);
        this.tool = tool;
    }

    public @Nullable ItemStack getTool() {
        return tool;
    }

    public static class DetermineSpawnPos extends LuckyCharmEvent implements ICancellableEvent {
        private Vec3 spawnPos;

        public DetermineSpawnPos(LivingEntity entity, @Nullable ItemStack tool, Vec3 spawnPos) {
            super(entity, tool);
            this.spawnPos = spawnPos;
        }

        public Vec3 getSpawnPos() {
            return spawnPos;
        }

        public void setSpawnPos(Vec3 spawnPos) {
            this.spawnPos = spawnPos;
        }
    }

    public static class DetermineTarget extends LuckyCharmEvent {
        @Nullable
        private Entity target;

        public DetermineTarget(LivingEntity entity, @Nullable ItemStack tool, @Nullable Entity target) {
            super(entity, tool);
            this.target = target;
        }

        public @Nullable Entity getTarget() {
            return target;
        }

        public void setTarget(@Nullable Entity target) {
            this.target = target;
        }
    }

    public static class DetermineLuckyCharms extends LuckyCharmEvent {
        @Nullable
        private final Entity target;
        private LuckyCharms luckyCharms;

        public DetermineLuckyCharms(LivingEntity entity, @Nullable ItemStack tool, @Nullable Entity target, LuckyCharms luckyCharms) {
            super(entity, tool);
            this.target = target;
            this.luckyCharms = luckyCharms;
        }

        public @Nullable Entity getTarget() {
            return target;
        }

        public LuckyCharms getLuckyCharms() {
            return luckyCharms;
        }

        public void setLuckyCharms(LuckyCharms luckyCharms) {
            this.luckyCharms = luckyCharms;
        }
    }
}
