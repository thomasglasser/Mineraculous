package dev.thomasglasser.mineraculous.impl.world.level.storage;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public record LeashingLadybugYoyoData(int leashedId, float maxRopeLength) {
    public static final StreamCodec<ByteBuf, LeashingLadybugYoyoData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, LeashingLadybugYoyoData::leashedId,
            ByteBufCodecs.FLOAT, LeashingLadybugYoyoData::maxRopeLength,
            LeashingLadybugYoyoData::new);

    private static final float DEFAULT_MAX_ROPE_LENGTH = 6.0F;

    public LeashingLadybugYoyoData(int leashedId, float maxRopeLength) {
        this.leashedId = leashedId;
        this.maxRopeLength = Math.max(maxRopeLength, ThrownLadybugYoyo.MIN_MAX_ROPE_LENGTH);
    }

    public LeashingLadybugYoyoData(int leashedId) {
        this(leashedId, DEFAULT_MAX_ROPE_LENGTH);
    }

    public LeashingLadybugYoyoData withMaxRopeLength(float maxRopeLength) {
        return new LeashingLadybugYoyoData(leashedId, Math.max(maxRopeLength, ThrownLadybugYoyo.MIN_MAX_ROPE_LENGTH));
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO, Optional.of(this));
        if (syncToClient) {
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO, Optional.of(this)), entity.getServer());
        }
    }

    public static void remove(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO, Optional.empty());
        if (syncToClient) {
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO, Optional.<LeashingLadybugYoyoData>empty()), entity.getServer());
        }
    }
}
