package dev.thomasglasser.mineraculous.impl.world.item.ability;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.level.storage.TravelCatStaffData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CatStaffTravelHandler {
    public static void tick(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide) {
            TravelCatStaffData travelCatStaffData = livingEntity.getData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF);
            behaviour(stack, livingEntity, travelCatStaffData);
        }

        if (livingEntity instanceof Player player)
            if (player.getCooldowns().isOnCooldown(stack.getItem()))
                livingEntity.resetFallDistance();
    }

    public static void init(Level level, LivingEntity livingEntity) {
        if (level.isClientSide) return;

        TravelCatStaffData travelCatStaffData = livingEntity.getData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF);
        if (travelCatStaffData.traveling()) return;

        Vec3 lookAngle = livingEntity.getLookAngle().normalize();
        BlockHitResult result = level.clip(new ClipContext(
                livingEntity.getEyePosition(),
                livingEntity.getEyePosition()
                        .add(lookAngle.scale(-MineraculousServerConfig.get().maxToolLength.get())),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                livingEntity));
        BlockPos hitPos = travelCatStaffData.blockPos();

        float length = 0;
        boolean traveling;
        if (result.getType() == HitResult.Type.BLOCK) {
            hitPos = result.getBlockPos();
            traveling = true;
        } else traveling = false;

        double initRot = ((livingEntity.getYRot() % 360) + 360) % 360;

        //SAVE DATA
        TravelCatStaffData newTravelData = new TravelCatStaffData(
                length, hitPos, traveling,
                lookAngle.toVector3f(),
                (float) livingEntity.getY(),
                (float) initRot,
                travelCatStaffData.launch());
        livingEntity.setData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF, newTravelData);
        newTravelData.save(livingEntity, true);
    }

    private static void behaviour(ItemStack stack, LivingEntity livingEntity, TravelCatStaffData travelData) {
        if (!travelData.traveling()) return;

        float length = travelData.length();
        boolean didLaunch = travelData.launch();
        BlockPos targetPos = travelData.blockPos();

        // Distance from the livingEntity to target
        float targetDistance = new Vector3f(
                (float) (livingEntity.getX() - targetPos.getX()),
                (float) (livingEntity.getY() - targetPos.getY()),
                (float) (livingEntity.getZ() - targetPos.getZ())).length();

        // Gradually increasing the length until it reaches the target
        length = adjustLength(length, targetDistance);

        if (length == targetDistance && !didLaunch) {
            launchLivingEntity(stack, livingEntity, travelData);
            didLaunch = true;
        }

        if (didLaunch && livingEntity.getDeltaMovement().y < 0.5) {
            TravelCatStaffData.remove(livingEntity, true);
        } else {
            TravelCatStaffData newTravelData = new TravelCatStaffData(
                    length, targetPos, true,
                    travelData.initialLookingAngle(),
                    travelData.y(),
                    travelData.initBodAngle(), didLaunch);
            newTravelData.save(livingEntity, true);
        }
        applyCollisionDamage(livingEntity);
    }

    private static void applyCollisionDamage(LivingEntity entity) {
        Vec3 velocity = entity.getDeltaMovement();
        double horizontalSpeedBefore = velocity.horizontalDistance();

        entity.move(MoverType.SELF, velocity);

        if (entity.horizontalCollision || entity.minorHorizontalCollision) {
            double horizontalSpeedAfter = entity.getDeltaMovement().horizontalDistance();
            double lostSpeed = horizontalSpeedBefore - horizontalSpeedAfter;
            float damage = (float) (lostSpeed * 10.0 - 3.0);

            if (damage > 0.0F) {
                TravelCatStaffData.remove(entity, true);
                entity.hurt(entity.damageSources().flyIntoWall(), damage);
            }
        }
        if (entity.verticalCollision || entity.verticalCollisionBelow) TravelCatStaffData.remove(entity, true);
    }

    private static void launchLivingEntity(ItemStack stack, LivingEntity livingEntity, TravelCatStaffData travelCatStaffData) {
        livingEntity.setDeltaMovement(new Vec3(travelCatStaffData.initialLookingAngle()).normalize().scale(4));
        livingEntity.hurtMarked = true;
        if (livingEntity instanceof Player player)
            player.getCooldowns().addCooldown(stack.getItem(), 40);
    }

    private static float adjustLength(float length, float targetDistance) {
        if (length < targetDistance && length <= MineraculousServerConfig.get().maxToolLength.get()) length += 8;
        if (length > targetDistance) length = targetDistance;
        return length;
    }
}
