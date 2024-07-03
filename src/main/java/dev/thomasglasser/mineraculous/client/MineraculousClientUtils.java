package dev.thomasglasser.mineraculous.client;

import dev.thomasglasser.mineraculous.client.renderer.MineraculousBlockEntityWithoutLevelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MineraculousClientUtils {
    private static final MineraculousBlockEntityWithoutLevelRenderer bewlr = new MineraculousBlockEntityWithoutLevelRenderer();

    public static MineraculousBlockEntityWithoutLevelRenderer getBewlr() {
        return bewlr;
    }

    public static void setShader(@Nullable ResourceLocation location) {
        PostChain postChain = Minecraft.getInstance().gameRenderer.currentEffect();
        if (location != null)
            Minecraft.getInstance().gameRenderer.loadEffect(location);
        else if (postChain != null)
            postChain.close();
    }

    public static boolean isFirstPerson() {
        return Minecraft.getInstance().options.getCameraType().isFirstPerson();
    }

    public static void renderParticlesFollowingEntity(LivingEntity entity, ParticleOptions type, double distanceFromSkin, double forwardShift, double rightShift, double upShift, float scale, boolean firstPerson) {
        Vec3 angle = firstPerson ? entity.getLookAngle() : new Vec3(0, 0, 0);
        Vec3 worldUp = new Vec3(0, 1, 0);
        Vec3 localForward = Vec3.directionFromRotation(new Vec2(0, firstPerson ? entity.getYRot() : entity.yBodyRot));
        Vec3 right = localForward.cross(worldUp);
        Vec3 up = localForward.cross(right);

        double x = entity.getX() + (rightShift * right.x()) - (upShift * up.x());
        double y = entity.getY() + (rightShift * right.y()) - (upShift * up.y());
        double z = entity.getZ() + (rightShift * right.z()) - (upShift * up.z());

        if (firstPerson) {
            x += (distanceFromSkin * angle.x());
            y += entity.getEyeHeight() + (distanceFromSkin * angle.y());
            z += (distanceFromSkin * angle.z());
        } else {
            Vec3 forward = localForward.scale(forwardShift);
            x += forward.x();
            y += forward.y();
            z += forward.z();
        }

        entity.level().addParticle(type, x, y, z, 0.0D, 0.0D, 0.0D);
    }

    public static boolean hasNoScreenOpen() {
        return Minecraft.getInstance().screen == null;
    }

    @Nullable
    public static Entity getLookEntity() {
        return Minecraft.getInstance().crosshairPickEntity;
    }

    public static void init() {}
}
