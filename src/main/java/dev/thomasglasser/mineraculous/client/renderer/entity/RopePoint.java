package dev.thomasglasser.mineraculous.client.renderer.entity;

import net.minecraft.world.phys.Vec3;

public class RopePoint {
    double xP, yP; // xy coords inside fromProjectiletoHand and vertical vector plane.
    final int ID;
    final Vec3 direction;// should always be from projectile to player ON XZ.

    public RopePoint(double x, double y, int ID, Vec3 direction) {
        this.xP = x;
        this.yP = y;
        this.ID = ID;
        this.direction = new Vec3(direction.toVector3f());
    }

    public double getXP() {
        return xP;
    }

    public double getYP() {
        return this.yP;
    }

    public double getX() {
        if (direction != null) {
            double k = direction.length() / getXP();
            k = 1 / k;
            Vec3 toPoint = new Vec3(direction.scale(k).toVector3f());
            return toPoint.x;
        } else return 0;
    }

    public double getZ() {
        if (direction != null) {
            double k = direction.length() / getXP();
            k = 1 / k;
            Vec3 toPoint = new Vec3(direction.scale(k).toVector3f());
            return toPoint.z;
        } else return 0;
    }
}
