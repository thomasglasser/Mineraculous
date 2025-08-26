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

public record MiraculousLadybugTargetData(Optional<Vec3> currentTarget, List<BlockPos> blockTargets) {
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
            MiraculousLadybugTargetData::new);

    public MiraculousLadybugTargetData(List<BlockPos> blockTargets) {
        this(Optional.empty(), blockTargets);
    }

    public MiraculousLadybugTargetData() {
        this(Optional.empty(), List.of());
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
