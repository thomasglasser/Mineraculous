package dev.thomasglasser.mineraculous.impl.world.item.ability;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetDeltaMovementPayload;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchCatStaffData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class CatStaffPerchHandler {
    public static float PERCH_STAFF_DISTANCE = 7f / 16f;
    private static final int MAX_TICKS = 30;
    private static final int ANIMATION_DELAY = 10; //TODO VALUE TO BE CHANGED WHEN ANIMATIONS ADDED
    private static final double MOVEMENT_THRESHOLD = 0.15d;
    private static final double MOVEMENT_SCALE = 0.1d;

    public void tick(ItemStack stack, Level level, Player player) {
        PerchCatStaffData perchData = player.getData(MineraculousAttachmentTypes.PERCH_CAT_STAFF);
        constrainMovement(player, perchData);

        if (perchData.perching()) {
            if (level.isClientSide()) {
                handleVerticalMovement(player, perchData);
                handleMovementInput(player, perchData);
            } else {
                updateServer(stack, level, player, perchData);
            }
        }
    }

    public static void itemUsed(Level level, Player player, PerchCatStaffData perchCatStaffData) {
        if (!level.isClientSide) {
            boolean perching = perchCatStaffData.perching();
            if (perching) {
                Vector3f initPos = perchCatStaffData.initPos();
                float groundRY = perchCatStaffData.yGroundLevel();
                float length = perchCatStaffData.length();
                boolean falling = perchCatStaffData.isFalling();
                int t = perchCatStaffData.tick();
                boolean nRender = perchCatStaffData.canRender();
                float yBeforeFalling = perchCatStaffData.yBeforeFalling();
                if (groundRY == length && t >= MAX_TICKS && !falling) {
                    if (!level.isClientSide) {
                        Vector3f lookAngle = new Vector3f((float) player.getLookAngle().x, 0f, (float) player.getLookAngle().z);
                        PerchCatStaffData newPerchData = new PerchCatStaffData(length, groundRY, perching, t, nRender, initPos, true, yBeforeFalling, lookAngle);
                        player.setData(MineraculousAttachmentTypes.PERCH_CAT_STAFF, newPerchData);
                        newPerchData.save(player, true);
                    }
                }
            } else {
                Vector4f init = PerchCatStaffData.initialize(player);
                Vec3 direction = new Vec3(init.x, init.y, init.z);
                float initRot = init.w;
                Vector3f initPos = new Vector3f((float) direction.x, initRot, (float) direction.z);
                float length = 0f;
                boolean isFalling = false;
                perching = true;

                PerchCatStaffData newPerchData = new PerchCatStaffData(
                        length, 0, perching, 0, false,
                        initPos, isFalling, 0, new Vector3f(0, 0, 0));
                newPerchData.save(player, true);
            }
        }
    }

    private void constrainMovement(Player player, PerchCatStaffData perchData) {
        boolean isFalling = perchData.isFalling();
        Vector3f initPos = perchData.initPos();

        if (!isFalling) {

            Vector3f staffPosition = new Vector3f(initPos);
            staffPosition = new Vector3f(staffPosition.x, 0, staffPosition.z);
            Vec3 fromPlayerToStaff = new Vec3(staffPosition.x - player.getX(), 0, staffPosition.z - player.getZ());

            if (perchData.perching()) {

                if (fromPlayerToStaff.length() < PERCH_STAFF_DISTANCE ||
                        fromPlayerToStaff.length() > PERCH_STAFF_DISTANCE) {

                    Vec3 constrain = new Vec3((double) staffPosition.x - player.getX(), 0, (double) staffPosition.z - player.getZ());
                    constrain = constrain.normalize();
                    constrain = constrain.scale(fromPlayerToStaff.length() - PERCH_STAFF_DISTANCE);
                    constrain = constrain.add(player.getX(), player.getY(), player.getZ());
                    player.setPos(constrain);
                }
            }
        }
    }

    private void updateServer(ItemStack stack, Level level, Player player, PerchCatStaffData perchData) {
        boolean isFalling = perchData.isFalling();
        boolean perching = perchData.perching();
        float length = perchData.length();
        Vector3f initPos = perchData.initPos();
        float catStaffPerchGroundRY;

        int t = Math.min(perchData.tick() + 1, MAX_TICKS);
        boolean shouldRender = t > ANIMATION_DELAY;
        if (t > ANIMATION_DELAY && t < MAX_TICKS) launchPlayerInAir(player);

        if (isFalling) {
            perchFallingBehaviour(stack, player, perchData);
        } else {
            catStaffPerchGroundRY = detectGround(level, player);
            float yBeforeFalling = (float) player.getY();

            length = adjustLength(catStaffPerchGroundRY, length);

            PerchCatStaffData newPerchData = new PerchCatStaffData(
                    length, catStaffPerchGroundRY, perching, t, shouldRender,
                    initPos, isFalling, yBeforeFalling, perchData.initialFallDirection());
            newPerchData.save(player, true);
        }
    }

    private void launchPlayerInAir(Player player) {
        if (player.getDeltaMovement().y >= -0.1)
            player.setDeltaMovement(0, 0.8, 0);
        else
            player.setDeltaMovement(0, -player.getDeltaMovement().y, 0);
        player.hurtMarked = true;
    }

    private void perchFallingBehaviour(ItemStack stack, Player player, PerchCatStaffData perchData) {
        Vector3f initPos = perchData.initPos();
        float yBeforeFalling = perchData.yBeforeFalling();
        float length = perchData.length();
        Vector3f initialFallDirection = perchData.initialFallDirection();

        Vector3f staffOrigin = new Vector3f(initPos.x, yBeforeFalling + length, initPos.z);
        Vec3 fromPlayerToStaff = new Vec3(staffOrigin.x - player.getX(), staffOrigin.y - player.getY(), staffOrigin.z - player.getZ());
        length = -length;
        if (fromPlayerToStaff.length() < length) {
            Vec3 constrain = new Vec3(fromPlayerToStaff.toVector3f());
            constrain = constrain.normalize();
            constrain = constrain.scale(fromPlayerToStaff.length() - length);
            constrain = constrain.add(player.getX(), player.getY(), player.getZ());
            player.setPos(constrain);

            Vec3 towards = new Vec3(initialFallDirection);
            towards = MineraculousMathUtils.projectOnCircle(fromPlayerToStaff, towards);
            towards = towards.normalize();
            player.setDeltaMovement(towards);
            player.hurtMarked = true;
        }

        if (fromPlayerToStaff.length() > length + 1) {
            clearPerch(player);
        }
    }

    private float detectGround(Level level, Player player) {
        float toReturn;
        int y = player.getBlockY();
        while (level.getBlockState(new BlockPos(player.getBlockX(), y, player.getBlockZ())).isEmpty() && Math.abs(player.getBlockY() - y) <= MineraculousServerConfig.get().maxCatStaffLength.get()) {
            y--;
        }
        y++;
        toReturn = (float) y - (float) player.getY();
        return toReturn;
    }

    private float adjustLength(float catStaffPerchGroundRY, float length) {
        if (catStaffPerchGroundRY < length) {
            length = length - 1f;
        }
        if (catStaffPerchGroundRY > length) {
            length = catStaffPerchGroundRY;
        }
        return length;
    }

    private void handleVerticalMovement(Player player, PerchCatStaffData perchData) {
        boolean isFalling = perchData.isFalling();
        float groundRYClient = perchData.yGroundLevel();
        float length = perchData.length();
        int perchTickClient = perchData.tick();
        if (!isFalling) {
            float d = 0;
            boolean upOrDownKeyPressed = false;
            boolean shouldNotFall = (groundRYClient == length);
            if (MineraculousKeyMappings.WEAPON_DOWN_ARROW.isDown()) {
                d -= 0.3f;
                upOrDownKeyPressed = true;
            }
            if (MineraculousKeyMappings.WEAPON_UP_ARROW.isDown() && Math.abs(groundRYClient) < MineraculousServerConfig.get().maxCatStaffLength.get()) {
                d += 0.3f;
                upOrDownKeyPressed = true;
            }
            if (perchTickClient >= MAX_TICKS) {
                if (!upOrDownKeyPressed && shouldNotFall) d = 0;
                Vec3 vec3 = new Vec3(player.getDeltaMovement().x, d, player.getDeltaMovement().z);
                player.setDeltaMovement(vec3);
                player.hurtMarked = true;
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetDeltaMovementPayload(vec3, true));
            }
        }
    }

    private void handleMovementInput(Player player, PerchCatStaffData perchData) {
        boolean isFalling = perchData.isFalling();
        Vector3f initPos = perchData.initPos();
        int tick = perchData.tick();
        MineraculousClientUtils.InputState input = MineraculousClientUtils.captureInput();
        Vec3 movement = Vec3.ZERO;
        if (isFalling) {
            if (input.jump())
                movement = new Vec3(player.getDeltaMovement().x, 1.5, player.getDeltaMovement().z);
        } else {
            Vector3f staffPosition = new Vector3f(initPos.x, 0, initPos.z);
            if (tick > MAX_TICKS && input.hasInput()) {
                Vec3 staffPositionRelativeToThePlayer = new Vec3(staffPosition.x - player.getX(), 0, staffPosition.z - player.getZ());
                movement = input.getMovementVector();
                movement = MineraculousMathUtils.projectOnCircle(staffPositionRelativeToThePlayer, movement);
                if (movement.length() > MOVEMENT_THRESHOLD)
                    movement = movement.scale(MOVEMENT_SCALE);
            }
        }
        if (!movement.equals(Vec3.ZERO)) {
            player.setDeltaMovement(movement);
            player.hurtMarked = true;
            TommyLibServices.NETWORK.sendToServer(new ServerboundSetDeltaMovementPayload(movement, true));
        }
    }

    public static void clearPerch(Player player) {
        player.setData(MineraculousAttachmentTypes.PERCH_CAT_STAFF, new PerchCatStaffData());
        PerchCatStaffData.remove(player, true);
    }
}
