package dev.thomasglasser.mineraculous.api.event;

import dev.thomasglasser.mineraculous.api.datamaps.LuckyCharms;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when Lucky Charm-related events occur.
 * See the subclasses for listening for spawn position, target, and lucky charms determination.
 *
 * @see DetermineSpawnPos
 * @see DetermineTarget
 * @see DetermineLuckyCharms
 */
public abstract class LuckyCharmEvent extends LivingEvent {
    @Nullable
    private final ItemStack tool;

    @ApiStatus.Internal
    public LuckyCharmEvent(LivingEntity entity, @Nullable ItemStack tool) {
        super(entity);
        this.tool = tool;
    }

    /**
     * Returns the tool used to summon the lucky charm.
     * 
     * @return The tool used to summon the lucky charm
     */
    public @Nullable ItemStack getTool() {
        return tool;
    }

    /**
     * Fired to determine the spawn position of the lucky charm.
     *
     * <p>This event is {@linkplain ICancellableEvent cancellable}.
     * If this event is cancelled, then the lucky charm will not spawn.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
     * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
     */
    public static class DetermineSpawnPos extends LuckyCharmEvent implements ICancellableEvent {
        private Vec3 spawnPos;

        @ApiStatus.Internal
        public DetermineSpawnPos(LivingEntity entity, @Nullable ItemStack tool, Vec3 spawnPos) {
            super(entity, tool);
            this.spawnPos = spawnPos;
        }

        /**
         * Returns the spawn position of the lucky charm.
         * 
         * @return The spawn position of the lucky charm
         */
        public Vec3 getSpawnPos() {
            return spawnPos;
        }

        /**
         * Sets the spawn position of the lucky charm.
         * 
         * @param spawnPos The spawn position of the lucky charm
         */
        public void setSpawnPos(Vec3 spawnPos) {
            this.spawnPos = spawnPos;
        }
    }

    /**
     * Fired to determine the target of the lucky charm.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
     * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
     */
    public static class DetermineTarget extends LuckyCharmEvent {
        @Nullable
        private Entity target;

        @ApiStatus.Internal
        public DetermineTarget(LivingEntity entity, @Nullable ItemStack tool, @Nullable Entity target) {
            super(entity, tool);
            this.target = target;
        }

        /**
         * Returns the target of the lucky charm.
         * 
         * @return The target of the lucky charm
         */
        public @Nullable Entity getTarget() {
            return target;
        }

        /**
         * Sets the target of the lucky charm.
         * 
         * @param target The target of the lucky charm
         */
        public void setTarget(@Nullable Entity target) {
            this.target = target;
        }
    }

    /**
     * Fired to determine the lucky charms to be summoned.
     *
     * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
     *
     * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
     * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
     */
    public static class DetermineLuckyCharms extends LuckyCharmEvent {
        @Nullable
        private final Entity target;
        private LuckyCharms luckyCharms;

        @ApiStatus.Internal
        public DetermineLuckyCharms(LivingEntity entity, @Nullable ItemStack tool, @Nullable Entity target, LuckyCharms luckyCharms) {
            super(entity, tool);
            this.target = target;
            this.luckyCharms = luckyCharms;
        }

        /**
         * Returns the target of the lucky charm.
         * 
         * @return The target of the lucky charm
         */
        public @Nullable Entity getTarget() {
            return target;
        }

        /**
         * Returns the lucky charms to be summoned.
         * 
         * @return The lucky charms to be summoned
         */
        public LuckyCharms getLuckyCharms() {
            return luckyCharms;
        }

        /**
         * Sets the lucky charms to be summoned.
         * 
         * @param luckyCharms The lucky charms to be summoned
         */
        public void setLuckyCharms(LuckyCharms luckyCharms) {
            this.luckyCharms = luckyCharms;
        }
    }
}
