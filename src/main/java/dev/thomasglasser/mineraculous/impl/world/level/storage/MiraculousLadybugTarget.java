package dev.thomasglasser.mineraculous.impl.world.level.storage;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public interface MiraculousLadybugTarget {
    Vec3 getPosition();

    MiraculousLadybugTargetType type();

    boolean isReverting();

    List<Vec3> getControlPoints();

    MiraculousLadybugTarget startReversion(ServerLevel level);

    MiraculousLadybugTarget instantRevert(ServerLevel level);

    MiraculousLadybugTarget tick(ServerLevel level);

    void spawnParticles(ServerLevel level);
}
