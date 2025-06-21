package dev.thomasglasser.mineraculous.api.world.level.storage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

public class AbilityReversionEntityData extends SavedData {
    public static final String FILE_ID = "ability_reversion_entity";
    private final Map<UUID, List<UUID>> trackedAndRelatedEntities = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, List<CompoundTag>> revertableEntities = new Object2ObjectOpenHashMap<>();
    private final Table<UUID, UUID, CompoundTag> convertedEntities = HashBasedTable.create();

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
        if (!trackedAndRelatedEntities.containsKey(trackedEntity))
            trackedAndRelatedEntities.put(trackedEntity, new ObjectArrayList<>());
        List<UUID> related = trackedAndRelatedEntities.get(trackedEntity);
        if (!related.contains(relatedEntity)) {
            related.add(relatedEntity);
            setDirty();
        }
    }

    public void startTracking(UUID uuid) {
        trackedAndRelatedEntities.computeIfAbsent(uuid, p -> new ObjectArrayList<>()).add(uuid);
        setDirty();
    }

    public void revert(UUID owner, ServerLevel level, Consumer<Entity> onReverted) {
        if (revertableEntities.containsKey(owner)) {
            for (CompoundTag entityData : revertableEntities.get(owner)) {
                UUID entityId = entityData.getUUID("UUID");
                Entity entity = level.getEntity(entityId);
                if (entity == null || entity.isRemoved()) {
                    entity = EntityType.loadEntityRecursive(entityData, level, e -> e);
                    if (entity != null)
                        level.addFreshEntity(entity);
                } else {
                    entity.load(entityData);
                }
                onReverted.accept(entity);
            }
            revertableEntities.remove(owner);
            setDirty();
        }
    }

    public void putRevertable(UUID owner, Entity entity) {
        putRelatedEntity(owner, entity.getUUID());
        if (entity instanceof PartEntity<?> partEntity) {
            putRevertable(owner, partEntity.getParent());
            return;
        }
        if (!entity.getType().canSerialize())
            return;
        if (!revertableEntities.containsKey(owner))
            revertableEntities.put(owner, new ReferenceArrayList<>());
        CompoundTag entityData = new CompoundTag();
        entity.save(entityData);
        revertableEntities.get(owner).add(entityData);
        setDirty();
    }

    public UUID getCause(Entity entity, ServerLevel level) {
        for (Map.Entry<UUID, List<CompoundTag>> entry : revertableEntities.entrySet()) {
            for (CompoundTag entityData : entry.getValue()) {
                UUID id = entityData.getUUID("UUID");
                Entity entity1 = level.getEntity(id);
                if (entity1 == entity)
                    return entry.getKey();
            }
        }
        return null;
    }

    public void putConverted(UUID performer, UUID entity, CompoundTag original) {
        convertedEntities.put(performer, entity, original);
        setDirty();
    }

    public boolean isConverted(UUID entity) {
        return convertedEntities.containsColumn(entity);
    }

    public void revertConversions(UUID performer, ServerLevel level) {
        Collection<CompoundTag> conversions = convertedEntities.row(performer).values();
        for (CompoundTag entityData : conversions) {
            revertConversion(entityData, level);
            convertedEntities.remove(performer, entityData.getUUID("UUID"));
        }
    }

    public @Nullable Entity revertConversion(UUID performer, UUID entity, ServerLevel level) {
        CompoundTag original = convertedEntities.remove(performer, entity);
        if (original != null) {
            return revertConversion(original, level);
        }
        return null;
    }

    public @Nullable Entity revertConversion(UUID entity, ServerLevel level) {
        for (UUID performer : convertedEntities.rowKeySet()) {
            CompoundTag original = convertedEntities.remove(performer, entity);
            if (original != null) {
                return revertConversion(original, level);
            }
        }
        return null;
    }

    private Entity revertConversion(CompoundTag original, ServerLevel level) {
        UUID entityId = original.getUUID("UUID");
        Entity entity = level.getEntity(entityId);
        Vec3 pos = null;
        if (entity != null && !entity.isRemoved()) {
            pos = entity.position();
            entity.discard();
        }
        entity = EntityType.loadEntityRecursive(original, level, e -> e);
        if (entity != null) {
            if (pos != null) {
                entity.setPos(pos);
            }
            level.addFreshEntity(entity);
        }
        setDirty();
        return entity;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        for (Map.Entry<UUID, List<UUID>> entry : trackedAndRelatedEntities.entrySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("TrackedEntity", entry.getKey());
            CompoundTag relatedEntities = new CompoundTag();
            for (UUID relatedEntity : entry.getValue())
                relatedEntities.putUUID(relatedEntity.toString(), relatedEntity);
            compoundTag.put("RelatedEntities", relatedEntities);
            listTag.add(compoundTag);
        }
        tag.put("TrackedAndRelatedEntities", listTag);
        ListTag revertable = new ListTag();
        for (Map.Entry<UUID, List<CompoundTag>> entry : revertableEntities.entrySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", entry.getKey());
            ListTag entities = new ListTag();
            entities.addAll(entry.getValue());
            compoundTag.put("Entities", entities);
            revertable.add(compoundTag);
        }
        tag.put("RevertableEntities", revertable);
        ListTag converted = new ListTag();
        for (UUID performer : convertedEntities.rowKeySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", performer);
            ListTag entities = new ListTag();
            for (Map.Entry<UUID, CompoundTag> entry : convertedEntities.row(performer).entrySet()) {
                CompoundTag entityData = new CompoundTag();
                entityData.putUUID("UUID", entry.getKey());
                entityData.put("Data", entry.getValue());
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
        ListTag recoverableEntities = tag.getList("RevertableEntities", ListTag.TAG_COMPOUND);
        for (int i = 0; i < recoverableEntities.size(); i++) {
            CompoundTag compoundTag = recoverableEntities.getCompound(i);
            UUID owner = compoundTag.getUUID("UUID");
            ListTag entities = compoundTag.getList("Entities", ListTag.TAG_COMPOUND);
            List<CompoundTag> entityList = new ObjectArrayList<>();
            for (int j = 0; j < entities.size(); j++)
                entityList.add(entities.getCompound(j));
            abilityReversionEntityData.revertableEntities.put(owner, entityList);
        }
        ListTag converted = tag.getList("ConvertedEntities", ListTag.TAG_COMPOUND);
        for (int i = 0; i < converted.size(); i++) {
            CompoundTag compoundTag = converted.getCompound(i);
            UUID performer = compoundTag.getUUID("UUID");
            ListTag entities = compoundTag.getList("Entities", ListTag.TAG_COMPOUND);
            for (int j = 0; j < entities.size(); j++) {
                CompoundTag entityData = entities.getCompound(j);
                UUID entity = entityData.getUUID("UUID");
                CompoundTag data = entityData.getCompound("Data");
                abilityReversionEntityData.convertedEntities.put(performer, entity, data);
            }
        }
        return abilityReversionEntityData;
    }
}
