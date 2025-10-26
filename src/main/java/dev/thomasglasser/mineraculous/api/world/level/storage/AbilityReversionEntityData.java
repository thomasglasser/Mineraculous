package dev.thomasglasser.mineraculous.api.world.level.storage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
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
    private final Map<UUID, List<UUID>> trackedAndRelatedEntities = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, List<Pair<ResourceKey<Level>, CompoundTag>>> revertibleEntities = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, List<UUID>> removableEntities = new Object2ObjectOpenHashMap<>();
    private final Table<UUID, UUID, Pair<ResourceKey<Level>, CompoundTag>> convertedEntities = HashBasedTable.create();

    public static AbilityReversionEntityData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(AbilityReversionEntityData.factory(), AbilityReversionEntityData.FILE_ID);
    }

    public static SavedData.Factory<AbilityReversionEntityData> factory() {
        return new SavedData.Factory<>(AbilityReversionEntityData::new, (p_294039_, p_324123_) -> load(p_294039_), DataFixTypes.LEVEL);
    }

    public void tick(Entity entity) {
        if (isBeingTracked(entity.getUUID()) && entity.tickCount % SharedConstants.TICKS_PER_SECOND * 5 == 0) {
            List<UUID> alreadyRelated = getRelatedEntities(entity.getUUID());
            List<Entity> newRelated = entity.level().getEntitiesOfClass(Entity.class, entity.getBoundingBox().inflate(16), target -> !alreadyRelated.contains(target.getUUID()) && (target.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed() || target.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()));
            for (Entity related : newRelated) {
                if (related.getUUID() != entity.getUUID()) {
                    putRelatedEntity(entity.getUUID(), related.getUUID());
                }
            }
        }
    }

    public boolean isBeingTracked(UUID uuid) {
        return trackedAndRelatedEntities.containsKey(uuid);
    }

    public @Nullable UUID getTrackedEntity(UUID uuid) {
        for (UUID tracked : trackedAndRelatedEntities.keySet()) {
            if (trackedAndRelatedEntities.get(tracked).contains(uuid)) {
                return tracked;
            }
        }
        return null;
    }

    public List<UUID> getRelatedEntities(UUID uuid) {
        return trackedAndRelatedEntities.get(uuid);
    }

    public List<UUID> getAndClearTrackedAndRelatedEntities(UUID uuid) {
        List<UUID> all = new ReferenceArrayList<>(trackedAndRelatedEntities.remove(uuid));
        all.add(uuid);
        setDirty();
        return all;
    }

    public void putRelatedEntity(UUID trackedEntity, UUID relatedEntity) {
        if (trackedEntity.equals(relatedEntity))
            return;
        if (!trackedAndRelatedEntities.containsKey(trackedEntity))
            trackedAndRelatedEntities.put(trackedEntity, new ObjectArrayList<>());
        List<UUID> related = trackedAndRelatedEntities.get(trackedEntity);
        if (!related.contains(relatedEntity)) {
            related.add(relatedEntity);
            setDirty();
        }
    }

    public void startTracking(UUID uuid) {
        trackedAndRelatedEntities.computeIfAbsent(uuid, p -> new ObjectArrayList<>());
        setDirty();
    }

    public void revert(UUID owner, ServerLevel level, Consumer<Entity> onReverted) {
        if (revertibleEntities.containsKey(owner)) {
            for (Pair<ResourceKey<Level>, CompoundTag> data : revertibleEntities.get(owner)) {
                ServerLevel targetLevel = level.getServer().getLevel(data.left());
                CompoundTag entityData = data.right();
                UUID entityId = entityData.getUUID("UUID");
                Entity entity = MineraculousEntityUtils.findEntity(level, entityId);
                if (entity != null && !entity.isRemoved()) {
                    entity.load(entityData);
                } else if (targetLevel != null) {
                    entity = EntityType.loadEntityRecursive(entityData, targetLevel, e -> e);
                    if (entity != null)
                        targetLevel.addFreshEntity(entity);
                }
                if (entity != null) {
                    onReverted.accept(entity);
                }
            }
            revertibleEntities.remove(owner);
            setDirty();
        }
        if (removableEntities.containsKey(owner)) {
            for (UUID id : removableEntities.get(owner)) {
                Entity entity = MineraculousEntityUtils.findEntity(level, id);
                if (entity != null) {
                    entity.discard();
                }
            }
            removableEntities.remove(owner);
            setDirty();
        }
    }

    public void putRevertible(UUID owner, Entity entity) {
        putRelatedEntity(owner, entity.getUUID());
        if (entity instanceof PartEntity<?> partEntity) {
            putRevertible(owner, partEntity.getParent());
            return;
        }
        if (!entity.getType().canSerialize())
            return;
        if (!revertibleEntities.containsKey(owner))
            revertibleEntities.put(owner, new ReferenceArrayList<>());
        CompoundTag entityData = new CompoundTag();
        entity.save(entityData);
        revertibleEntities.get(owner).add(Pair.of(entity.level().dimension(), entityData));
        setDirty();
    }

    public void putRemovable(UUID owner, Entity entity) {
        putRelatedEntity(owner, entity.getUUID());
        if (entity instanceof PartEntity<?> partEntity) {
            putRemovable(owner, partEntity.getParent());
            return;
        }
        if (!removableEntities.containsKey(owner))
            removableEntities.put(owner, new ReferenceArrayList<>());
        removableEntities.get(owner).add(entity.getUUID());
        setDirty();
    }

    public UUID getCause(Entity entity, ServerLevel level) {
        for (Map.Entry<UUID, List<Pair<ResourceKey<Level>, CompoundTag>>> entry : revertibleEntities.entrySet()) {
            for (Pair<ResourceKey<Level>, CompoundTag> data : entry.getValue()) {
                CompoundTag entityData = data.right();
                UUID id = entityData.getUUID("UUID");
                Entity entity1 = MineraculousEntityUtils.findEntity(level, id);
                if (entity1 == entity)
                    return entry.getKey();
            }
        }
        for (Map.Entry<UUID, List<UUID>> entry : removableEntities.entrySet()) {
            for (UUID id : entry.getValue()) {
                Entity entity1 = MineraculousEntityUtils.findEntity(level, id);
                if (entity1 == entity)
                    return entry.getKey();
            }
        }
        return null;
    }

    public void putConverted(UUID performer, Entity entity) {
        CompoundTag tag = new CompoundTag();
        entity.save(tag);
        convertedEntities.put(performer, entity.getUUID(), Pair.of(entity.level().dimension(), tag));
        setDirty();
    }

    public boolean isConverted(UUID entity) {
        return convertedEntities.containsColumn(entity);
    }

    public void revertConversions(UUID performer, ServerLevel level) {
        Map<UUID, Pair<ResourceKey<Level>, CompoundTag>> row = convertedEntities.row(performer);
        Collection<Pair<ResourceKey<Level>, CompoundTag>> conversions = row.values();
        for (Pair<ResourceKey<Level>, CompoundTag> data : conversions) {
            revertConversion(data, level);
        }
        row.clear();
        setDirty();
    }

    public @Nullable Entity revertConversion(UUID performer, UUID entity, ServerLevel level) {
        Pair<ResourceKey<Level>, CompoundTag> original = convertedEntities.remove(performer, entity);
        if (original != null) {
            return revertConversion(original, level);
        }
        return null;
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
        ListTag trackedAndRelated = new ListTag();
        for (Map.Entry<UUID, List<UUID>> entry : trackedAndRelatedEntities.entrySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("TrackedEntity", entry.getKey());
            CompoundTag relatedEntities = new CompoundTag();
            for (UUID relatedEntity : entry.getValue())
                relatedEntities.putUUID(relatedEntity.toString(), relatedEntity);
            compoundTag.put("RelatedEntities", relatedEntities);
            trackedAndRelated.add(compoundTag);
        }
        tag.put("TrackedAndRelatedEntities", trackedAndRelated);
        ListTag revertible = new ListTag();
        for (Map.Entry<UUID, List<Pair<ResourceKey<Level>, CompoundTag>>> entry : revertibleEntities.entrySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", entry.getKey());
            ListTag entities = new ListTag();
            for (Pair<ResourceKey<Level>, CompoundTag> entityData : entry.getValue()) {
                CompoundTag entity = new CompoundTag();
                entity.put("Dimension", Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, entityData.left()).getOrThrow());
                entity.put("Data", entityData.right());
                entities.add(entity);
            }
            compoundTag.put("Entities", entities);
            revertible.add(compoundTag);
        }
        tag.put("RevertibleEntities", revertible);
        ListTag removable = new ListTag();
        for (Map.Entry<UUID, List<UUID>> entry : removableEntities.entrySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", entry.getKey());
            CompoundTag entities = new CompoundTag();
            for (UUID other : entry.getValue())
                entities.putUUID(other.toString(), other);
            compoundTag.put("Entities", entities);
            removable.add(compoundTag);
        }
        tag.put("RemovableEntities", removable);
        ListTag converted = new ListTag();
        for (UUID performer : convertedEntities.rowKeySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", performer);
            ListTag entities = new ListTag();
            for (Map.Entry<UUID, Pair<ResourceKey<Level>, CompoundTag>> entry : convertedEntities.row(performer).entrySet()) {
                CompoundTag entityData = new CompoundTag();
                entityData.putUUID("UUID", entry.getKey());
                entityData.put("Dimension", Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, entry.getValue().left()).getOrThrow());
                entityData.put("Data", entry.getValue().right());
                entities.add(entityData);
            }
            compoundTag.put("Entities", entities);
            converted.add(compoundTag);
        }
        tag.put("ConvertedEntities", converted);
        return tag;
    }

    public static AbilityReversionEntityData load(CompoundTag tag) {
        AbilityReversionEntityData abilityReversionEntityData = new AbilityReversionEntityData();
        ListTag listTag = tag.getList("TrackedAndRelatedEntities", ListTag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compoundTag = listTag.getCompound(i);
            UUID trackedEntity = compoundTag.getUUID("TrackedEntity");
            CompoundTag relatedEntities = compoundTag.getCompound("RelatedEntities");
            List<UUID> relatedEntityList = new ObjectArrayList<>();
            for (String key : relatedEntities.getAllKeys())
                relatedEntityList.add(relatedEntities.getUUID(key));
            abilityReversionEntityData.trackedAndRelatedEntities.put(trackedEntity, relatedEntityList);
        }
        ListTag revertibleEntities = tag.getList("RevertibleEntities", ListTag.TAG_COMPOUND);
        for (int i = 0; i < revertibleEntities.size(); i++) {
            CompoundTag compoundTag = revertibleEntities.getCompound(i);
            UUID owner = compoundTag.getUUID("UUID");
            ListTag entities = compoundTag.getList("Entities", ListTag.TAG_COMPOUND);
            List<Pair<ResourceKey<Level>, CompoundTag>> entityList = new ObjectArrayList<>();
            for (int j = 0; j < entities.size(); j++) {
                CompoundTag entity = entities.getCompound(j);
                ResourceKey<Level> dimension = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, entity.get("Dimension")).getOrThrow();
                CompoundTag data = entity.getCompound("Data");
                entityList.add(Pair.of(dimension, data));
            }
            abilityReversionEntityData.revertibleEntities.put(owner, entityList);
        }
        ListTag removableEntities = tag.getList("RemovableEntities", ListTag.TAG_COMPOUND);
        for (int i = 0; i < removableEntities.size(); i++) {
            CompoundTag compoundTag = removableEntities.getCompound(i);
            UUID owner = compoundTag.getUUID("UUID");
            CompoundTag entities = compoundTag.getCompound("Entities");
            List<UUID> entityList = new ObjectArrayList<>();
            for (String key : entities.getAllKeys())
                entityList.add(entities.getUUID(key));
            abilityReversionEntityData.removableEntities.put(owner, entityList);
        }
        ListTag converted = tag.getList("ConvertedEntities", ListTag.TAG_COMPOUND);
        for (int i = 0; i < converted.size(); i++) {
            CompoundTag compoundTag = converted.getCompound(i);
            UUID performer = compoundTag.getUUID("UUID");
            ListTag entities = compoundTag.getList("Entities", ListTag.TAG_COMPOUND);
            for (int j = 0; j < entities.size(); j++) {
                CompoundTag entityData = entities.getCompound(j);
                UUID entity = entityData.getUUID("UUID");
                ResourceKey<Level> dimension = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, entityData.get("Dimension")).getOrThrow();
                CompoundTag data = entityData.getCompound("Data");
                abilityReversionEntityData.convertedEntities.put(performer, entity, Pair.of(dimension, data));
            }
        }
        return abilityReversionEntityData;
    }
}
