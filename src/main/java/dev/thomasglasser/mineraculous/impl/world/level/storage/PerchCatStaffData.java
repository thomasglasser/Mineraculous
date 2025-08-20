package dev.thomasglasser.mineraculous.impl.world.level.storage;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.network.ClientboundCatStaffPerchPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

public record PerchCatStaffData(
        float length,
        float yGroundLevel,
        boolean perching,
        int tick,
        boolean canRender,
        Vector3f initPos,
        boolean isFalling,
        float yBeforeFalling,
        Vector3f initialFallDirection) {

    public static final StreamCodec<ByteBuf, PerchCatStaffData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            ByteBufCodecs.FLOAT, PerchCatStaffData::length,
            ByteBufCodecs.FLOAT, PerchCatStaffData::yGroundLevel,
            ByteBufCodecs.BOOL, PerchCatStaffData::perching,
            ByteBufCodecs.INT, PerchCatStaffData::tick,
            ByteBufCodecs.BOOL, PerchCatStaffData::canRender,
            ByteBufCodecs.VECTOR3F, PerchCatStaffData::initPos, //y stores rotation
            ByteBufCodecs.BOOL, PerchCatStaffData::isFalling,
            ByteBufCodecs.FLOAT, PerchCatStaffData::yBeforeFalling,
            ByteBufCodecs.VECTOR3F, PerchCatStaffData::initialFallDirection,
            PerchCatStaffData::new);
    public PerchCatStaffData() {
        this(0f, 0, false, 0, false, new Vector3f(0f, 0f, 0f), false, 0f, new Vector3f(0f, 0f, 0f));
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.PERCH_CAT_STAFF, this);
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundCatStaffPerchPayload(entity.getId(), this), entity.getServer());
    }

    public static void remove(Entity entity, boolean syncToClient) {
        PerchCatStaffData data = new PerchCatStaffData();
        entity.setData(MineraculousAttachmentTypes.PERCH_CAT_STAFF, data);
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundCatStaffPerchPayload(entity.getId(), data), entity.getServer());
    }
}
