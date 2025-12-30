package dev.thomasglasser.mineraculous.api.event;

import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

public abstract class ItemBreakEvent extends Event {
    private final ItemStack stack;
    private final ServerLevel level;
    private final Vec3 pos;
    @Nullable
    private final LivingEntity breaker;

    public ItemBreakEvent(ItemStack stack, ServerLevel level, Vec3 pos, @Nullable LivingEntity breaker) {
        this.stack = stack;
        this.level = level;
        this.pos = pos;
        this.breaker = breaker;
    }

    public ItemStack getStack() {
        return stack;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public Vec3 getPos() {
        return pos;
    }

    @Nullable
    public LivingEntity getBreaker() {
        return breaker;
    }

    public static class Pre extends ItemBreakEvent implements ICancellableEvent {
        public Pre(ItemStack stack, ServerLevel level, Vec3 pos, @Nullable LivingEntity breaker) {
            super(stack, level, pos, breaker);
        }
    }

    public static class DetermineDamage extends ItemBreakEvent {
        private int damage;

        public DetermineDamage(ItemStack stack, ServerLevel level, Vec3 pos, @Nullable LivingEntity breaker, int damage) {
            super(stack, level, pos, breaker);
            this.damage = damage;
        }

        public int getDamage() {
            return damage;
        }

        public void setDamage(int damage) {
            this.damage = damage;
        }
    }

    public static class Post extends ItemBreakEvent {
        private MineraculousItemUtils.BreakResult breakResult;

        public Post(ItemStack stack, ServerLevel level, Vec3 pos, @Nullable LivingEntity breaker, MineraculousItemUtils.BreakResult breakResult) {
            super(stack, level, pos, breaker);
            this.breakResult = breakResult;
        }

        public MineraculousItemUtils.BreakResult getBreakResult() {
            return breakResult;
        }

        public void setBreakResult(MineraculousItemUtils.BreakResult breakResult) {
            this.breakResult = breakResult;
        }
    }
}
