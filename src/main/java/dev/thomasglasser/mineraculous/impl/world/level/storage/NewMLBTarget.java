package dev.thomasglasser.mineraculous.impl.world.level.storage;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public interface NewMLBTarget {
    Vec3 getPosition();

    NewMLBTargetType type();

    boolean isReverting();

    List<Vec3> getControlPoints();

    NewMLBTarget startReversion(ServerLevel level);

    NewMLBTarget instantRevert(ServerLevel level);

    NewMLBTarget tick(ServerLevel level);

    void spawnParticles(ServerLevel level);
}
