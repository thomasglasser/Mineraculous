package dev.thomasglasser.mineraculous.impl.world.level.storage;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public record MiraculousLadybugTargetData(List<BlockPos> blockTargets, List<Vec3> entityTargets) {
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousLadybugTargetData> STREAM_CODEC = StreamCodec.composite(
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
            StreamCodec.of(
                    (buf, list) -> {
                        buf.writeVarInt(list.size());
                        for (Vec3 vec : list) {
                            TommyLibExtraStreamCodecs.VEC_3.encode(buf, vec);
                        }
                    },
                    buf -> {
                        int size = buf.readVarInt();
                        List<Vec3> list = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) {
                            list.add(TommyLibExtraStreamCodecs.VEC_3.decode(buf));
                        }
                        return list;
                    }),
            MiraculousLadybugTargetData::entityTargets,
            MiraculousLadybugTargetData::new);

    public MiraculousLadybugTargetData() {
        this(null, null);
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

    public MiraculousLadybugTargetData withEntityTargets(List<Vec3> newTargets) {
        return new MiraculousLadybugTargetData(this.blockTargets, newTargets);
    }

    public MiraculousLadybugTargetData withBlockTargets(List<BlockPos> newTargets) {
        return new MiraculousLadybugTargetData(newTargets, this.entityTargets);
    }
}
