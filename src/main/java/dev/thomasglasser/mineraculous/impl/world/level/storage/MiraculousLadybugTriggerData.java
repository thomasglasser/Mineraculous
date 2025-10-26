package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.MiraculousLadybug;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;

public record MiraculousLadybugTriggerData(List<BlockPos> blockTasks, List<MiraculousLadybugTargetData.EntityTarget> entityTasks, Optional<Integer> performerId, Optional<Holder<SoundEvent>> revertSound, int tickCount) {

    public static final Codec<MiraculousLadybugTriggerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("block_tasks").forGetter(MiraculousLadybugTriggerData::blockTasks),
            MiraculousLadybugTargetData.EntityTarget.CODEC.listOf().fieldOf("entity_tasks").forGetter(MiraculousLadybugTriggerData::entityTasks),
            Codec.INT.optionalFieldOf("performer_id").forGetter(MiraculousLadybugTriggerData::performerId),
            SoundEvent.CODEC.optionalFieldOf("revert_sound").forGetter(MiraculousLadybugTriggerData::revertSound),
            Codec.INT.fieldOf("tick_count").forGetter(MiraculousLadybugTriggerData::tickCount)).apply(instance, MiraculousLadybugTriggerData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousLadybugTriggerData> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), MiraculousLadybugTriggerData::blockTasks,
            MiraculousLadybugTargetData.EntityTarget.STREAM_CODEC.apply(ByteBufCodecs.list()), MiraculousLadybugTriggerData::entityTasks,
            ByteBufCodecs.optional(ByteBufCodecs.INT), MiraculousLadybugTriggerData::performerId,
            ByteBufCodecs.optional(SoundEvent.STREAM_CODEC), MiraculousLadybugTriggerData::revertSound,
            ByteBufCodecs.INT, MiraculousLadybugTriggerData::tickCount,
            MiraculousLadybugTriggerData::new);

    public static final int MIRACULOUS_LADYBUGS_COUNT = 8;
    public MiraculousLadybugTriggerData() {
        this(ImmutableList.of(), ImmutableList.of(), Optional.empty(), Optional.empty(), 0);
    }

    public MiraculousLadybugTriggerData(List<BlockPos> blockTasks, List<MiraculousLadybugTargetData.EntityTarget> entityTasks, Optional<Integer> performerId, Optional<Holder<SoundEvent>> revertSound) {
        this(blockTasks, entityTasks, performerId, revertSound, 0);
    }

    public MiraculousLadybugTriggerData incrementTicks() {
        return new MiraculousLadybugTriggerData(blockTasks, entityTasks, performerId, revertSound, tickCount + 1);
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TRIGGER, Optional.of(this));
        if (syncToClient) {
            TommyLibServices.NETWORK.sendToAllClients(
                    new ClientboundSyncDataAttachmentPayload<>(
                            entity.getId(),
                            MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TRIGGER,
                            Optional.of(this)),
                    entity.getServer());
        }
    }

    public static void remove(Entity entity, boolean syncToClient) {
        MiraculousLadybugTriggerData newValue = new MiraculousLadybugTriggerData();
        entity.setData(MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TRIGGER, Optional.of(newValue));
        if (syncToClient) {
            TommyLibServices.NETWORK.sendToAllClients(
                    new ClientboundSyncDataAttachmentPayload<>(
                            entity.getId(),
                            MineraculousAttachmentTypes.MIRACULOUS_LADYBUG_TRIGGER,
                            Optional.<Optional<MiraculousLadybugTriggerData>>empty()),
                    entity.getServer());
        }
    }

    public LivingEntity getPerformer(Level level) {
        return performerId.isPresent() && level.getEntity(performerId.get()) instanceof LivingEntity performer ? performer : null;
    }

    public void tick(ItemEntity entity, ServerLevel level) {
        double y = entity.getDeltaMovement().y;
        if (entity.isNoGravity() || (!entity.isNoGravity() && y < 0.13)) {
            LivingEntity performer = this.getPerformer(level);
            if (performer != null) {
                this.incrementTicks().save(entity, true);
                entity.setNoGravity(true);
                entity.setDeltaMovement(0, 0, 0);
                entity.hurtMarked = true;
                if (tickCount == 1) {
                    Ability.playSound(level, performer, revertSound);
                    spawnSphereParticles(MineraculousParticleTypes.SUMMONING_LADYBUG.get(), level, entity.position(), 200);
                    spawnSphereParticles(ParticleTypes.END_ROD, level, entity.position(), 10);
                } else if (tickCount > 20) {
                    ArrayList<MiraculousLadybugTargetData> taskTable = assignTasks(blockTasks, entityTasks);
                    spawnMiraculousLadybugs(level, entity, taskTable);
                    LuckyCharmIdData.get(level).incrementLuckyCharmId(performer.getUUID());
                    entity.discard();
                }
            }
        }
    }

    private static void spawnSphereParticles(SimpleParticleType type, ServerLevel level, Vec3 vec3, int particleCount) {
        double radius = 0.5;

        for (int i = 0; i < particleCount; i++) {
            double theta = Math.random() * 2 * Math.PI;
            double phi = Math.acos(2 * Math.random() - 1);

            double r = radius * Math.cbrt(Math.random());

            double xOffset = r * Math.sin(phi) * Math.cos(theta);
            double yOffset = r * Math.cos(phi);
            double zOffset = r * Math.sin(phi) * Math.sin(theta);

            level.sendParticles(
                    type,
                    vec3.x + xOffset,
                    vec3.y + yOffset,
                    vec3.z + zOffset,
                    1,
                    0, 0, 0, 0);
        }
    }

    private static void spawnMiraculousLadybugs(ServerLevel level, Entity entity, List<MiraculousLadybugTargetData> taskTable) {
        ArrayList<Vector2d> circle = MineraculousMathUtils.generateCirclePoints(50, MIRACULOUS_LADYBUGS_COUNT);
        Vec3 spawnPos = entity.position();

        int spawnedMLBCount;
        for (spawnedMLBCount = 0; spawnedMLBCount < MIRACULOUS_LADYBUGS_COUNT; spawnedMLBCount++) {
            MiraculousLadybugTargetData task = new MiraculousLadybugTargetData();
            ArrayList<BlockPos> updatedBlockTargets = new ArrayList<>();
            if (spawnedMLBCount < taskTable.size()) {
                task = taskTable.get(spawnedMLBCount);
                updatedBlockTargets = new ArrayList<>(task.blockTargets());
            }
            int x = (int) circle.get(spawnedMLBCount).x;
            int y = (int) circle.get(spawnedMLBCount).y;
            Vec3 circlePos = spawnPos.add(x, 0, y);
            updatedBlockTargets.addFirst(new BlockPos(MineraculousMathUtils.getVec3i(circlePos)));
            updatedBlockTargets.addFirst(new BlockPos(MineraculousMathUtils.getVec3i(spawnPos)));
            task = task.withBlockTargets(updatedBlockTargets);
            task = task.calculateSpline();
            MiraculousLadybug miraculousLadybug = new MiraculousLadybug(level);
            miraculousLadybug.setPos(spawnPos);
            level.addFreshEntity(miraculousLadybug);
            task.save(miraculousLadybug, true);
            level.sendParticles(ParticleTypes.FLASH, spawnPos.x, spawnPos.y, spawnPos.z, 1, 0, 0, 0, 0);
        }
    }

    private static ArrayList<MiraculousLadybugTargetData> assignTasks(List<BlockPos> blockTargets, List<MiraculousLadybugTargetData.EntityTarget> entityTargets) {
        ArrayList<MiraculousLadybugTargetData> taskTable = new ArrayList<>();
        int taskIndex = -1;

        // Assign block tasks
        int blockTasks = blockTargets.size();
        if (blockTasks > 0) {
            int tasksPerEntity = blockTasks / MIRACULOUS_LADYBUGS_COUNT;
            int remainder = blockTasks % MIRACULOUS_LADYBUGS_COUNT;
            int maxCount = Math.min(blockTasks, MIRACULOUS_LADYBUGS_COUNT);
            int start = 0;
            for (int i = 0; i < maxCount; i++) { //in case the remainder is smaller than 8, entities with no task will be completely discarded
                int end = start + tasksPerEntity + (i < remainder ? 1 : 0);
                List<BlockPos> subTargets = blockTargets.subList(start, Math.min(end, blockTasks));
                taskTable.add(new MiraculousLadybugTargetData(subTargets, ImmutableList.of()));
                taskIndex++;
                start = end;
            }
        }

        taskIndex++;
        taskIndex = taskIndex >= MIRACULOUS_LADYBUGS_COUNT ? taskIndex % MIRACULOUS_LADYBUGS_COUNT : taskIndex; //task index must belong to {0, 1, 2 ... 7}

        // Assign entity tasks
        int entityTasks = entityTargets.size();
        if (entityTasks > 0) {
            int tasksPerEntity = entityTasks / MIRACULOUS_LADYBUGS_COUNT;
            int remainder = entityTasks % MIRACULOUS_LADYBUGS_COUNT;
            int maxCount = Math.min(entityTasks, MIRACULOUS_LADYBUGS_COUNT);
            int start = 0;
            for (int i = 0; i < maxCount; i++) { //in case the remainder is smaller than 8, entities with no task will be completely discarded
                int end = start + tasksPerEntity + (i < remainder ? 1 : 0);
                List<MiraculousLadybugTargetData.EntityTarget> subTargets = entityTargets.subList(start, Math.min(end, entityTasks));
                if (taskIndex < taskTable.size()) {
                    taskTable.set(taskIndex, taskTable.get(taskIndex).withEntityTargets(subTargets));
                } else {
                    taskTable.add(new MiraculousLadybugTargetData(ImmutableList.of(), subTargets));
                }
                taskIndex++;
                taskIndex = taskIndex >= MIRACULOUS_LADYBUGS_COUNT ? taskIndex % MIRACULOUS_LADYBUGS_COUNT : taskIndex;
                start = end;
            }
        }

        return taskTable;
    }
}
