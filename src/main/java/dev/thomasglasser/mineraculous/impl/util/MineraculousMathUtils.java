package dev.thomasglasser.mineraculous.impl.util;

import net.minecraft.world.phys.Vec3;

public class MineraculousMathUtils {
    public static Vec3 projectOnCircle(Vec3 fromPointToCenter, Vec3 vec3) {
        Vec3 crossProd = fromPointToCenter.cross(vec3);
        Vec3 t = crossProd.cross(fromPointToCenter);

        double cosTheta = t.dot(vec3) / (t.length() * vec3.length());
        double tln = cosTheta * vec3.length();
        t = t.normalize().scale(tln);

        return t;
    }

    public static double normalizeDegreesLikeEntityRot(double degrees) {
        degrees = degrees % 360;
        if (degrees > 180)
            degrees = degrees - 360;
        if (degrees < -180)
            degrees = degrees + 360;
        return degrees;
    }
}
