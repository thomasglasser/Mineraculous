package dev.thomasglasser.mineraculous.api.event;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.Nullable;

public abstract class MiraculousEvent extends LivingEvent {
    private final Holder<Miraculous> miraculous;
    private final MiraculousData miraculousData;
    private final ItemStack stack;

    public MiraculousEvent(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack) {
        super(entity);
        this.miraculous = miraculous;
        this.miraculousData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous);
        this.stack = stack;
    }

    public Holder<Miraculous> getMiraculous() {
        return miraculous;
    }

    public MiraculousData getMiraculousData() {
        return miraculousData;
    }

    public ItemStack getStack() {
        return stack;
    }

    public static class CanEquip extends MiraculousEvent {
        private final CuriosData curiosData;

        private boolean canEquip = true;

        public CanEquip(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack, CuriosData curiosData) {
            super(entity, miraculous, stack);
            this.curiosData = curiosData;
        }

        public CuriosData getCuriosData() {
            return curiosData;
        }

        public boolean canEquip() {
            return canEquip;
        }

        public void setCanEquip(boolean canEquip) {
            this.canEquip = canEquip;
        }
    }

    public static class Equip extends MiraculousEvent {
        public Equip(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack) {
            super(entity, miraculous, stack);
        }
    }

    public static class Unequip extends MiraculousEvent {
        public Unequip(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack) {
            super(entity, miraculous, stack);
        }
    }

    public static abstract class Transform extends MiraculousEvent {
        public Transform(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack) {
            super(entity, miraculous, stack);
        }

        public static class Pre extends Transform implements ICancellableEvent {
            public Pre(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack) {
                super(entity, miraculous, stack);
            }
        }

        public static class Start extends Transform {
            private Optional<Integer> transformationFrames;

            public Start(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack, Optional<Integer> transformationFrames) {
                super(entity, miraculous, stack);
                this.transformationFrames = transformationFrames;
            }

            public Optional<Integer> getTransformationFrames() {
                return transformationFrames;
            }

            public void setTransformationFrames(Optional<Integer> transformationFrames) {
                this.transformationFrames = transformationFrames;
            }
        }

        public static class Finish extends Transform {
            private boolean shouldSetLastUsed = true;

            public Finish(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack) {
                super(entity, miraculous, stack);
            }

            public boolean shouldSetLastUsed() {
                return shouldSetLastUsed;
            }

            public void setShouldSetLastUsed(boolean shouldSetLastUsed) {
                this.shouldSetLastUsed = shouldSetLastUsed;
            }
        }
    }

    public static abstract class Detransform extends MiraculousEvent {
        private final boolean removed;

        public Detransform(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack, boolean removed) {
            super(entity, miraculous, stack);
            this.removed = removed;
        }

        public boolean wasRemoved() {
            return removed;
        }

        public static class Pre extends Detransform implements ICancellableEvent {
            public Pre(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack, boolean removed) {
                super(entity, miraculous, stack, removed);
            }
        }

        public static class Start extends Detransform {
            private Optional<Integer> detransformationFrames;

            public Start(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack, Optional<Integer> detransformationFrames) {
                super(entity, miraculous, stack, false);
                this.detransformationFrames = detransformationFrames;
            }

            public Optional<Integer> getDetransformationFrames() {
                return detransformationFrames;
            }

            public void setDetransformationFrames(Optional<Integer> detransformationFrames) {
                this.detransformationFrames = detransformationFrames;
            }
        }

        public static class Finish extends Detransform {
            public Finish(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack, boolean removed) {
                super(entity, miraculous, stack, removed);
            }
        }
    }

    public static abstract class Renounce extends MiraculousEvent {
        @Nullable
        private final Kwami kwami;

        public Renounce(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack, @Nullable Kwami kwami) {
            super(entity, miraculous, stack);
            this.kwami = kwami;
        }

        @Nullable
        public Kwami getKwami() {
            return kwami;
        }

        public static class Pre extends Renounce implements ICancellableEvent {
            private boolean shouldRequireKwami;

            public Pre(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack, @Nullable Kwami kwami, boolean shouldRequireKwami) {
                super(entity, miraculous, stack, kwami);
                this.shouldRequireKwami = shouldRequireKwami;
            }

            public boolean shouldRequireKwami() {
                return shouldRequireKwami;
            }

            public void setShouldRequireKwami(boolean shouldRequireKwami) {
                this.shouldRequireKwami = shouldRequireKwami;
            }
        }

        public static class Post extends Renounce {
            public Post(LivingEntity entity, Holder<Miraculous> miraculous, ItemStack stack, @Nullable Kwami kwami) {
                super(entity, miraculous, stack, kwami);
            }
        }
    }
}
