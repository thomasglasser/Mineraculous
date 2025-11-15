package dev.thomasglasser.mineraculous.api.world.level.storage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
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
    private final Table<UUID, EntityLocation, Set<RevertibleEntity>> revertibleEntities = HashBasedTable.create();
    private final SetMultimap<UUID, UUID> removableEntities = HashMultimap.create();
    private final Table<UUID, EntityLocation, Set<RevertibleEntity>> convertedEntities = HashBasedTable.create();

    public static AbilityReversionEntityData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(AbilityReversionEntityData.factory(), AbilityReversionEntityData.FILE_ID);
    }

    public static SavedData.Factory<AbilityReversionEntityData> factory() {
        return new SavedData.Factory<>(AbilityReversionEntityData::new, (p_294039_, p_324123_) -> load(p_294039_), DataFixTypes.LEVEL);
    }

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

    public boolean isBeingTracked(UUID uuid) {
        return trackedAndRelatedEntities.containsKey(uuid);
    }

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

    public SetMultimap<ResourceKey<Level>, Vec3> getReversionAndConversionPositions(UUID uuid) {
        SetMultimap<ResourceKey<Level>, Vec3> positions = HashMultimap.create();
        for (EntityLocation location : revertibleEntities.row(uuid).keySet()) {
            positions.put(location.dimension(), location.pos());
        }
        for (EntityLocation location : convertedEntities.row(uuid).keySet()) {
            positions.put(location.dimension(), location.pos());
        }
        return positions;
    }

    public Map<UUID, CompoundTag> getRevertibleAndConvertedAt(UUID uuid, ResourceKey<Level> dimension, Vec3 pos) {
        EntityLocation location = new EntityLocation(dimension, pos);
        Map<UUID, CompoundTag> data = new Reference2ReferenceOpenHashMap<>();
        Set<RevertibleEntity> revertibles = revertibleEntities.get(uuid, location);
        if (revertibles != null) {
            for (RevertibleEntity entity : revertibles) {
                data.put(entity.uuid(), entity.data());
            }
        }
        Set<RevertibleEntity> converted = convertedEntities.get(uuid, location);
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

    public void revertRevertibleAndConverted(UUID owner, ServerLevel level, Vec3 pos) {
        EntityLocation location = new EntityLocation(level.dimension(), pos);
        Set<RevertibleEntity> revertibles = new ReferenceOpenHashSet<>();
        Set<RevertibleEntity> entities = this.revertibleEntities.remove(owner, location);
        if (entities != null) {
            revertibles.addAll(entities);
        }
        entities = this.convertedEntities.remove(owner, location);
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

    public void putRevertible(UUID owner, Entity entity) {
        putRelatedEntity(owner, entity.getUUID());
        if (entity instanceof PartEntity<?> partEntity) {
            putRevertible(owner, partEntity.getParent());
            return;
        }
        if (!entity.getType().canSerialize())
            return;
        if (!isMarkedForReversion(entity.getUUID())) {
            getOrCreateRevertible(owner, EntityLocation.of(entity)).add(RevertibleEntity.of(entity));
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

    public boolean isMarkedForReversion(UUID entity) {
        for (RevertibleEntity revertible : getAllRevertible()) {
            if (revertible.uuid().equals(entity))
                return true;
        }
        return false;
    }

    // TODO: Do this with item reverting in the beginning
    public void revertRemovable(UUID owner, ServerLevel level) {
        Set<UUID> removables = removableEntities.removeAll(owner);
        if (!removables.isEmpty()) {
            for (UUID removableId : removables) {
                Entity removable = MineraculousEntityUtils.findEntity(level, removableId);
                if (removable != null) {
                    removable.discard();
                }
            }
            setDirty();
        }
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

    public void revertConversion(UUID entity, ServerLevel level, UnaryOperator<Entity> onLoaded) {
        for (Table.Cell<UUID, EntityLocation, Set<RevertibleEntity>> cell : convertedEntities.cellSet()) {
            Iterator<RevertibleEntity> it = cell.getValue().iterator();
            while (it.hasNext()) {
                RevertibleEntity revertible = it.next();
                if (revertible.uuid().equals(entity)) {
                    revert(revertible, cell.getColumnKey().dimension(), level, onLoaded);
                    it.remove();
                    setDirty();
                    return;
                }
            }
        }
    }

    public void revertConversion(UUID entity, ServerLevel level) {
        revertConversion(entity, level, UnaryOperator.identity());
    }

    private Set<RevertibleEntity> getOrCreateConverted(UUID owner, EntityLocation location) {
        return convertedEntities.row(owner).computeIfAbsent(location, l -> new ObjectOpenHashSet<>());
    }

    public void putConverted(UUID performer, Entity entity) {
        if (!isConverted(entity.getUUID())) {
            getOrCreateConverted(performer, EntityLocation.of(entity)).add(RevertibleEntity.of(entity));
            setDirty();
        }
    }

    private Set<RevertibleEntity> getAllConverted() {
        Set<RevertibleEntity> converted = new ObjectOpenHashSet<>();
        for (Set<RevertibleEntity> entities : convertedEntities.values()) {
            converted.addAll(entities);
        }
        return converted;
    }

    public boolean isConverted(UUID entity) {
        for (RevertibleEntity revertible : getAllConverted()) {
            if (revertible.uuid().equals(entity))
                return true;
        }
        return false;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        Function<EntityLocation, Tag> locationEncoder = MineraculousNbtUtils.codecEncoder(EntityLocation.CODEC);
        Function<RevertibleEntity, Tag> revertibleEncoder = MineraculousNbtUtils.codecEncoder(RevertibleEntity.CODEC);
        tag.put("TrackedAndRelated", MineraculousNbtUtils.writeStringKeyedMultimap(trackedAndRelatedEntities, UUID::toString, NbtUtils::createUUID));
        tag.put("Revertible", MineraculousNbtUtils.writeStringRowKeyedTable(revertibleEntities, UUID::toString, locationEncoder, values -> MineraculousNbtUtils.writeCollection(values, revertibleEncoder)));
        tag.put("Removable", MineraculousNbtUtils.writeStringKeyedMultimap(removableEntities, UUID::toString, NbtUtils::createUUID));
        tag.put("Converted", MineraculousNbtUtils.writeStringRowKeyedTable(convertedEntities, UUID::toString, locationEncoder, values -> MineraculousNbtUtils.writeCollection(values, revertibleEncoder)));
        return tag;
    }

    public static AbilityReversionEntityData load(CompoundTag tag) {
        Function<Tag, EntityLocation> locationDecoder = MineraculousNbtUtils.codecDecoder(EntityLocation.CODEC);
        Function<Tag, RevertibleEntity> revertibleDecoder = MineraculousNbtUtils.codecDecoder(RevertibleEntity.CODEC);
        AbilityReversionEntityData data = new AbilityReversionEntityData();
        data.trackedAndRelatedEntities.putAll(MineraculousNbtUtils.readStringKeyedMultimap(HashMultimap::create, tag.getCompound("TrackedAndRelated"), UUID::fromString, NbtUtils::loadUUID));
        // Java gets weird about the generics in the set here, requires a more generic declaration
        Table<UUID, EntityLocation, Set<RevertibleEntity>> table = MineraculousNbtUtils.readStringRowKeyedTable(HashBasedTable::create, tag.getCompound("Revertible"), UUID::fromString, locationDecoder, t -> MineraculousNbtUtils.readCollection(ReferenceOpenHashSet::new, (ListTag) t, revertibleDecoder));
        data.revertibleEntities.putAll(table);
        data.removableEntities.putAll(MineraculousNbtUtils.readStringKeyedMultimap(HashMultimap::create, tag.getCompound("Removable"), UUID::fromString, NbtUtils::loadUUID));
        table = MineraculousNbtUtils.readStringRowKeyedTable(HashBasedTable::create, tag.getCompound("Converted"), UUID::fromString, locationDecoder, t -> MineraculousNbtUtils.readCollection(ReferenceOpenHashSet::new, (ListTag) t, revertibleDecoder));
        data.convertedEntities.putAll(table);
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
