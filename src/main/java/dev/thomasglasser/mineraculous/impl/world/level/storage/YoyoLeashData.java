package dev.thomasglasser.mineraculous.impl.world.level.storage;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;

public record YoyoLeashData(int leashHolder) {
    public static final StreamCodec<ByteBuf, YoyoLeashData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, YoyoLeashData::leashHolder,
            YoyoLeashData::new);

    public void tick(Entity entity, ServerLevel level) {
        Entity leasher = level.getEntity(leashHolder);
        if (entity.isAlive() && entity instanceof Leashable && leasher != null && leasher.isAlive() && leasher.level() == level) {
            tickInternal((Entity & Leashable) entity, leasher);
        } else {
            remove(entity, true);
        }
    }

    private <T extends Entity & Leashable> void tickInternal(T leashed, Entity leasher) {
        float distance = leashed.distanceTo(leasher);
        if (!leashed.handleLeashAtDistance(leasher, distance)) {
            return;
        }

        if (distance > 6) {
            leashed.elasticRangeLeashBehaviour(leasher, distance);
            leashed.checkSlowFallDistance();
        } else {
            leashed.closeRangeLeashBehaviour(leasher);
        }
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.YOYO_LEASH, Optional.of(this));
        if (syncToClient) {
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.YOYO_LEASH, Optional.of(this)), entity.getServer());
        }
    }

    public static void remove(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.YOYO_LEASH, Optional.empty());
        if (syncToClient) {
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.YOYO_LEASH, Optional.<YoyoLeashData>empty()), entity.getServer());
        }
    }
}
