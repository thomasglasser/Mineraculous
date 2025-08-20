package dev.thomasglasser.mineraculous.impl.world.level.storage;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.network.ClientboundCatStaffTravelPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3f;

public record TravelCatStaffData(float length, BlockPos blockPos, boolean traveling, Vector3f initialLookingAngle, float y, float initBodAngle, boolean launch) {

    public static final StreamCodec<ByteBuf, TravelCatStaffData> STREAM_CODEC = TommyLibExtraStreamCodecs.composite(
            ByteBufCodecs.FLOAT, TravelCatStaffData::length,
            BlockPos.STREAM_CODEC, TravelCatStaffData::blockPos,
            ByteBufCodecs.BOOL, TravelCatStaffData::traveling,
            ByteBufCodecs.VECTOR3F, TravelCatStaffData::initialLookingAngle,
            ByteBufCodecs.FLOAT, TravelCatStaffData::y,
            ByteBufCodecs.FLOAT, TravelCatStaffData::initBodAngle,
            ByteBufCodecs.BOOL, TravelCatStaffData::launch,
            TravelCatStaffData::new);
    public TravelCatStaffData() {
        this(0f, new BlockPos(0, 0, 0), false, new Vector3f(0, 0, 0), 0, 0, false);
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF, this);
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundCatStaffTravelPayload(entity.getId(), this), entity.getServer());
    }

    public static void remove(Entity entity, boolean syncToClient) {
        TravelCatStaffData data = new TravelCatStaffData();
        entity.setData(MineraculousAttachmentTypes.TRAVEL_CAT_STAFF, data);
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundCatStaffTravelPayload(entity.getId(), data), entity.getServer());
    }
}
