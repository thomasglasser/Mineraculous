package dev.thomasglasser.mineraculous.impl.world.level.storage;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

public record TravelingCatStaffData(float length, BlockPos blockPos, boolean traveling, Vector3f initialLookingAngle, float y, float initBodAngle, boolean launch) {

    public static final StreamCodec<ByteBuf, TravelingCatStaffData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            ByteBufCodecs.FLOAT, TravelingCatStaffData::length,
            BlockPos.STREAM_CODEC, TravelingCatStaffData::blockPos,
            ByteBufCodecs.BOOL, TravelingCatStaffData::traveling,
            ByteBufCodecs.VECTOR3F, TravelingCatStaffData::initialLookingAngle,
            ByteBufCodecs.FLOAT, TravelingCatStaffData::y,
            ByteBufCodecs.FLOAT, TravelingCatStaffData::initBodAngle,
            ByteBufCodecs.BOOL, TravelingCatStaffData::launch,
            TravelingCatStaffData::new);
    public static TravelingCatStaffData DEFAULT = new TravelingCatStaffData();
    public TravelingCatStaffData() {
        this(0f, new BlockPos(0, 0, 0), false, new Vector3f(0, 0, 0), 0, 0, false);
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF, this);
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.TRAVELING_CAT_STAFF, this), entity.getServer());
    }

    public static void remove(Entity entity, boolean syncToClient) {
        TravelingCatStaffData data = new TravelingCatStaffData();
        entity.setData(MineraculousAttachmentTypes.TRAVELING_CAT_STAFF, data);
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.TRAVELING_CAT_STAFF, data), entity.getServer());
    }
}
