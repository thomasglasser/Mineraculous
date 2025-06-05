package dev.thomasglasser.mineraculous.client.renderer.entity;

import net.minecraft.world.phys.Vec3;

public class RopePoint {
    private final double localX;
    private final double localY;
    private final Vec3 direction;
    private final Vec3 worldPos;

    public RopePoint(double localX, double localY, Vec3 direction) {
        this.localX = localX;
        this.localY = localY;
        this.direction = direction;
        this.worldPos = calculateWorldPos();
    }

    public double localX() {
        return localX;
    }

    public double localY() {
        return localY;
    }

    public Vec3 direction() {
        return direction;
    }

    public Vec3 worldPos() {
        return worldPos;
    }

    public double worldX() {
        return worldPos.x;
    }

    public double worldZ() {
        return worldPos.z;
    }

    private Vec3 calculateWorldPos() {
        double k = direction.length() / localX;
        k = 1 / k;
        return direction.scale(k);
    }
}
