package dev.thomasglasser.mineraculous.impl.world.level.storage;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.world.item.ability.CatStaffPerchHandler;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

public record PerchingCatStaffData(
        float length,
        float yGroundLevel,
        boolean perching,
        int tick,
        boolean canRender,
        Vector3f initPos,
        boolean isFalling,
        float yBeforeFalling,
        Vector3f initialFallDirection,
        boolean fastDescending) {

    public static final StreamCodec<ByteBuf, PerchingCatStaffData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            ByteBufCodecs.FLOAT, PerchingCatStaffData::length,
            ByteBufCodecs.FLOAT, PerchingCatStaffData::yGroundLevel,
            ByteBufCodecs.BOOL, PerchingCatStaffData::perching,
            ByteBufCodecs.INT, PerchingCatStaffData::tick,
            ByteBufCodecs.BOOL, PerchingCatStaffData::canRender,
            ByteBufCodecs.VECTOR3F, PerchingCatStaffData::initPos, //y stores rotation
            ByteBufCodecs.BOOL, PerchingCatStaffData::isFalling,
            ByteBufCodecs.FLOAT, PerchingCatStaffData::yBeforeFalling,
            ByteBufCodecs.VECTOR3F, PerchingCatStaffData::initialFallDirection,
            ByteBufCodecs.BOOL, PerchingCatStaffData::fastDescending,
            PerchingCatStaffData::new);

    public static PerchingCatStaffData DEFAULT = new PerchingCatStaffData();
    public PerchingCatStaffData() {
        this(0f, 0, false, 0, false, new Vector3f(0f, 0f, 0f), false, 0f, new Vector3f(0f, 0f, 0f), false);
    }

    public void save(Entity entity) {
        entity.setData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF, this);
    }

    public static void remove(Entity entity) {
        entity.removeData(MineraculousAttachmentTypes.PERCHING_CAT_STAFF);
    }

    public static Vector4f initialize(Entity entity) {
        if (entity instanceof Player player) {
            float initRot = player.getYRot();
            if (initRot < 0) //simplify:
                initRot += 360.0f;
            if (initRot >= 360.0f)
                initRot -= 360.0f;
            double cos = Math.cos(Math.toRadians(initRot)); //z
            double sin = -Math.sin(Math.toRadians(initRot)); //x
            Vec3 direction = new Vec3(sin, 0, cos);
            Vec3 playerPos = new Vec3(player.getX(), 0, player.getZ());
            direction = direction.normalize();
            direction = direction.scale(CatStaffPerchHandler.PERCH_STAFF_DISTANCE);
            direction = direction.add(playerPos);
            return new Vector4f((float) direction.x, (float) direction.y, (float) direction.z, initRot);
        }
        return new Vector4f(0, 0, 0, 0);
    }
}
