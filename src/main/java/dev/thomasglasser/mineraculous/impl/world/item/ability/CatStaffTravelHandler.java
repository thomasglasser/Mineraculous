package dev.thomasglasser.mineraculous.impl.world.item.ability;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.level.storage.TravelCatStaffData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class CatStaffTravelHandler {
    public void tick(ItemStack stack, Level level, Player player) {
        if (!level.isClientSide) {
            TravelCatStaffData travelCatStaffData = player.getData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF);
            behaviour(stack, player, travelCatStaffData);
        }

        if (player.getCooldowns().isOnCooldown(stack.getItem()))
            player.resetFallDistance();
    }

    public static void init(Level level, Player player) {
        TravelCatStaffData travelCatStaffData = player.getData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF);
        if (travelCatStaffData.traveling()) return;

        Vec3 lookAngle = player.getLookAngle().normalize();
        BlockHitResult result = level.clip(new ClipContext(
                player.getEyePosition(),
                player.getEyePosition()
                        .add(lookAngle.scale(-MineraculousServerConfig.get().maxCatStaffLength.get())),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                player));
        BlockPos hitPos = travelCatStaffData.blockPos();

        float length = 0;
        boolean traveling;
        if (result.getType() == HitResult.Type.BLOCK) {
            hitPos = result.getBlockPos();
            traveling = true;
        } else traveling = false;

        double initRot = ((player.getYRot() % 360) + 360) % 360;

        //SAVE DATA
        TravelCatStaffData newTravelData = new TravelCatStaffData(
                length, hitPos, traveling,
                lookAngle.toVector3f(),
                (float) player.getY(),
                (float) initRot,
                travelCatStaffData.launch());
        player.setData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF, newTravelData);
        newTravelData.save(player, true);
    }

    private void behaviour(ItemStack stack, Player player, TravelCatStaffData travelData) {
        if (!travelData.traveling()) return;

        float length = travelData.length();
        boolean didLaunch = travelData.launch();
        BlockPos targetPos = travelData.blockPos();

        // Distance from the player to target
        float targetDistance = new Vector3f(
                (float) (player.getX() - targetPos.getX()),
                (float) (player.getY() - targetPos.getY()),
                (float) (player.getZ() - targetPos.getZ())).length();

        // Gradually increasing the length until it reaches the target
        length = adjustLength(length, targetDistance);

        if (length == targetDistance && !didLaunch) {
            launchPlayer(stack, player, travelData);
            didLaunch = true;
        }

        if (didLaunch && player.getDeltaMovement().y < 0.5) {
            clearTravel(player);
        } else {
            TravelCatStaffData newTravelData = new TravelCatStaffData(
                    length, targetPos, true,
                    travelData.initialLookingAngle(),
                    travelData.y(),
                    travelData.initBodAngle(), didLaunch);
            newTravelData.save(player, true);
        }
    }

    private void launchPlayer(ItemStack stack, Player player, TravelCatStaffData travelCatStaffData) {
        player.setDeltaMovement(new Vec3(travelCatStaffData.initialLookingAngle()).normalize().scale(4));
        player.hurtMarked = true;
        player.getCooldowns().addCooldown(stack.getItem(), 40);
    }

    private float adjustLength(float length, float targetDistance) {
        if (length < targetDistance && length <= MineraculousServerConfig.get().maxCatStaffLength.get()) length += 8;
        if (length > targetDistance) length = targetDistance;
        return length;
    }

    public static void clearTravel(Player player) {
        player.setData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF, new TravelCatStaffData());
        TravelCatStaffData.remove(player, true);
    }
}
