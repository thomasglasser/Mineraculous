package dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget;

import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface MiraculousLadybugTargetCollector {
    static MiraculousLadybugTargetCollector of(BiConsumer<ResourceKey<Level>, MiraculousLadybugTarget<?>> nonClusterable, BiConsumer<ResourceKey<Level>, MiraculousLadybugTarget<?>> clusterable) {
        return new MiraculousLadybugTargetCollector() {
            @Override
            public void put(ResourceKey<Level> dimension, MiraculousLadybugTarget<?> target) {
                nonClusterable.accept(dimension, target);
            }

            @Override
            public void putClusterable(ResourceKey<Level> dimension, MiraculousLadybugTarget<?> target) {
                clusterable.accept(dimension, target);
            }
        };
    }

    void put(ResourceKey<Level> dimension, MiraculousLadybugTarget<?> target);

    void putClusterable(ResourceKey<Level> dimension, MiraculousLadybugTarget<?> target);
}
