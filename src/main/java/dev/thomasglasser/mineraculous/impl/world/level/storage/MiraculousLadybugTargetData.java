package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.util.TommyLibExtraStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

public record MiraculousLadybugTargetData(List<BlockPos> blockTargets, List<Vec3> entityTargets, List<Vec3> pathControlPoints, double splinePosition) {

    public static final Codec<MiraculousLadybugTargetData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("block_targets").forGetter(MiraculousLadybugTargetData::blockTargets),
            Vec3.CODEC.listOf().fieldOf("entity_targets").forGetter(MiraculousLadybugTargetData::entityTargets),
            Vec3.CODEC.listOf().fieldOf("path_control_points").forGetter(MiraculousLadybugTargetData::pathControlPoints),
            Codec.DOUBLE.fieldOf("spline_position").forGetter(MiraculousLadybugTargetData::splinePosition)).apply(instance, MiraculousLadybugTargetData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousLadybugTargetData> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), MiraculousLadybugTargetData::blockTargets,
            TommyLibExtraStreamCodecs.VEC_3.apply(ByteBufCodecs.list()), MiraculousLadybugTargetData::entityTargets,
            TommyLibExtraStreamCodecs.VEC_3.apply(ByteBufCodecs.list()), MiraculousLadybugTargetData::pathControlPoints,
            ByteBufCodecs.DOUBLE, MiraculousLadybugTargetData::splinePosition,
            MiraculousLadybugTargetData::new);
    public MiraculousLadybugTargetData() {
        this(ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), 0);
    }

    public MiraculousLadybugTargetData(List<BlockPos> blockTargets, List<Vec3> entityTargets) {
        this(blockTargets, entityTargets, ImmutableList.of(), 0);
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
        return new MiraculousLadybugTargetData(this.blockTargets, newTargets, this.pathControlPoints, this.splinePosition);
    }

    public MiraculousLadybugTargetData withBlockTargets(List<BlockPos> newTargets) {
        return new MiraculousLadybugTargetData(newTargets, this.entityTargets, this.pathControlPoints, this.splinePosition);
    }

    public MiraculousLadybugTargetData setSplinePosition(double splinePosition) {
        return new MiraculousLadybugTargetData(this.blockTargets, this.entityTargets, this.pathControlPoints, splinePosition);
    }

    public MiraculousLadybugTargetData calculateSpline() {
        List<Vec3> controlPoints = calculateControlPoints(this.blockTargets, this.entityTargets);
        MineraculousMathUtils.CatmullRom path = new MineraculousMathUtils.CatmullRom(controlPoints);
        return new MiraculousLadybugTargetData(this.blockTargets, this.entityTargets, controlPoints, path.getFirstParameter());
    }

    //TODO add spirals
    private static List<Vec3> calculateControlPoints(List<BlockPos> blockTargets, List<Vec3> entityTargets) {
        List<Vec3> targets = new ArrayList<>();
        int blockCount = blockTargets.size();
        int entityCount = entityTargets.size();
        if (blockCount >= 3)
            targets.addAll(MineraculousMathUtils.getCenter(blockTargets.subList(2, blockCount)));
        if (entityCount > 0)
            targets.addAll(entityTargets);
        Vec3 spawnPos = blockTargets.get(0).getCenter();
        Vec3 circlePos = blockTargets.get(1).getCenter();
        targets = MineraculousMathUtils.sortTargets(targets, circlePos);
        targets.addFirst(circlePos);
        targets.addFirst(spawnPos);
        int targetsCount = targets.size();
        ArrayList<Vec3> controlPoints = new ArrayList<>();
        if (targetsCount >= 3) {
            controlPoints.addAll(targets.subList(2, targetsCount));
        }
        for (int i = 0; i <= 25; i++) {
            controlPoints.add(i, spawnPos.lerp(circlePos, i / 25d));
        }
        return controlPoints;
    }
}
