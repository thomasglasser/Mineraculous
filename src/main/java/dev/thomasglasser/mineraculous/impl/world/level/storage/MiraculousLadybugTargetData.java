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

public record MiraculousLadybugTargetData(List<BlockPos> blockTargets, List<EntityTarget> entityTargets, List<Vec3> pathControlPoints, double splinePosition) {

    public static final Codec<MiraculousLadybugTargetData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("block_targets").forGetter(MiraculousLadybugTargetData::blockTargets),
            EntityTarget.CODEC.listOf().fieldOf("entity_targets").forGetter(MiraculousLadybugTargetData::entityTargets),
            Vec3.CODEC.listOf().fieldOf("path_control_points").forGetter(MiraculousLadybugTargetData::pathControlPoints),
            Codec.DOUBLE.fieldOf("spline_position").forGetter(MiraculousLadybugTargetData::splinePosition)).apply(instance, MiraculousLadybugTargetData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousLadybugTargetData> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), MiraculousLadybugTargetData::blockTargets,
            EntityTarget.STREAM_CODEC.apply(ByteBufCodecs.list()), MiraculousLadybugTargetData::entityTargets,
            TommyLibExtraStreamCodecs.VEC_3.apply(ByteBufCodecs.list()), MiraculousLadybugTargetData::pathControlPoints,
            ByteBufCodecs.DOUBLE, MiraculousLadybugTargetData::splinePosition,
            MiraculousLadybugTargetData::new);

    public MiraculousLadybugTargetData() {
        this(ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), 0);
    }

    public MiraculousLadybugTargetData(List<BlockPos> blockTargets, List<EntityTarget> entityTargets) {
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

    public MiraculousLadybugTargetData withEntityTargets(List<EntityTarget> newTargets) {
        return new MiraculousLadybugTargetData(blockTargets, newTargets, pathControlPoints, splinePosition);
    }

    public MiraculousLadybugTargetData withBlockTargets(List<BlockPos> newTargets) {
        return new MiraculousLadybugTargetData(newTargets, entityTargets, pathControlPoints, splinePosition);
    }

    public MiraculousLadybugTargetData setSplinePosition(double splinePosition) {
        return new MiraculousLadybugTargetData(blockTargets, entityTargets, pathControlPoints, splinePosition);
    }

    public MiraculousLadybugTargetData calculateSpline() {
        List<Vec3> controlPoints = calculateControlPoints(blockTargets, entityTargets);
        MineraculousMathUtils.CatmullRom path = new MineraculousMathUtils.CatmullRom(controlPoints);
        return new MiraculousLadybugTargetData(this.blockTargets, this.entityTargets, controlPoints, path.getFirstParameter());
    }

    //TODO add spirals!!!!!
    private static List<Vec3> calculateControlPoints(List<BlockPos> blockTargets, List<EntityTarget> entityTargets) {
        ArrayList<EntityTarget> targets = new ArrayList<>();
        int blockCount = blockTargets.size();
        int entityCount = entityTargets.size();
        if (blockCount >= 3)
            targets.addAll(EntityTarget.convertToEntityTarget(MineraculousMathUtils.getCenter(blockTargets.subList(2, blockCount))));
        if (entityCount > 0)
            targets.addAll(entityTargets);
        Vec3 spawnPos = blockTargets.get(0).getCenter();
        Vec3 circlePos = blockTargets.get(1).getCenter();
        targets = new ArrayList<>(MineraculousMathUtils.sortTargets(targets, circlePos, EntityTarget::position));
        targets.addFirst(EntityTarget.convertToEntityTarget(circlePos));
        targets.addFirst(EntityTarget.convertToEntityTarget(spawnPos));
        int targetsCount = targets.size();
        ArrayList<Vec3> controlPoints = new ArrayList<>();
        if (targetsCount >= 3) {
            for (int i = 2; i < targetsCount; i++) {
                EntityTarget target = targets.get(i);
                if (target.width == -1) { //normal target
                    controlPoints.add(target.position);
                } else { //spin target
                    controlPoints.addAll(MineraculousMathUtils.spinAround(target.position, target.width, target.height, Math.PI / 2d, target.height / 16d));
                }
            }
        }
        for (int i = 0; i <= 25; i++) {
            controlPoints.add(i, spawnPos.lerp(circlePos, i / 25d));
        }
        return controlPoints;
    }
    public record EntityTarget(Vec3 position, float width, float height) {

        public static Codec<EntityTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec3.CODEC.fieldOf("position").forGetter(EntityTarget::position),
                Codec.FLOAT.fieldOf("width").forGetter(EntityTarget::width),
                Codec.FLOAT.fieldOf("height").forGetter(EntityTarget::height)).apply(instance, EntityTarget::new));
        public static StreamCodec<RegistryFriendlyByteBuf, EntityTarget> STREAM_CODEC = StreamCodec.composite(
                TommyLibExtraStreamCodecs.VEC_3, EntityTarget::position,
                ByteBufCodecs.FLOAT, EntityTarget::width,
                ByteBufCodecs.FLOAT, EntityTarget::height,
                EntityTarget::new);
        public static EntityTarget convertToEntityTarget(Vec3 pos) {
            return new EntityTarget(pos, -1, -1);
        }

        public static ArrayList<EntityTarget> convertToEntityTarget(ArrayList<Vec3> arrayList) {
            ArrayList<EntityTarget> toReturn = new ArrayList<>();
            for (Vec3 vec : arrayList) {
                toReturn.add(convertToEntityTarget(vec));
            }
            return toReturn;
        }
    }
}
