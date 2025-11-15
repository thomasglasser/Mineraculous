package dev.thomasglasser.mineraculous.api.world.level.storage;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

/// Data for reverting trackable entity changes
public class AbilityReversionEntityData extends SavedData {
    public static final String FILE_ID = "ability_reversion_entity";
    private final SetMultimap<UUID, UUID> trackedAndRelatedEntities = HashMultimap.create();
    private final SetMultimap<UUID, RevertibleEntity> revertibleEntities = HashMultimap.create();
    private final SetMultimap<UUID, UUID> removableEntities = HashMultimap.create();
    private final SetMultimap<UUID, RevertibleEntity> convertedEntities = HashMultimap.create();

    public static AbilityReversionEntityData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(AbilityReversionEntityData.factory(), AbilityReversionEntityData.FILE_ID);
    }

    public static SavedData.Factory<AbilityReversionEntityData> factory() {
        return new SavedData.Factory<>(AbilityReversionEntityData::new, (p_294039_, p_324123_) -> load(p_294039_), DataFixTypes.LEVEL);
    }

    public void tick(Entity entity) {
        if (isBeingTracked(entity.getUUID()) && entity.tickCount % SharedConstants.TICKS_PER_SECOND * 5 == 0) {
            Set<UUID> alreadyRelated = getRelatedEntities(entity.getUUID());
            List<Entity> newRelated = entity.level().getEntitiesOfClass(Entity.class, entity.getBoundingBox().inflate(16), target -> !alreadyRelated.contains(target.getUUID()) && (target.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed() || target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()));
            for (Entity related : newRelated) {
                if (related.getUUID() != entity.getUUID()) {
                    putRelatedEntity(entity.getUUID(), related.getUUID());
                }
            }
        }
    }

    // TODO: Move to an event
    protected boolean shouldBeTracked(Entity entity) {
        return entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed() || entity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent();
    }

    public void putRelatedEntity(UUID trackedEntity, UUID relatedEntity) {
        if (trackedEntity.equals(relatedEntity))
            return;
        Collection<UUID> related = relatedEntities.get(trackedEntity);
        if (!related.contains(relatedEntity)) {
            related.add(relatedEntity);
            setDirty();
        }
    }
  
  
    // TODO: Reimplement changes
    public Set<UUID> getRelatedEntities(UUID uuid) {
        return trackedAndRelatedEntities.get(uuid);
    }

    public Set<UUID> getAndClearTrackedAndRelatedEntities(UUID uuid) {
        Set<UUID> related = trackedAndRelatedEntities.removeAll(uuid);
        setDirty();
        return related;
    }

    public void putRelatedEntity(UUID trackedEntity, UUID relatedEntity) {
        if (trackedEntity.equals(relatedEntity))
            return;
        trackedAndRelatedEntities.put(trackedEntity, relatedEntity);
        setDirty();
    }

    public void startTracking(UUID uuid) {
        trackedAndRelatedEntities.put(uuid, uuid);
        setDirty();
    }

    public void revert(UUID owner, ServerLevel level) {
        for (RevertibleEntity revertible : revertibleEntities.removeAll(owner)) {
            UUID entityId = revertible.uuid();
            ServerLevel targetLevel = level.getServer().getLevel(revertible.dimension());
            CompoundTag entityData = revertible.data();
            Entity entity = MineraculousEntityUtils.findEntity(level, entityId);
            if (entity != null && !entity.isRemoved()) {
                entity.load(entityData);
            } else if (targetLevel != null) {
                entity = EntityType.loadEntityRecursive(entityData, targetLevel, e -> e);
                if (entity != null)
                    targetLevel.addFreshEntity(entity);
            }
        }
        for (UUID id : removableEntities.removeAll(owner)) {
            Entity entity = MineraculousEntityUtils.findEntity(level, id);
            if (entity != null) {
                entity.discard();
            }
        }
        setDirty();
    }

    protected <R, C, V> V computeIfAbsent(Table<R, C, V> table, R rowKey, C columnKey, Supplier<V> supplier) {
        return table.row(rowKey).computeIfAbsent(columnKey, c -> supplier.get());
    }

    protected List<CompoundTag> getOrCreateRevertible(UUID owner, Pair<ResourceKey<Level>, Vec3> pos) {
        return computeIfAbsent(revertibleEntities, owner, pos, ReferenceArrayList::new);
    }

    protected List<UUID> getOrCreateRemovable(UUID owner, Pair<ResourceKey<Level>, Vec3> pos) {
        return computeIfAbsent(removableEntities, owner, pos, ReferenceArrayList::new);
    }

    public void putRevertible(UUID owner, Entity entity) {
        putRelatedEntity(owner, entity.getUUID());
        if (entity instanceof PartEntity<?> partEntity) {
            putRevertible(owner, partEntity.getParent());
            return;
        }
        if (!entity.getType().canSerialize())
            return;
        if (!isMarkedForReversion(entity.getUUID())) {
            revertibleEntities.put(owner, RevertibleEntity.of(entity));
            setDirty();
        }
    }

    public boolean isMarkedForReversion(UUID entity) {
        for (RevertibleEntity revertible : revertibleEntities.values()) {
            if (revertible.uuid().equals(entity))
                return true;
        }
        return false;
    }

    public void putRemovable(UUID owner, Entity entity) {
        putRelatedEntity(owner, entity.getUUID());
        if (entity instanceof PartEntity<?> partEntity) {
            putRemovable(owner, partEntity.getParent());
            return;
        }
        removableEntities.put(owner, entity.getUUID());
        setDirty();
    }

    public @Nullable UUID getCause(Entity entity, ServerLevel level) {
        for (Map.Entry<UUID, RevertibleEntity> entry : revertibleEntities.entries()) {
            Entity found = MineraculousEntityUtils.findEntity(level, entry.getValue().uuid());
            if (found == entity)
                return entry.getKey();
        }
        for (Map.Entry<UUID, UUID> entry : removableEntities.entries()) {
            Entity found = MineraculousEntityUtils.findEntity(level, entry.getValue());
            if (found == entity)
                return entry.getKey();
        }
        return null;
    }

    public void putConverted(UUID performer, Entity entity) {
        if (!isConverted(entity.getUUID())) {
            convertedEntities.put(performer, RevertibleEntity.of(entity));
            setDirty();
        }
    }

    public boolean isConverted(UUID entity) {
        for (RevertibleEntity revertible : convertedEntities.values()) {
            if (revertible.uuid().equals(entity))
                return true;
        }
        return false;
    }

    public void revertConversions(UUID performer, ServerLevel level) {
        for (RevertibleEntity revertible : convertedEntities.removeAll(performer)) {
            revertConversion(revertible, level);
        }
        setDirty();
    }

    public @Nullable Entity revertConversion(UUID entity, ServerLevel level) {
        UUID foundPerformer = null;
        RevertibleEntity found = null;
        for (UUID performer : convertedEntities.keySet()) {
            for (RevertibleEntity revertible : convertedEntities.get(performer)) {
                if (revertible.uuid().equals(entity)) {
                    foundPerformer = performer;
                    found = revertible;
                    break;
                }
            }
        }
        if (found != null) {
            convertedEntities.remove(foundPerformer, found);
            setDirty();
            return revertConversion(found, level);
        }
        return null;
    }

    private Entity revertConversion(RevertibleEntity revertible, ServerLevel level) {
        UUID entityId = revertible.uuid();
        ResourceKey<Level> dimension = revertible.dimension();
        CompoundTag data = revertible.data();
        Entity entity = MineraculousEntityUtils.findEntity(level, entityId);
        Vec3 pos = null;
        ServerLevel targetLevel;
        if (entity != null && !entity.isRemoved()) {
            pos = entity.position();
            targetLevel = (ServerLevel) entity.level();
            entity.discard();
        } else {
            targetLevel = level.getServer().getLevel(dimension);
        }

        if (targetLevel != null) {
            entity = EntityType.loadEntityRecursive(data, targetLevel, e -> e);
            if (entity != null) {
                if (pos != null) {
                    entity.setPos(pos);
                }
                targetLevel.addFreshEntity(entity);
            }
        } else {
            entity = null;
        }

        setDirty();
        return entity;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag trackedAndRelated = new CompoundTag();
        for (UUID trackedId : trackedAndRelatedEntities.keySet()) {
            ListTag entries = new ListTag();
            for (UUID relatedId : trackedAndRelatedEntities.get(trackedId)) {
                entries.add(NbtUtils.createUUID(relatedId));
            }
            trackedAndRelated.put(trackedId.toString(), entries);
        }
        tag.put("TrackedAndRelated", trackedAndRelated);
        CompoundTag revertible = new CompoundTag();
        for (UUID ownerId : revertibleEntities.keySet()) {
            ListTag entries = new ListTag();
            for (RevertibleEntity revertibleEntity : revertibleEntities.get(ownerId)) {
                entries.add(RevertibleEntity.CODEC.encodeStart(NbtOps.INSTANCE, revertibleEntity).getOrThrow());
            }
            revertible.put(ownerId.toString(), entries);
        }
        tag.put("Revertible", revertible);
        CompoundTag removable = new CompoundTag();
        for (UUID ownerId : removableEntities.keySet()) {
            ListTag entries = new ListTag();
            for (UUID removableId : removableEntities.get(ownerId)) {
                entries.add(NbtUtils.createUUID(removableId));
            }
            removable.put(ownerId.toString(), entries);
        }
        tag.put("Removable", removable);
        CompoundTag converted = new CompoundTag();
        for (UUID ownerId : convertedEntities.keySet()) {
            ListTag entries = new ListTag();
            for (RevertibleEntity convertedEntity : convertedEntities.get(ownerId)) {
                entries.add(RevertibleEntity.CODEC.encodeStart(NbtOps.INSTANCE, convertedEntity).getOrThrow());
            }
            converted.put(ownerId.toString(), entries);
        }
        tag.put("Converted", converted);
        return tag;
    }

    public static AbilityReversionEntityData load(CompoundTag tag) {
        AbilityReversionEntityData data = new AbilityReversionEntityData();
        CompoundTag trackedAndRelated = tag.getCompound("TrackedAndRelated");
        for (String trackedString : trackedAndRelated.getAllKeys()) {
            UUID trackedId = UUID.fromString(trackedString);
            ListTag entries = trackedAndRelated.getList(trackedString, ListTag.TAG_INT_ARRAY);
            for (Tag related : entries) {
                data.trackedAndRelatedEntities.put(trackedId, NbtUtils.loadUUID(related));
            }
        }
        CompoundTag revertible = tag.getCompound("Revertible");
        for (String ownerString : revertible.getAllKeys()) {
            UUID ownerId = UUID.fromString(ownerString);
            ListTag entries = revertible.getList(ownerString, ListTag.TAG_COMPOUND);
            for (Tag revertibleEntity : entries) {
                data.revertibleEntities.put(ownerId, RevertibleEntity.CODEC.parse(NbtOps.INSTANCE, revertibleEntity).getOrThrow());
            }
        }
        CompoundTag removable = tag.getCompound("Removable");
        for (String ownerString : removable.getAllKeys()) {
            UUID ownerId = UUID.fromString(ownerString);
            ListTag entries = removable.getList(ownerString, ListTag.TAG_INT_ARRAY);
            for (Tag removableId : entries) {
                data.removableEntities.put(ownerId, NbtUtils.loadUUID(removableId));
            }
        }
        CompoundTag converted = tag.getCompound("Converted");
        for (String ownerString : converted.getAllKeys()) {
            UUID ownerId = UUID.fromString(ownerString);
            ListTag entries = converted.getList(ownerString, ListTag.TAG_COMPOUND);
            for (Tag convertedEntity : entries) {
                data.convertedEntities.put(ownerId, RevertibleEntity.CODEC.parse(NbtOps.INSTANCE, convertedEntity).getOrThrow());
            }
        }
        return data;
    }

    private record RevertibleEntity(UUID uuid, ResourceKey<Level> dimension, CompoundTag data) {

        private static final Codec<RevertibleEntity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.CODEC.fieldOf("uuid").forGetter(RevertibleEntity::uuid),
                ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(RevertibleEntity::dimension),
                CompoundTag.CODEC.fieldOf("data").forGetter(RevertibleEntity::data)).apply(instance, RevertibleEntity::new));
        private static RevertibleEntity of(Entity entity) {
            if (!entity.getType().canSerialize() || entity instanceof PartEntity<?>) {
                throw new IllegalArgumentException("Cannot revert non-serializable or part entity " + entity.getUUID());
            }
            CompoundTag tag = new CompoundTag();
            entity.save(tag);
            return new RevertibleEntity(entity.getUUID(), entity.level().dimension(), tag);
        }
    }
}
