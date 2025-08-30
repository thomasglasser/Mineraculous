package dev.thomasglasser.mineraculous.impl.world.level.storage;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record MiraculousLadybugTargetData(Optional<Vec3> currentTarget, List<BlockPos> blockTargets, int sphereTicks) {

    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousLadybugTargetData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(TommyLibExtraStreamCodecs.VEC_3), MiraculousLadybugTargetData::currentTarget,
            StreamCodec.of(
                    (buf, list) -> {
                        buf.writeVarInt(list.size());
                        for (BlockPos pos : list) {
                            BlockPos.STREAM_CODEC.encode(buf, pos);
                        }
                    },
                    buf -> {
                        int size = buf.readVarInt();
                        List<BlockPos> list = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) {
                            list.add(BlockPos.STREAM_CODEC.decode(buf));
                        }
                        return list;
                    }),
            MiraculousLadybugTargetData::blockTargets,
            ByteBufCodecs.INT, MiraculousLadybugTargetData::sphereTicks,
            MiraculousLadybugTargetData::new);
    public MiraculousLadybugTargetData(List<BlockPos> blockTargets) {
        this(Optional.empty(), blockTargets, 0);
    }

    public MiraculousLadybugTargetData() {
        this(Optional.empty(), List.of(), 0);
    }

    public void tick(Entity entity, boolean syncToClient) {
        int newTick = Math.max(0, this.sphereTicks - 1);
        MiraculousLadybugTargetData newData = new MiraculousLadybugTargetData(this.currentTarget, this.blockTargets, newTick);
        entity.setData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET, newData);
        if (syncToClient) {
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET, newData), entity.getServer());
        }
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET, this);
        if (syncToClient) {
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET, this), entity.getServer());
        }
    }

    public static void remove(Entity entity, boolean syncToClient) {
        MiraculousLadybugTargetData newValue = new MiraculousLadybugTargetData();
        entity.setData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET, newValue);
        if (syncToClient) {
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TARGET, newValue), entity.getServer());
        }
    }
}
