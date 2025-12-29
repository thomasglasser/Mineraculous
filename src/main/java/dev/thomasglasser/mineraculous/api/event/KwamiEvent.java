package dev.thomasglasser.mineraculous.api.event;

import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
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
        public Eat(Kwami kwami) {
            super(kwami);
        }

        public static class Start extends Eat implements ICancellableEvent {
            private int eatTicks;

            public Start(Kwami kwami, int eatTicks) {
                super(kwami);
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
            public Finish(Kwami kwami) {
                super(kwami);
            }

            public static class Pre extends Finish implements ICancellableEvent {
                public Pre(Kwami kwami) {
                    super(kwami);
                }
            }

            public static class Post extends Finish {
                private boolean charged;

                public Post(Kwami kwami, boolean charged) {
                    super(kwami);
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
