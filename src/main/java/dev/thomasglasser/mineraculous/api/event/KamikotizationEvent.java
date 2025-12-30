package dev.thomasglasser.mineraculous.api.event;

import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.Nullable;

public abstract class KamikotizationEvent extends LivingEvent {
    private final Optional<KamikotizationData> kamikotizationData;
    @Nullable
    private final ItemStack stack;

    public KamikotizationEvent(LivingEntity entity, Optional<KamikotizationData> kamikotizationData, @Nullable ItemStack stack) {
        super(entity);
        this.kamikotizationData = kamikotizationData;
        this.stack = stack;
    }

    public Optional<KamikotizationData> getKamikotizationData() {
        return kamikotizationData;
    }

    public @Nullable ItemStack getStack() {
        return stack;
    }

    public static abstract class Transform extends KamikotizationEvent {
        public Transform(LivingEntity entity, KamikotizationData kamikotizationData, ItemStack stack) {
            super(entity, Optional.of(kamikotizationData), stack);
        }

        public static class Pre extends Transform implements ICancellableEvent {
            public Pre(LivingEntity entity, KamikotizationData kamikotizationData, ItemStack stack) {
                super(entity, kamikotizationData, stack);
            }
        }

        public static class Start extends Transform {
            private Optional<Integer> transformationFrames;

            public Start(LivingEntity entity, KamikotizationData kamikotizationData, ItemStack stack, Optional<Integer> transformationFrames) {
                super(entity, kamikotizationData, stack);
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
            public Finish(LivingEntity entity, KamikotizationData kamikotizationData, ItemStack stack) {
                super(entity, kamikotizationData, stack);
            }
        }
    }

    public static abstract class Detransform extends KamikotizationEvent {
        public Detransform(LivingEntity entity, Optional<KamikotizationData> kamikotizationData, ItemStack stack) {
            super(entity, kamikotizationData, stack);
        }

        public static class Pre extends Detransform implements ICancellableEvent {
            public Pre(LivingEntity entity, KamikotizationData kamikotizationData, ItemStack stack) {
                super(entity, Optional.of(kamikotizationData), stack);
            }
        }

        public static class Start extends Detransform {
            private Optional<Integer> detransformationFrames;

            public Start(LivingEntity entity, KamikotizationData kamikotizationData, ItemStack stack, Optional<Integer> detransformationFrames) {
                super(entity, Optional.of(kamikotizationData), stack);
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
            public Finish(LivingEntity entity, ItemStack stack) {
                super(entity, Optional.empty(), stack);
            }
        }
    }
}
