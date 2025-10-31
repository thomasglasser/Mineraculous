package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.google.common.collect.ImmutableList;
import com.ibm.icu.impl.Pair;
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
import java.util.Collection;
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

public record MiraculousLadybugTriggerData(List<MiraculousLadybugBlockTarget> blockTargets, List<MiraculousLadybugEntityTarget> entityTargets, Optional<Integer> performerId, Optional<Holder<SoundEvent>> revertSound, int tickCount) {

    public static final Codec<MiraculousLadybugTriggerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MiraculousLadybugBlockTarget.CODEC.listOf().fieldOf("block_targets").forGetter(MiraculousLadybugTriggerData::blockTargets),
            MiraculousLadybugEntityTarget.CODEC.listOf().fieldOf("entity_targets").forGetter(MiraculousLadybugTriggerData::entityTargets),
            Codec.INT.optionalFieldOf("performer_id").forGetter(MiraculousLadybugTriggerData::performerId),
            SoundEvent.CODEC.optionalFieldOf("revert_sound").forGetter(MiraculousLadybugTriggerData::revertSound),
            Codec.INT.fieldOf("tick_count").forGetter(MiraculousLadybugTriggerData::tickCount)).apply(instance, MiraculousLadybugTriggerData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousLadybugTriggerData> STREAM_CODEC = StreamCodec.composite(
            MiraculousLadybugBlockTarget.STREAM_CODEC.apply(ByteBufCodecs.list()), MiraculousLadybugTriggerData::blockTargets,
            MiraculousLadybugEntityTarget.STREAM_CODEC.apply(ByteBufCodecs.list()), MiraculousLadybugTriggerData::entityTargets,
            ByteBufCodecs.optional(ByteBufCodecs.INT), MiraculousLadybugTriggerData::performerId,
            ByteBufCodecs.optional(SoundEvent.STREAM_CODEC), MiraculousLadybugTriggerData::revertSound,
            ByteBufCodecs.INT, MiraculousLadybugTriggerData::tickCount,
            MiraculousLadybugTriggerData::new);

    public static final int MIRACULOUS_LADYBUGS_COUNT = 8;
    public MiraculousLadybugTriggerData() {
        this(ImmutableList.of(), ImmutableList.of(), Optional.empty(), Optional.empty(), 0);
    }

    public MiraculousLadybugTriggerData(Collection<MiraculousLadybugBlockTarget> blockTargets, Collection<MiraculousLadybugEntityTarget> entityTargets, Optional<Integer> performerId, Optional<Holder<SoundEvent>> revertSound) {
        this(ImmutableList.copyOf(blockTargets), ImmutableList.copyOf(entityTargets), performerId, revertSound, 0);
    }

    public MiraculousLadybugTriggerData incrementTicks() {
        return new MiraculousLadybugTriggerData(blockTargets, entityTargets, performerId, revertSound, tickCount + 1);
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
            this.incrementTicks().save(entity, true);
            entity.setNoGravity(true);
            entity.setDeltaMovement(0, 0, 0);
            entity.hurtMarked = true;
            if (tickCount == 1) {
                if (performer != null)
                    Ability.playSound(level, performer, revertSound);
                spawnSphereParticles(MineraculousParticleTypes.SUMMONING_LADYBUG.get(), level, entity.position(), 200);
                spawnSphereParticles(ParticleTypes.END_ROD, level, entity.position(), 10);
            } else if (tickCount > 20) {
                spawnMiraculousLadybugs(level, entity);
                if (performer != null)
                    LuckyCharmIdData.get(level).incrementLuckyCharmId(performer.getUUID());
                entity.discard();
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

    private void spawnMiraculousLadybugs(ServerLevel level, Entity entity) {
        ArrayList<Vector2d> circle = MineraculousMathUtils.generateCirclePoints(50, MIRACULOUS_LADYBUGS_COUNT);
        Vec3 spawnPos = entity.position();
        ArrayList<Pair<List<MiraculousLadybugBlockTarget>, List<MiraculousLadybugEntityTarget>>> targetDatas = assignTargets(blockTargets, entityTargets);

        int spawnedMLBCount;
        for (spawnedMLBCount = 0; spawnedMLBCount < MIRACULOUS_LADYBUGS_COUNT; spawnedMLBCount++) {
            Pair<List<MiraculousLadybugBlockTarget>, List<MiraculousLadybugEntityTarget>> targetData = Pair.of(List.of(), List.of());
            ArrayList<MiraculousLadybugBlockTarget> updatedBlockTargets = new ArrayList<>();
            if (spawnedMLBCount < targetDatas.size()) {
                targetData = targetDatas.get(spawnedMLBCount);
                updatedBlockTargets = new ArrayList<>(targetData.first);
            }
            int x = (int) circle.get(spawnedMLBCount).x;
            int y = (int) circle.get(spawnedMLBCount).y;
            Vec3 circlePos = spawnPos.add(x, 0, y);
            updatedBlockTargets.addFirst(MiraculousLadybugBlockTarget.wrap(new BlockPos(MineraculousMathUtils.getVec3i(circlePos))));
            updatedBlockTargets.addFirst(MiraculousLadybugBlockTarget.wrap(new BlockPos(MineraculousMathUtils.getVec3i(spawnPos))));
            MiraculousLadybug miraculousLadybug = new MiraculousLadybug(level);
            miraculousLadybug.setPos(spawnPos);
            level.addFreshEntity(miraculousLadybug);
            MiraculousLadybugTargetData.create(updatedBlockTargets, targetData.second).save(miraculousLadybug, true);
        }
        level.sendParticles(ParticleTypes.FLASH, spawnPos.x, spawnPos.y, spawnPos.z, 1, 0, 0, 0, 0);
    }

    private static ArrayList<Pair<List<MiraculousLadybugBlockTarget>, List<MiraculousLadybugEntityTarget>>> assignTargets(List<MiraculousLadybugBlockTarget> blockTargets, List<MiraculousLadybugEntityTarget> entityTargets) {
        ArrayList<Pair<List<MiraculousLadybugBlockTarget>, List<MiraculousLadybugEntityTarget>>> targets = new ArrayList<>();

        int blockCount = blockTargets.size();
        int entityCount = entityTargets.size();
        int totalCount = blockCount + entityCount;

        if (totalCount == 0) {
            return targets;
        }

        // Calculate distribution for blocks
        int blockTasksPerEntity = blockCount > 0 ? blockCount / MIRACULOUS_LADYBUGS_COUNT : 0;
        int blockRemainder = blockCount > 0 ? blockCount % MIRACULOUS_LADYBUGS_COUNT : 0;
        int blockMaxCount = Math.min(blockCount, MIRACULOUS_LADYBUGS_COUNT);

        // Calculate distribution for entities
        int entityTasksPerEntity = entityCount > 0 ? entityCount / MIRACULOUS_LADYBUGS_COUNT : 0;
        int entityRemainder = entityCount > 0 ? entityCount % MIRACULOUS_LADYBUGS_COUNT : 0;
        int entityMaxCount = Math.min(entityCount, MIRACULOUS_LADYBUGS_COUNT);

        int maxCount = Math.max(blockMaxCount, entityMaxCount);
        int blockStart = 0;
        int entityStart = 0;

        for (int i = 0; i < maxCount; i++) {
            List<MiraculousLadybugBlockTarget> blockSubTargets = ImmutableList.of();
            List<MiraculousLadybugEntityTarget> entitySubTargets = ImmutableList.of();

            // Handle blocks
            if (i < blockMaxCount) {
                int blockEnd = blockStart + blockTasksPerEntity + (i < blockRemainder ? 1 : 0);
                blockSubTargets = blockTargets.subList(blockStart, Math.min(blockEnd, blockCount));
                blockStart = blockEnd;
            }

            // Handle entities
            if (i < entityMaxCount) {
                int entityEnd = entityStart + entityTasksPerEntity + (i < entityRemainder ? 1 : 0);
                entitySubTargets = entityTargets.subList(entityStart, Math.min(entityEnd, entityCount));
                entityStart = entityEnd;
            }

            // Add the combined target data
            targets.add(Pair.of(blockSubTargets, entitySubTargets));
        }

        return targets;
    }
}
