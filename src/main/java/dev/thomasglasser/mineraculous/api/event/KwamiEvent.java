package dev.thomasglasser.mineraculous.api.event;

import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class KwamiEvent extends Event {
    private final Kwami kwami;

    public KwamiEvent(Kwami kwami) {
        this.kwami = kwami;
    }

    public Kwami getKwami() {
        return kwami;
    }

    public abstract static class Eat extends KwamiEvent {
        private final ItemStack stack;

        public Eat(Kwami kwami, ItemStack stack) {
            super(kwami);
            this.stack = stack;
        }

        public ItemStack getStack() {
            return stack;
        }

        public static class Start extends Eat implements ICancellableEvent {
            private int eatTicks;

            public Start(Kwami kwami, ItemStack stack, int eatTicks) {
                super(kwami, stack);
                this.eatTicks = eatTicks;
            }

            public int getEatTicks() {
                return eatTicks;
            }

            public void setEatTicks(int eatTicks) {
                this.eatTicks = eatTicks;
            }
        }

        public abstract static class Finish extends Eat {
            public Finish(Kwami kwami, ItemStack stack) {
                super(kwami, stack);
            }

            public static class Pre extends Finish implements ICancellableEvent {
                public Pre(Kwami kwami, ItemStack stack) {
                    super(kwami, stack);
                }
            }

            public static class Post extends Finish {
                private boolean charged;

                public Post(Kwami kwami, ItemStack stack, boolean charged) {
                    super(kwami, stack);
                    this.charged = charged;
                }

                public boolean isCharged() {
                    return charged;
                }

                public void setCharged(boolean charged) {
                    this.charged = charged;
                }
            }
        }
    }
}
