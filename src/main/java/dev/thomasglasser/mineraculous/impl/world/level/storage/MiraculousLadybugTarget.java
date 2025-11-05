package dev.thomasglasser.mineraculous.impl.world.level.storage;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public interface MiraculousLadybugTarget {
    Vec3 position();

    MiraculousLadybugTargetType type();

    MiraculousLadybugTarget revert(ServerLevel level);

    void revertInstantly(ServerLevel level);

    boolean shouldStartRevert();
}
