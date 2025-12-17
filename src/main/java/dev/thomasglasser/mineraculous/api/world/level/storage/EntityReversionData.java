package dev.thomasglasser.mineraculous.api.world.level.storage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.nbt.MineraculousNbtUtils;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/// Data for reverting trackable entity changes
public class EntityReversionData extends SavedData {
    private static final String FILE_ID = "entity_reversion";
    private final SetMultimap<UUID, UUID> trackedAndRelatedEntities = HashMultimap.create();
    private final Table<UUID, EntityLocation, Set<RevertibleEntity>> revertibleEntities = HashBasedTable.create();
    private final SetMultimap<UUID, UUID> removableEntities = HashMultimap.create();
    private final Table<UUID, EntityLocation, Set<RevertibleEntity>> convertedEntities = HashBasedTable.create();
    private final SetMultimap<UUID, UUID> copiedEntities = HashMultimap.create();

    public static EntityReversionData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(EntityReversionData.factory(), EntityReversionData.FILE_ID);
    }

    private static Factory<EntityReversionData> factory() {
        return new Factory<>(EntityReversionData::new, EntityReversionData::load, DataFixTypes.LEVEL);
    }

    @ApiStatus.Internal
    public void tick(Entity entity) {
        if (isBeingTracked(entity.getUUID()) && entity.tickCount % SharedConstants.TICKS_PER_SECOND * 5 == 0) {
            Set<UUID> alreadyRelated = getRelatedEntities(entity.getUUID());
            List<Entity> newRelated = entity.level().getEntitiesOfClass(Entity.class, entity.getBoundingBox().inflate(16), target -> !alreadyRelated.contains(target.getUUID()) && shouldBeTracked(target));
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

    /**
     * Determines if the provided ID is currently being tracked.
     *
     * @param id The ID to check
     * @return Whether the ID is currently being tracked
     */
    public boolean isBeingTracked(UUID id) {
        return trackedAndRelatedEntities.containsKey(id);
    }

    /**
     * Returns an immutable set of all entities related to the provided ID.
     *
     * @param trackedId The ID to check for related entities
     * @return An immutable set of all entities related to the provided ID
     */
    public Set<UUID> getRelatedEntities(UUID trackedId) {
        return ImmutableSet.copyOf(trackedAndRelatedEntities.get(trackedId));
    }

    /**
     * Returns a set of all entities related to the provided ID,
     * removing them from the tracking list.
     *
     * @param trackedId The ID to stop tracking and get related entities for
     * @return A set of all entities related to the provided ID
     */
    public Set<UUID> getAndClearTrackedAndRelatedEntities(UUID trackedId) {
        Set<UUID> related = trackedAndRelatedEntities.removeAll(trackedId);
        setDirty();
        return related;
    }

    /**
     * Adds a related entity to the tracking list for a tracked entity.
     *
     * @param trackedId The ID of the tracked entity to add a related entity to
     * @param relatedId The ID of the related entity to add
     */
    public void putRelatedEntity(UUID trackedId, UUID relatedId) {
        if (trackedId.equals(relatedId))
            return;
        trackedAndRelatedEntities.put(trackedId, relatedId);
        setDirty();
    }

    /**
     * Marks the provided ID for tracking.
     *
     * @param id The ID to start tracking
     */
    public void startTracking(UUID id) {
        trackedAndRelatedEntities.put(id, id);
        setDirty();
    }

    /**
     * Collects all positions for reversion and conversion for the provided ID.
     *
     * @param cause The ID to collect positions for
     * @return A multimap containing all positions for reversion and conversion for the provided ID, keyed by dimension and position
     */
    public SetMultimap<ResourceKey<Level>, Vec3> getReversionAndConversionPositions(UUID cause) {
        SetMultimap<ResourceKey<Level>, Vec3> positions = HashMultimap.create();
        for (EntityLocation location : revertibleEntities.row(cause).keySet()) {
            positions.put(location.dimension(), location.pos());
        }
        for (EntityLocation location : convertedEntities.row(cause).keySet()) {
            positions.put(location.dimension(), location.pos());
        }
        return positions;
    }

    /**
     * Collects all entities for reversion and conversion at the provided position for the provided ID.
     *
     * @param cause     The ID to collect entities for
     * @param dimension The dimension to collect entities in
     * @param pos       The position to collect entities at
     * @return A map containing all entities for reversion and conversion at the provided position for the provided ID, keyed by entity UUID
     */
    public Map<UUID, CompoundTag> getRevertibleAndConvertedAt(UUID cause, ResourceKey<Level> dimension, Vec3 pos) {
        EntityLocation location = new EntityLocation(dimension, pos);
        Map<UUID, CompoundTag> data = new Reference2ReferenceOpenHashMap<>();
        Set<RevertibleEntity> revertibles = revertibleEntities.get(cause, location);
        if (revertibles != null) {
            for (RevertibleEntity entity : revertibles) {
                data.put(entity.uuid(), entity.data());
            }
        }
        Set<RevertibleEntity> converted = convertedEntities.get(cause, location);
        if (converted != null) {
            for (RevertibleEntity entity : converted) {
                data.put(entity.uuid(), entity.data());
            }
        }
        return data;
    }

    private void revert(RevertibleEntity revertible, ResourceKey<Level> targetDimension, ServerLevel level, UnaryOperator<Entity> onLoaded) {
        CompoundTag data = revertible.data();
        Entity entity = MineraculousEntityUtils.findEntity(level, revertible.uuid());
        if (entity != null) {
            entity.discard();
        }
        level = level.getServer().getLevel(targetDimension);
        if (level == null) {
            MineraculousConstants.LOGGER.error("Tried to revert an entity in a level that does not exist: {}", targetDimension.location());
            return;
        }
        entity = EntityType.loadEntityRecursive(data, level, loaded -> {
            onLoaded.apply(loaded);
            return loaded;
        });
        if (entity != null) {
            level.addFreshEntity(entity);
        }
    }

    private void revert(RevertibleEntity revertible, EntityLocation location, ServerLevel level) {
        revert(revertible, location.dimension(), level, UnaryOperator.identity());
    }

    /**
     * Reverts all revertible and converted entities at the provided position for the provided cause.
     *
     * @param cause The ID to revert entities for
     * @param level The level to revert entities in
     * @param pos   The position to revert entities at
     */
    public void revertRevertibleAndConverted(UUID cause, ServerLevel level, Vec3 pos) {
        EntityLocation location = new EntityLocation(level.dimension(), pos);
        Set<RevertibleEntity> revertibles = new ReferenceOpenHashSet<>();
        Set<RevertibleEntity> entities = this.revertibleEntities.remove(cause, location);
        if (entities != null) {
            revertibles.addAll(entities);
        }
        entities = this.convertedEntities.remove(cause, location);
        if (entities != null) {
            revertibles.addAll(entities);
        }
        if (!revertibles.isEmpty()) {
            for (RevertibleEntity revertible : revertibles) {
                revert(revertible, location, level);
            }
            setDirty();
        }
    }

    private Set<RevertibleEntity> getOrCreateRevertible(UUID owner, EntityLocation location) {
        return revertibleEntities.row(owner).computeIfAbsent(location, l -> new ObjectOpenHashSet<>());
    }

    /**
     * Marks the provided entity for reversion for the provided cause.
     *
     * @param cause  The ID to mark the entity for reversion for
     * @param entity The entity to mark for reversion
     */
    public void putRevertible(UUID cause, Entity entity) {
        putRelatedEntity(cause, entity.getUUID());
        if (entity instanceof PartEntity<?> partEntity) {
            putRevertible(cause, partEntity.getParent());
            return;
        }
        if (!entity.getType().canSerialize())
            return;
        if (!isMarkedForReversion(entity.getUUID())) {
            getOrCreateRevertible(cause, EntityLocation.of(entity)).add(RevertibleEntity.of(entity));
            setDirty();
        }
    }

    private Set<RevertibleEntity> getAllRevertible() {
        Set<RevertibleEntity> revertible = new ObjectOpenHashSet<>();
        for (Set<RevertibleEntity> entities : revertibleEntities.values()) {
            revertible.addAll(entities);
        }
        return revertible;
    }

    /**
     * Determines if the provided entity is marked for reversion.
     *
     * @param entityId The ID of the entity to check
     * @return Whether the provided entity is marked for reversion
     */
    public boolean isMarkedForReversion(UUID entityId) {
        for (RevertibleEntity revertible : getAllRevertible()) {
            if (revertible.uuid().equals(entityId))
                return true;
        }
        return false;
    }

    /**
     * Reverts all removable and copied entities for the provided cause.
     *
     * @param cause The ID to revert removable and copied entities for
     * @param level The level to get the server from for entity finding
     */
    public void revertRemovableAndCopied(UUID cause, ServerLevel level) {
        Set<UUID> removables = removableEntities.removeAll(cause);
        if (!removables.isEmpty()) {
            for (UUID removableId : removables) {
                Entity removable = MineraculousEntityUtils.findEntity(level, removableId);
                if (removable != null) {
                    removable.discard();
                }
            }
        }
        for (UUID id : copiedEntities.removeAll(cause)) {
            Entity entity = MineraculousEntityUtils.findEntity(level, id);
            if (entity != null) {
                entity.discard();
            }
        }
        setDirty();
    }

    /**
     * Marks an entity for removal for the provided cause.
     *
     * @param cause  The ID to mark the entity for removal for
     * @param entity The entity to mark for removal
     */
    public void putRemovable(UUID cause, Entity entity) {
        putRelatedEntity(cause, entity.getUUID());
        if (entity instanceof PartEntity<?> partEntity) {
            putRemovable(cause, partEntity.getParent());
            return;
        }
        removableEntities.put(cause, entity.getUUID());
        setDirty();
    }

    /**
     * Determines the cause of the provided entity's reversion or removal if it's marked for it.
     *
     * @param entity The entity to check
     * @return The cause of the provided entity's reversion or removal if it's marked for it, or {@code null} if the entity is not marked for reversion or removal
     */
    public @Nullable UUID getCause(Entity entity) {
        for (Table.Cell<UUID, EntityLocation, Set<RevertibleEntity>> cell : revertibleEntities.cellSet()) {
            for (RevertibleEntity revertible : cell.getValue()) {
                if (revertible.uuid().equals(entity.getUUID()))
                    return cell.getRowKey();
            }
        }
        for (Map.Entry<UUID, UUID> entry : removableEntities.entries()) {
            if (entry.getValue().equals(entity.getUUID()))
                return entry.getKey();
        }
        for (Table.Cell<UUID, EntityLocation, Set<RevertibleEntity>> cell : convertedEntities.cellSet()) {
            for (RevertibleEntity revertible : cell.getValue()) {
                if (revertible.uuid().equals(entity.getUUID()))
                    return cell.getRowKey();
            }
        }
        return null;
    }

    /**
     * Reverts the entity with the provided ID if it's marked for reversion or removal,
     * applying the provided operator to the entity when loaded.
     *
     * @param entityId The ID of the entity to revert if it's marked for reversion or removal
     * @param level    The level to revert the entity in
     * @param onLoaded The operator to apply to the entity when loaded
     */
    public void revertConversionOrCopy(UUID entityId, ServerLevel level, UnaryOperator<Entity> onLoaded) {
        for (Table.Cell<UUID, EntityLocation, Set<RevertibleEntity>> cell : convertedEntities.cellSet()) {
            Iterator<RevertibleEntity> it = cell.getValue().iterator();
            while (it.hasNext()) {
                RevertibleEntity revertible = it.next();
                if (revertible.uuid().equals(entityId)) {
                    revert(revertible, cell.getColumnKey().dimension(), level, onLoaded);
                    it.remove();
                    setDirty();
                    break;
                }
            }
        }
        for (UUID cause : copiedEntities.keySet()) {
            Iterator<UUID> it = copiedEntities.get(cause).iterator();
            while (it.hasNext()) {
                if (it.next().equals(entityId)) {
                    Entity copy = MineraculousEntityUtils.findEntity(level, entityId);
                    if (copy != null) {
                        copy.discard();
                    }
                    it.remove();
                    setDirty();
                    break;
                }
            }
        }
    }

    public void revertConversionOrCopy(UUID entityId, ServerLevel level) {
        revertConversionOrCopy(entityId, level, UnaryOperator.identity());
    }

    private Set<RevertibleEntity> getOrCreateConverted(UUID cause, EntityLocation location) {
        return convertedEntities.row(cause).computeIfAbsent(location, l -> new ObjectOpenHashSet<>());
    }

    /**
     * Marks an entity as converted for the provided cause.
     *
     * @param cause  The ID to mark the entity as converted for
     * @param entity The entity to mark as converted
     */
    public void putConverted(UUID cause, Entity entity) {
        if (!isConvertedOrCopied(entity.getUUID())) {
            getOrCreateConverted(cause, EntityLocation.of(entity)).add(RevertibleEntity.of(entity));
            setDirty();
        }
    }

    /**
     * Marks the provided entity as a copied conversion if the original is converted.
     *
     * @param original The converted entity being copied
     * @param copy     The copy of the original entity
     */
    public void putCopied(Entity original, Entity copy) {
        UUID converter = getConverter(original.getUUID());
        if (converter != null) {
            copiedEntities.put(converter, copy.getUUID());
        }
    }

    /**
     * Determines the cause of the provided entity's conversion if it's converted.
     *
     * @param entityId The ID of the entity to check for conversion
     * @return The cause of the provided entity's conversion if it's converted, or {@code null} if the entity is not converted
     */
    public @Nullable UUID getConverter(UUID entityId) {
        for (Table.Cell<UUID, EntityLocation, Set<RevertibleEntity>> cell : convertedEntities.cellSet()) {
            for (RevertibleEntity revertible : cell.getValue()) {
                if (revertible.uuid().equals(entityId))
                    return cell.getRowKey();
            }
        }
        return null;
    }

    private Set<RevertibleEntity> getAllConverted() {
        Set<RevertibleEntity> converted = new ObjectOpenHashSet<>();
        for (Set<RevertibleEntity> entities : convertedEntities.values()) {
            converted.addAll(entities);
        }
        return converted;
    }

    /**
     * Determines if the provided entity is converted or copied.
     *
     * @param entityId The ID of the entity to check
     * @return Whether the provided entity is converted or copied
     */
    public boolean isConvertedOrCopied(UUID entityId) {
        for (RevertibleEntity revertible : getAllConverted()) {
            if (revertible.uuid().equals(entityId))
                return true;
        }
        for (UUID copied : copiedEntities.values()) {
            if (copied.equals(entityId))
                return true;
        }
        return false;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        Function<EntityLocation, Tag> locationEncoder = MineraculousNbtUtils.codecEncoder(EntityLocation.CODEC, ops);
        Function<RevertibleEntity, Tag> revertibleEncoder = MineraculousNbtUtils.codecEncoder(RevertibleEntity.CODEC, ops);
        tag.put("TrackedAndRelated", MineraculousNbtUtils.writeStringKeyedMultimap(trackedAndRelatedEntities, UUID::toString, NbtUtils::createUUID));
        tag.put("Revertible", MineraculousNbtUtils.writeStringRowKeyedTable(revertibleEntities, UUID::toString, locationEncoder, values -> MineraculousNbtUtils.writeCollection(values, revertibleEncoder)));
        tag.put("Removable", MineraculousNbtUtils.writeStringKeyedMultimap(removableEntities, UUID::toString, NbtUtils::createUUID));
        tag.put("Converted", MineraculousNbtUtils.writeStringRowKeyedTable(convertedEntities, UUID::toString, locationEncoder, values -> MineraculousNbtUtils.writeCollection(values, revertibleEncoder)));
        tag.put("Copied", MineraculousNbtUtils.writeStringKeyedMultimap(copiedEntities, UUID::toString, NbtUtils::createUUID));
        return tag;
    }

    private static EntityReversionData load(CompoundTag tag, HolderLookup.Provider registries) {
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        Function<Tag, EntityLocation> locationDecoder = MineraculousNbtUtils.codecDecoder(EntityLocation.CODEC, ops);
        Function<Tag, RevertibleEntity> revertibleDecoder = MineraculousNbtUtils.codecDecoder(RevertibleEntity.CODEC, ops);
        EntityReversionData data = new EntityReversionData();
        data.trackedAndRelatedEntities.putAll(MineraculousNbtUtils.readStringKeyedMultimap(HashMultimap::create, tag.getCompound("TrackedAndRelated"), UUID::fromString, NbtUtils::loadUUID));
        // Java gets weird about the generics in the set here, requires a more generic declaration
        Table<UUID, EntityLocation, Set<RevertibleEntity>> table = MineraculousNbtUtils.readStringRowKeyedTable(HashBasedTable::create, tag.getCompound("Revertible"), UUID::fromString, locationDecoder, t -> MineraculousNbtUtils.readCollection(ReferenceOpenHashSet::new, (ListTag) t, revertibleDecoder));
        data.revertibleEntities.putAll(table);
        data.removableEntities.putAll(MineraculousNbtUtils.readStringKeyedMultimap(HashMultimap::create, tag.getCompound("Removable"), UUID::fromString, NbtUtils::loadUUID));
        table = MineraculousNbtUtils.readStringRowKeyedTable(HashBasedTable::create, tag.getCompound("Converted"), UUID::fromString, locationDecoder, t -> MineraculousNbtUtils.readCollection(ReferenceOpenHashSet::new, (ListTag) t, revertibleDecoder));
        data.convertedEntities.putAll(table);
        data.copiedEntities.putAll(MineraculousNbtUtils.readStringKeyedMultimap(HashMultimap::create, tag.getCompound("Copied"), UUID::fromString, NbtUtils::loadUUID));
        return data;
    }

    private record EntityLocation(ResourceKey<Level> dimension, Vec3 pos) {
        private static final Codec<EntityLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(EntityLocation::dimension),
                Vec3.CODEC.fieldOf("pos").forGetter(EntityLocation::pos)).apply(instance, EntityLocation::new));

        public static EntityLocation of(Entity entity) {
            return new EntityLocation(entity.level().dimension(), entity.position());
        }
    }

    private record RevertibleEntity(UUID uuid, CompoundTag data) {
        private static final Codec<RevertibleEntity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.CODEC.fieldOf("uuid").forGetter(RevertibleEntity::uuid),
                CompoundTag.CODEC.fieldOf("data").forGetter(RevertibleEntity::data)).apply(instance, RevertibleEntity::new));

        private static RevertibleEntity of(Entity entity) {
            if (!entity.getType().canSerialize() || entity instanceof PartEntity<?>) {
                throw new IllegalArgumentException("Cannot revert non-serializable or part entity " + entity.getUUID());
            }
            CompoundTag tag = new CompoundTag();
            entity.save(tag);
            return new RevertibleEntity(entity.getUUID(), tag);
        }
    }
}
