package dev.thomasglasser.mineraculous.client.renderer.entity;

import net.minecraft.world.phys.Vec3;

public record RopePoint(double xP, double yP, int id, Vec3 direction) {
    public double getX() {
        if (direction != null) {
            double k = direction.length() / xP();
            k = 1 / k;
            Vec3 toPoint = new Vec3(direction.scale(k).toVector3f());
            return toPoint.x;
        } else return 0;
    }

    public double getZ() {
        if (direction != null) {
            double k = direction.length() / xP();
            k = 1 / k;
            Vec3 toPoint = new Vec3(direction.scale(k).toVector3f());
            return toPoint.z;
        } else return 0;
    }
}
