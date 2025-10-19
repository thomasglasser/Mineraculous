package dev.thomasglasser.mineraculous.api.world.level.storage;

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
    private final Multimap<UUID, UUID> relatedEntities = MultimapBuilder.hashKeys().arrayListValues().build();
    private final Table<UUID, Pair<ResourceKey<Level>, Vec3>, List<CompoundTag>> revertibleEntities = HashBasedTable.create();
    private final Table<UUID, Pair<ResourceKey<Level>, Vec3>, List<UUID>> removableEntities = HashBasedTable.create();
    private final Table<UUID, UUID, Pair<ResourceKey<Level>, CompoundTag>> convertedEntities = HashBasedTable.create();

    public static AbilityReversionEntityData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(AbilityReversionEntityData.factory(), AbilityReversionEntityData.FILE_ID);
    }

    public static SavedData.Factory<AbilityReversionEntityData> factory() {
        return new SavedData.Factory<>(AbilityReversionEntityData::new, (p_294039_, p_324123_) -> load(p_294039_), DataFixTypes.LEVEL);
    }

    public void tick(Entity entity) {
        UUID uuid = entity.getUUID();
        if (entity.tickCount % SharedConstants.TICKS_PER_SECOND == 0 && relatedEntities.containsKey(uuid)) {
            for (Entity related : entity.level().getEntitiesOfClass(Entity.class, entity.getBoundingBox().inflate(16), this::shouldBeTracked)) {
                putRelatedEntity(uuid, related.getUUID());
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

    public @Nullable UUID getTrackedEntity(UUID uuid) {
        for (UUID tracked : relatedEntities.keySet()) {
            if (relatedEntities.get(tracked).contains(uuid)) {
                return tracked;
            }
        }
        return null;
    }

    public Collection<UUID> getAndClearTrackedAndRelatedEntities(UUID uuid) {
        Collection<UUID> trackedAndRelated = relatedEntities.removeAll(uuid);
        setDirty();
        return trackedAndRelated;
    }

    public void startTracking(UUID uuid) {
        relatedEntities.put(uuid, uuid);
        setDirty();
    }

    public Multimap<ResourceKey<Level>, Vec3> getReversionPositions(UUID uuid) {
        Multimap<ResourceKey<Level>, Vec3> positions = MultimapBuilder.hashKeys().arrayListValues().build();
        for (Pair<ResourceKey<Level>, Vec3> pos : revertibleEntities.row(uuid).keySet()) {
            positions.put(pos.left(), pos.right());
        }
        for (Pair<ResourceKey<Level>, Vec3> pos : removableEntities.row(uuid).keySet()) {
            positions.put(pos.left(), pos.right());
        }
        return positions;
    }

    public void revert(UUID owner, ServerLevel level, Vec3 pos) {
        Pair<ResourceKey<Level>, Vec3> loc = Pair.of(level.dimension(), pos);
        List<CompoundTag> revertible = revertibleEntities.remove(owner, loc);
        if (revertible != null) {
            for (CompoundTag data : revertible) {
                UUID entityId = data.getUUID("UUID");
                Entity entity = MineraculousEntityUtils.findEntity(level, entityId);
                if (entity != null && !entity.isRemoved()) {
                    entity.load(data);
                } else {
                    entity = EntityType.loadEntityRecursive(data, level, e -> e);
                    if (entity != null)
                        level.addFreshEntity(entity);
                }
            }
            setDirty();
        }
        List<UUID> removable = removableEntities.remove(owner, loc);
        if (removable != null) {
            for (UUID id : removable) {
                Entity entity = MineraculousEntityUtils.findEntity(level, id);
                if (entity != null) {
                    entity.discard();
                }
            }
            setDirty();
        }
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
        CompoundTag entityData = new CompoundTag();
        entity.save(entityData);
        getOrCreateRevertible(owner, Pair.of(entity.level().dimension(), entity.position())).add(entityData);
        setDirty();
    }

    public void putRemovable(UUID owner, Entity entity) {
        putRelatedEntity(owner, entity.getUUID());
        if (entity instanceof PartEntity<?> partEntity) {
            putRemovable(owner, partEntity.getParent());
            return;
        }
        getOrCreateRemovable(owner, Pair.of(entity.level().dimension(), entity.position())).add(entity.getUUID());
        setDirty();
    }

    public @Nullable UUID findCause(Entity entity, ServerLevel level) {
        for (Table.Cell<UUID, Pair<ResourceKey<Level>, Vec3>, List<CompoundTag>> cell : revertibleEntities.cellSet()) {
            for (CompoundTag data : cell.getValue()) {
                if (entity == MineraculousEntityUtils.findEntity(level, data.getUUID("UUID")))
                    return cell.getRowKey();
            }
        }
        for (Table.Cell<UUID, Pair<ResourceKey<Level>, Vec3>, List<UUID>> cell : removableEntities.cellSet()) {
            for (UUID uuid : cell.getValue()) {
                if (entity == MineraculousEntityUtils.findEntity(level, uuid))
                    return cell.getRowKey();
            }
        }
        return null;
    }

    public void putConverted(UUID owner, Entity entity) {
        CompoundTag tag = new CompoundTag();
        entity.save(tag);
        convertedEntities.put(owner, entity.getUUID(), Pair.of(entity.level().dimension(), tag));
        setDirty();
    }

    public boolean isConverted(UUID entity) {
        return convertedEntities.containsColumn(entity);
    }

    public @Nullable Entity revertConversion(UUID entity, ServerLevel level) {
        for (UUID performer : convertedEntities.rowKeySet()) {
            Pair<ResourceKey<Level>, CompoundTag> original = convertedEntities.remove(performer, entity);
            if (original != null) {
                return revertConversion(original, level);
            }
        }
        return null;
    }

    private Entity revertConversion(Pair<ResourceKey<Level>, CompoundTag> original, ServerLevel level) {
        CompoundTag entityData = original.right();
        UUID entityId = entityData.getUUID("UUID");
        Entity entity = MineraculousEntityUtils.findEntity(level, entityId);
        Vec3 pos = null;
        ServerLevel targetLevel;
        if (entity != null && !entity.isRemoved()) {
            pos = entity.position();
            targetLevel = (ServerLevel) entity.level();
            entity.discard();
        } else {
            targetLevel = level.getServer().getLevel(original.left());
        }

        if (targetLevel != null) {
            entity = EntityType.loadEntityRecursive(entityData, targetLevel, e -> e);
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
        ListTag related = new ListTag();
        for (UUID uuid : relatedEntities.keySet()) {
            ListTag entities = new ListTag();
            for (UUID relatedEntity : relatedEntities.get(uuid)) {
                entities.add(NbtUtils.createUUID(relatedEntity));
            }
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", uuid);
            compoundTag.put("Related", entities);
            related.add(compoundTag);
        }
        ListTag revertible = new ListTag();
        for (UUID uuid : revertibleEntities.rowKeySet()) {
            ListTag positions = new ListTag();
            for (Pair<ResourceKey<Level>, Vec3> pos : revertibleEntities.row(uuid).keySet()) {
                ListTag entities = new ListTag();
                entities.addAll(revertibleEntities.get(uuid, pos));
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.put("Dimension", Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, pos.left()).getOrThrow());
                compoundTag.put("Pos", Vec3.CODEC.encodeStart(NbtOps.INSTANCE, pos.right()).getOrThrow());
                compoundTag.put("Entities", entities);
                positions.add(compoundTag);
            }
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", uuid);
            compoundTag.put("Positions", positions);
            revertible.add(compoundTag);
        }
        ListTag removable = new ListTag();
        for (UUID uuid : removableEntities.rowKeySet()) {
            ListTag positions = new ListTag();
            for (Pair<ResourceKey<Level>, Vec3> pos : removableEntities.row(uuid).keySet()) {
                ListTag entities = new ListTag();
                for (UUID id : removableEntities.get(uuid, pos)) {
                    entities.add(NbtUtils.createUUID(id));
                }
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.put("Dimension", Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, pos.left()).getOrThrow());
                compoundTag.put("Pos", Vec3.CODEC.encodeStart(NbtOps.INSTANCE, pos.right()).getOrThrow());
                compoundTag.put("Entities", entities);
                positions.add(compoundTag);
            }
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", uuid);
            compoundTag.put("Positions", positions);
            removable.add(compoundTag);
        }
        ListTag converted = new ListTag();
        for (UUID uuid : convertedEntities.rowKeySet()) {
            ListTag entities = new ListTag();
            for (UUID id : convertedEntities.row(uuid).keySet()) {
                Pair<ResourceKey<Level>, CompoundTag> data = convertedEntities.get(uuid, id);
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putUUID("UUID", id);
                compoundTag.put("Dimension", Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, data.left()).getOrThrow());
                compoundTag.put("Entity", data.right());
                entities.add(compoundTag);
            }
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", uuid);
            compoundTag.put("Entities", entities);
            converted.add(compoundTag);
        }
        tag.put("Related", related);
        tag.put("Revertible", revertible);
        tag.put("Removable", removable);
        tag.put("Converted", converted);
        return tag;
    }

    public static AbilityReversionEntityData load(CompoundTag tag) {
        AbilityReversionEntityData data = new AbilityReversionEntityData();
        ListTag related = tag.getList("Related", ListTag.TAG_COMPOUND);
        for (int i = 0; i < related.size(); i++) {
            CompoundTag compoundTag = related.getCompound(i);
            UUID uuid = compoundTag.getUUID("UUID");
            for (Tag entityId : compoundTag.getList("Entities", ListTag.TAG_INT_ARRAY)) {
                data.relatedEntities.put(uuid, NbtUtils.loadUUID(entityId));
            }
        }
        ListTag revertible = tag.getList("Revertible", ListTag.TAG_COMPOUND);
        for (int i = 0; i < revertible.size(); i++) {
            CompoundTag compoundTag = revertible.getCompound(i);
            UUID uuid = compoundTag.getUUID("UUID");
            ListTag positions = compoundTag.getList("Positions", ListTag.TAG_COMPOUND);
            for (int j = 0; j < positions.size(); j++) {
                CompoundTag position = positions.getCompound(j);
                ResourceKey<Level> dimension = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, position.get("Dimension")).getOrThrow();
                Vec3 pos = net.minecraft.world.phys.Vec3.CODEC.parse(NbtOps.INSTANCE, position.get("Pos")).getOrThrow();
                List<CompoundTag> revertibles = data.getOrCreateRevertible(uuid, Pair.of(dimension, pos));
                ListTag entities = position.getList("Entities", ListTag.TAG_COMPOUND);
                for (int k = 0; k < entities.size(); k++) {
                    revertibles.add(entities.getCompound(k));
                }
            }
        }
        ListTag removable = tag.getList("Removable", ListTag.TAG_COMPOUND);
        for (int i = 0; i < removable.size(); i++) {
            CompoundTag compoundTag = removable.getCompound(i);
            UUID uuid = compoundTag.getUUID("UUID");
            ListTag positions = compoundTag.getList("Positions", ListTag.TAG_COMPOUND);
            for (int j = 0; j < positions.size(); j++) {
                CompoundTag position = positions.getCompound(j);
                ResourceKey<Level> dimension = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, position.get("Dimension")).getOrThrow();
                Vec3 pos = Vec3.CODEC.parse(NbtOps.INSTANCE, position.get("Pos")).getOrThrow();
                List<UUID> removables = data.getOrCreateRemovable(uuid, Pair.of(dimension, pos));
                ListTag entities = position.getList("Entities", ListTag.TAG_COMPOUND);
                for (Tag entityId : entities) {
                    removables.add(NbtUtils.loadUUID(entityId));
                }
            }
        }
        ListTag converted = tag.getList("Converted", ListTag.TAG_COMPOUND);
        for (int i = 0; i < converted.size(); i++) {
            CompoundTag compoundTag = converted.getCompound(i);
            UUID uuid = compoundTag.getUUID("UUID");
            ListTag entities = compoundTag.getList("Entities", ListTag.TAG_COMPOUND);
            for (int j = 0; j < entities.size(); j++) {
                CompoundTag compoundTag1 = entities.getCompound(j);
                UUID id = compoundTag1.getUUID("UUID");
                ResourceKey<Level> dimension = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, compoundTag1.get("Dimension")).getOrThrow();
                CompoundTag entity = compoundTag1.getCompound("Entity");
                data.convertedEntities.put(uuid, id, Pair.of(dimension, entity));
            }
        }
        return data;
    }
}
