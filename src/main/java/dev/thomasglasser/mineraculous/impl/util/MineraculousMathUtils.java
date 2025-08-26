package dev.thomasglasser.mineraculous.impl.util;

import net.minecraft.world.entity.LivingEntity;
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

    public static Vec3 getMovementVector(LivingEntity livingEntity, boolean... inputs) {
        boolean front = inputs[0];
        boolean back = inputs[1];
        boolean left = inputs[2];
        boolean right = inputs[3];
        Vec3 movement = new Vec3(0f, 0f, 0f);
        if (livingEntity != null) {
            double yawRad = Math.toRadians(livingEntity.getYRot());
            Vec3 frontMovement = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();
            Vec3 leftMovement = new Vec3(frontMovement.z, 0, -frontMovement.x).normalize();
            Vec3 backMovement = frontMovement.scale(-1).normalize();
            Vec3 rightMovement = leftMovement.scale(-1).normalize();

            if (front) movement = movement.add(frontMovement);
            if (back) movement = movement.add(backMovement);
            if (left) movement = movement.add(leftMovement);
            if (right) movement = movement.add(rightMovement);
            movement = movement.normalize();
        }
        return movement;
    }
}
