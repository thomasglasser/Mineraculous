package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.world.ability.Ability;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.mineraculous.impl.world.entity.MiraculousLadybug;
import dev.thomasglasser.mineraculous.impl.world.level.miraculousladybugtarget.MiraculousLadybugTarget;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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

public record MiraculousLadybugTriggerData(List<MiraculousLadybugTarget<?>> targets, Optional<Integer> performerId, Optional<Holder<SoundEvent>> revertSound, int tickCount) {

    public static final Codec<MiraculousLadybugTriggerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MiraculousLadybugTarget.CODEC.listOf().fieldOf("targets").forGetter(MiraculousLadybugTriggerData::targets),
            Codec.INT.optionalFieldOf("performer_id").forGetter(MiraculousLadybugTriggerData::performerId),
            SoundEvent.CODEC.optionalFieldOf("revert_sound").forGetter(MiraculousLadybugTriggerData::revertSound),
            Codec.INT.fieldOf("tick_count").forGetter(MiraculousLadybugTriggerData::tickCount)).apply(instance, MiraculousLadybugTriggerData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousLadybugTriggerData> STREAM_CODEC = StreamCodec.composite(
            MiraculousLadybugTarget.STREAM_CODEC.apply(ByteBufCodecs.list()), MiraculousLadybugTriggerData::targets,
            ByteBufCodecs.optional(ByteBufCodecs.INT), MiraculousLadybugTriggerData::performerId,
            ByteBufCodecs.optional(SoundEvent.STREAM_CODEC), MiraculousLadybugTriggerData::revertSound,
            ByteBufCodecs.INT, MiraculousLadybugTriggerData::tickCount,
            MiraculousLadybugTriggerData::new);

    public static final int MIRACULOUS_LADYBUGS_COUNT = 8;
    public MiraculousLadybugTriggerData() {
        this(ImmutableList.of(), Optional.empty(), Optional.empty(), 0);
    }

    public MiraculousLadybugTriggerData(Collection<MiraculousLadybugTarget<?>> targets, Optional<Integer> performerId, Optional<Holder<SoundEvent>> revertSound) {
        this(ImmutableList.copyOf(targets), performerId, revertSound, 0);
    }

    public MiraculousLadybugTriggerData incrementTicks() {
        return new MiraculousLadybugTriggerData(targets, performerId, revertSound, tickCount + 1);
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
        Vec3 spawnPosition = entity.position();
        ArrayList<ArrayList<MiraculousLadybugTarget<?>>> targetDatas = assignTargets();

        int spawnedMLBCount;
        for (spawnedMLBCount = 0; spawnedMLBCount < MIRACULOUS_LADYBUGS_COUNT; spawnedMLBCount++) {
            ArrayList<MiraculousLadybugTarget<?>> targets = targetDatas.get(spawnedMLBCount);
            double x = circle.get(spawnedMLBCount).x;
            double y = circle.get(spawnedMLBCount).y;
            Vec3 circlePosition = spawnPosition.add(x, 0, y);
            targets = new ArrayList<>(MineraculousMathUtils.sortTargets(targets, circlePosition));
            MiraculousLadybugTargetData targetData = MiraculousLadybugTargetData.create(targets, spawnPosition, circlePosition);
            MiraculousLadybug miraculousLadybug = new MiraculousLadybug(level);
            miraculousLadybug.setPos(spawnPosition);
            level.addFreshEntity(miraculousLadybug);
            miraculousLadybug.setTargetData(targetData);
        }
        level.sendParticles(ParticleTypes.FLASH, spawnPosition.x, spawnPosition.y, spawnPosition.z, 1, 0, 0, 0, 0);
    }

    private ArrayList<ArrayList<MiraculousLadybugTarget<?>>> assignTargets() {
        ArrayList<ArrayList<MiraculousLadybugTarget<?>>> targetsTable = new ArrayList<>(MIRACULOUS_LADYBUGS_COUNT);
        for (int i = 0; i < MIRACULOUS_LADYBUGS_COUNT; i++) {
            targetsTable.add(new ArrayList<>());
        }
        int targetsCount = targets.size() - 1;
        int miraculousLadybugIndex = 0;
        while (targetsCount > -1) {
            MiraculousLadybugTarget<?> target = targets.get(targetsCount);
            ArrayList<MiraculousLadybugTarget<?>> list = targetsTable.get(miraculousLadybugIndex);
            list.add(target);
            targetsTable.set(miraculousLadybugIndex, list);
            miraculousLadybugIndex = miraculousLadybugIndex == MIRACULOUS_LADYBUGS_COUNT - 1 ? 0 : miraculousLadybugIndex + 1;
            targetsCount--;
        }

        return targetsTable;
    }
}
