package dev.thomasglasser.mineraculous.api.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public abstract class StealEvent extends PlayerEvent {
    private final Player target;

    public StealEvent(Player player, Player target) {
        super(player);
        this.target = target;
    }

    public Player getTarget() {
        return target;
    }

    public abstract static class Start extends StealEvent implements ICancellableEvent {
        private int takeTicks;
        private int maxTakeTicks;

        public Start(Player player, Player target, int takeTicks, int maxTakeTicks) {
            super(player, target);
            this.takeTicks = takeTicks;
            this.maxTakeTicks = maxTakeTicks;
        }

        public int getTakeTicks() {
            return takeTicks;
        }

        public int getMaxTakeTicks() {
            return maxTakeTicks;
        }

        public void setTakeTicks(int takeTicks) {
            this.takeTicks = takeTicks;
        }

        public void setMaxTakeTicks(int maxTakeTicks) {
            this.maxTakeTicks = maxTakeTicks;
        }

        public static class Pre extends Start {
            public Pre(Player player, Player target, int takeTicks, int maxTakeTicks) {
                super(player, target, takeTicks, maxTakeTicks);
            }
        }

        public static class Tick extends Start {
            public Tick(Player player, Player target, int takeTicks, int maxTakeTicks) {
                super(player, target, takeTicks, maxTakeTicks);
            }
        }

        public static class Post extends Start {
            public Post(Player player, Player target, int takeTicks, int maxTakeTicks) {
                super(player, target, takeTicks, maxTakeTicks);
            }
        }
    }

    public static class Finish extends StealEvent implements ICancellableEvent {
        private final ItemStack stack;

        public Finish(Player player, Player target, ItemStack stack) {
            super(player, target);
            this.stack = stack;
        }

        public ItemStack getStack() {
            return stack;
        }
    }
}
