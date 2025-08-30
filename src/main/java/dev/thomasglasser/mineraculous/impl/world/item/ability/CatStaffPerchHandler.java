package dev.thomasglasser.mineraculous.impl.world.item.ability;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.impl.network.ServerboundUpdateStaffInputPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundUpdateStaffPerchLength;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchingCatStaffData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class CatStaffPerchHandler {
    public static final float PERCH_STAFF_DISTANCE = 7f / 16f;
    public static final int MAX_TICKS = 30;
    public static final double MOVEMENT_THRESHOLD = 0.15d;
    public static final double MOVEMENT_SCALE = 0.1d;
    private static final int ANIMATION_DELAY = 10; //TODO VALUE TO BE CHANGED WHEN ANIMATIONS ADDED

    public static void tick(Level level, LivingEntity livingEntity) {
        PerchingCatStaffData perchData = livingEntity.getData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF);
        constrainMovement(livingEntity, perchData);

        if (perchData.perching()) {
            if (level.isClientSide()) {
                handleVerticalMovement(livingEntity, perchData);
                handleMovementInput(perchData);
            } else {
                updateServer(level, livingEntity, perchData);
            }
        }

        if (livingEntity instanceof Player player)
            if (player.getCooldowns().isOnCooldown(MineraculousItems.CAT_STAFF.get()))
                livingEntity.resetFallDistance();
    }

    public static void itemLeftClicked(Level level, LivingEntity livingEntity, PerchingCatStaffData perchingCatStaffData) {
        if (!level.isClientSide) {
            boolean perching = perchingCatStaffData.perching();
            if (perching) {
                Vector3f initPos = perchingCatStaffData.initPos();
                float groundRY = perchingCatStaffData.yGroundLevel();
                float length = perchingCatStaffData.length();
                boolean falling = perchingCatStaffData.isFalling();
                int t = perchingCatStaffData.tick();
                boolean nRender = perchingCatStaffData.canRender();
                float yBeforeFalling = perchingCatStaffData.yBeforeFalling();
                if (groundRY == length && t >= MAX_TICKS && !falling) {
                    double yawRad = Math.toRadians(livingEntity.getYRot());
                    Vector3f lookAngle = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize().toVector3f();
                    PerchingCatStaffData newPerchData = new PerchingCatStaffData(length, groundRY, perching, t, nRender, initPos, true, yBeforeFalling, lookAngle, false);
                    newPerchData.save(livingEntity, true);
                }
            }
        }
    }

    public static void itemUsed(Level level, LivingEntity livingEntity, PerchingCatStaffData perchingCatStaffData) {
        if (!level.isClientSide) {
            boolean perching = perchingCatStaffData.perching();
            if (perching) {
                Vector3f initPos = perchingCatStaffData.initPos();
                float groundRY = perchingCatStaffData.yGroundLevel();
                float length = perchingCatStaffData.length();
                boolean falling = perchingCatStaffData.isFalling();
                boolean canRender = perchingCatStaffData.canRender();
                float yB = perchingCatStaffData.yBeforeFalling();
                int t = perchingCatStaffData.tick();
                if (groundRY == length && t >= MAX_TICKS && !falling) {
                    livingEntity.resetFallDistance();
                    livingEntity.hurtMarked = true;
                    livingEntity.setDeltaMovement(0, -6, 0);
                    PerchingCatStaffData newPerchData = new PerchingCatStaffData(
                            length, groundRY, perching, t, canRender,
                            initPos, false, yB, new Vector3f(0, 0, 0),
                            true);
                    newPerchData.save(livingEntity, true);
                }
            } else {
                Vector4f init = PerchingCatStaffData.initialize(livingEntity);
                Vec3 direction = new Vec3(init.x, init.y, init.z);
                float initRot = init.w;
                Vector3f initPos = new Vector3f((float) direction.x, initRot, (float) direction.z);
                float length = 0f;
                boolean isFalling = false;
                perching = true;

                PerchingCatStaffData newPerchData = new PerchingCatStaffData(
                        length, 0, perching, 0, false,
                        initPos, isFalling, 0, new Vector3f(0, 0, 0),
                        false);
                newPerchData.save(livingEntity, true);
            }
        }
    }

    private static void constrainMovement(LivingEntity livingEntity, PerchingCatStaffData perchData) {
        boolean isFalling = perchData.isFalling();
        Vector3f initPos = perchData.initPos();

        if (!isFalling) {

            Vector3f staffPosition = new Vector3f(initPos);
            staffPosition = new Vector3f(staffPosition.x, 0, staffPosition.z);
            Vec3 fromPlayerToStaff = new Vec3(staffPosition.x - livingEntity.getX(), 0, staffPosition.z - livingEntity.getZ());

            if (perchData.perching()) {

                if (fromPlayerToStaff.length() < PERCH_STAFF_DISTANCE ||
                        fromPlayerToStaff.length() > PERCH_STAFF_DISTANCE) {

                    Vec3 constrain = new Vec3((double) staffPosition.x - livingEntity.getX(), 0, (double) staffPosition.z - livingEntity.getZ());
                    constrain = constrain.normalize();
                    constrain = constrain.scale(fromPlayerToStaff.length() - PERCH_STAFF_DISTANCE);
                    constrain = constrain.add(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                    livingEntity.setPos(constrain);
                }
            }
        }
    }

    private static void updateServer(Level level, LivingEntity livingEntity, PerchingCatStaffData perchData) {
        boolean isFalling = perchData.isFalling();
        boolean fastDescending = perchData.fastDescending();
        boolean perching = perchData.perching();
        float length = perchData.length();
        Vector3f initPos = perchData.initPos();
        float catStaffPerchGroundRY = perchData.yGroundLevel();

        int t = Math.min(perchData.tick() + 1, MAX_TICKS);
        boolean shouldRender = t > ANIMATION_DELAY;
        if (t > ANIMATION_DELAY && t < MAX_TICKS)
            launchPlayerInAir(livingEntity);

        if (isFalling) {
            perchFallingBehaviour(livingEntity, perchData);
        } else {
            catStaffPerchGroundRY = detectGround(level, livingEntity);
            float yBeforeFalling = (float) livingEntity.getY();

            length = adjustLength(catStaffPerchGroundRY, length);

            PerchingCatStaffData newPerchData = new PerchingCatStaffData(
                    length, catStaffPerchGroundRY, perching, t, shouldRender,
                    initPos, isFalling, yBeforeFalling, perchData.initialFallDirection(),
                    fastDescending);
            newPerchData.save(livingEntity, true);
        }

        if (fastDescending) {
            livingEntity.resetFallDistance();
            if (Math.abs(catStaffPerchGroundRY) < 1) {
                PerchingCatStaffData.remove(livingEntity, true);
            }
        }
    }

    private static void launchPlayerInAir(LivingEntity livingEntity) {
        if (livingEntity.getDeltaMovement().y >= -0.1)
            livingEntity.setDeltaMovement(0, 0.8, 0);
        else
            livingEntity.setDeltaMovement(0, -livingEntity.getDeltaMovement().y, 0);
        livingEntity.hurtMarked = true;
    }

    private static void perchFallingBehaviour(LivingEntity livingEntity, PerchingCatStaffData perchData) {
        Vector3f initPos = perchData.initPos();
        float yBeforeFalling = perchData.yBeforeFalling();
        float length = perchData.length();
        Vector3f initialFallDirection = perchData.initialFallDirection();

        Vector3f staffOrigin = new Vector3f(initPos.x, yBeforeFalling + length, initPos.z);
        Vec3 fromPlayerToStaff = new Vec3(staffOrigin.x - livingEntity.getX(), staffOrigin.y - livingEntity.getY(), staffOrigin.z - livingEntity.getZ());

        float distance = (float) fromPlayerToStaff.length();
        Vec3 towards = new Vec3(initialFallDirection);
        towards = MineraculousMathUtils.projectOnCircle(fromPlayerToStaff, towards);
        towards = towards.normalize().scale(2);

        if (distance <= -length) {
            Vec3 constrain = fromPlayerToStaff.normalize().scale(distance + length);
            livingEntity.move(MoverType.SELF, constrain);
            livingEntity.setDeltaMovement(towards);
            livingEntity.hurtMarked = true;
        }

        applyCollisionDamage(livingEntity);
        livingEntity.resetFallDistance();

        if (distance > -length + 2 || staffOrigin.y + 0.2 > livingEntity.getY()) {
            PerchingCatStaffData.remove(livingEntity, true);
            if (livingEntity instanceof Player player) {
                player.getCooldowns().addCooldown(MineraculousItems.CAT_STAFF.get(), 10);
            }
        }
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
                PerchingCatStaffData.remove(entity, true);
                entity.hurt(entity.damageSources().flyIntoWall(), damage);
            }
        }
    }

    private static float detectGround(Level level, LivingEntity livingEntity) {
        float toReturn;
        int y = livingEntity.getBlockY();
        while (level.getBlockState(new BlockPos(livingEntity.getBlockX(), y, livingEntity.getBlockZ())).isEmpty() && Math.abs(livingEntity.getBlockY() - y) <= MineraculousServerConfig.get().maxToolLength.get()) {
            y--;
        }
        y++;
        toReturn = (float) y - (float) livingEntity.getY();
        return toReturn;
    }

    private static float adjustLength(float catStaffPerchGroundRY, float length) {
        if (catStaffPerchGroundRY < length) {
            length = length - 1f;
        }
        if (catStaffPerchGroundRY > length) {
            length = catStaffPerchGroundRY;
        }
        return length;
    }

    private static void handleVerticalMovement(LivingEntity livingEntity, PerchingCatStaffData perchData) {
        boolean isFalling = perchData.isFalling();
        boolean fastDescending = perchData.fastDescending();
        float groundRYClient = perchData.yGroundLevel();
        float length = perchData.length();
        int perchTickClient = perchData.tick();
        if (!isFalling && !fastDescending) {
            float d = 0;
            boolean upOrDownKeyPressed = false;
            boolean shouldNotFall = (groundRYClient == length);
            if (MineraculousKeyMappings.DESCEND_TOOL.isDown()) {
                d -= 0.3f;
                upOrDownKeyPressed = true;
            }
            if (MineraculousKeyMappings.ASCEND_TOOL.isDown() && Math.abs(groundRYClient) < MineraculousServerConfig.get().maxToolLength.get()) {
                d += 0.3f;
                upOrDownKeyPressed = true;
            }
            if (perchTickClient >= MAX_TICKS) {
                if (!upOrDownKeyPressed && shouldNotFall) d = 0;
                Vec3 vec3 = new Vec3(livingEntity.getDeltaMovement().x, d, livingEntity.getDeltaMovement().z);
                livingEntity.setDeltaMovement(vec3);
            }
            TommyLibServices.NETWORK.sendToServer(new ServerboundUpdateStaffPerchLength(perchData, MineraculousKeyMappings.ASCEND_TOOL.isDown(), MineraculousKeyMappings.DESCEND_TOOL.isDown()));
        }
    }

    private static void handleMovementInput(PerchingCatStaffData perchData) {
        MineraculousClientUtils.InputState input = MineraculousClientUtils.captureInput();
        int packedInput = input.packInputs();
        TommyLibServices.NETWORK.sendToServer(new ServerboundUpdateStaffInputPayload(packedInput, perchData));
    }
}
