package dev.thomasglasser.mineraculous.world.level.storage;

import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

public class AbilityReversionEntityData extends SavedData {
    public static final String FILE_ID = "ability_reversion_entity";
    private final Map<UUID, List<UUID>> trackedAndRelatedEntities = new HashMap<>();
    private final Map<UUID, List<CompoundTag>> recoverableEntities = new HashMap<>();

    public static AbilityReversionEntityData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(AbilityReversionEntityData.factory(), AbilityReversionEntityData.FILE_ID);
    }

    public static SavedData.Factory<AbilityReversionEntityData> factory() {
        return new SavedData.Factory<>(AbilityReversionEntityData::new, (p_294039_, p_324123_) -> load(p_294039_), DataFixTypes.LEVEL);
    }

    public void tick(Entity entity) {
        if (isBeingTracked(entity.getUUID()) && entity.tickCount % SharedConstants.TICKS_PER_SECOND * 5 == 0) {
            List<UUID> alreadyRelated = getRelatedEntities(entity.getUUID());
            List<LivingEntity> newRelated = entity.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(16), livingEntity -> !alreadyRelated.contains(livingEntity.getUUID()) && (livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUSES).isTransformed() || livingEntity.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()));
            for (LivingEntity related : newRelated) {
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
        return trackedAndRelatedEntities.keySet().stream().filter(trackedEntity -> trackedAndRelatedEntities.get(trackedEntity).contains(uuid)).findFirst().orElse(null);
    }

    public List<UUID> getRelatedEntities(UUID uuid) {
        return trackedAndRelatedEntities.get(uuid);
    }

    public List<UUID> getTrackedAndRelatedEntities(UUID uuid) {
        ArrayList<UUID> all = new ArrayList<>(trackedAndRelatedEntities.get(uuid));
        all.add(uuid);
        return all;
    }

    public void putRelatedEntity(UUID trackedEntity, UUID relatedEntity) {
        if (!trackedAndRelatedEntities.containsKey(trackedEntity))
            trackedAndRelatedEntities.put(trackedEntity, new ReferenceArrayList<>());
        List<UUID> related = trackedAndRelatedEntities.get(trackedEntity);
        if (!related.contains(relatedEntity)) {
            related.add(relatedEntity);
            setDirty();
        }
    }

    public void startTracking(UUID uuid) {
        trackedAndRelatedEntities.computeIfAbsent(uuid, p -> new ArrayList<>()).add(uuid);
        setDirty();
    }

    public void stopTracking(UUID uuid) {
        trackedAndRelatedEntities.remove(uuid);
        setDirty();
    }

    public void revert(UUID owner, ServerLevel level) {
        if (recoverableEntities.containsKey(owner)) {
            for (CompoundTag entityData : recoverableEntities.get(owner)) {
                UUID entityId = entityData.getUUID("UUID");
                Entity entity = level.getEntity(entityId);
                if (entity == null || entity.isRemoved()) {
                    entity = EntityType.loadEntityRecursive(entityData, level, e -> e);
                    if (entity != null)
                        level.addFreshEntity(entity);
                } else {
                    entity.load(entityData);
                }
            }
            recoverableEntities.remove(owner);
            setDirty();
        }
    }

    public void putRecoverable(UUID owner, Entity entity) {
        putRelatedEntity(owner, entity.getUUID());
        if (entity instanceof PartEntity<?> partEntity) {
            putRecoverable(owner, partEntity.getParent());
            return;
        }
        if (!entity.getType().canSerialize())
            return;
        if (!recoverableEntities.containsKey(owner))
            recoverableEntities.put(owner, new ArrayList<>());
        CompoundTag entityData = new CompoundTag();
        entity.save(entityData);
        recoverableEntities.get(owner).add(entityData);
        setDirty();
    }

    public UUID getRecoverer(Entity entity, ServerLevel level) {
        for (Map.Entry<UUID, List<CompoundTag>> entry : recoverableEntities.entrySet()) {
            for (CompoundTag entityData : entry.getValue()) {
                UUID id = entityData.getUUID("UUID");
                Entity entity1 = level.getEntity(id);
                if (entity1 == entity)
                    return entry.getKey();
            }
        }
        return null;
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
        ListTag recoverable = new ListTag();
        for (Map.Entry<UUID, List<CompoundTag>> entry : recoverableEntities.entrySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", entry.getKey());
            ListTag entities = new ListTag();
            entities.addAll(entry.getValue());
            compoundTag.put("Entities", entities);
            recoverable.add(compoundTag);
        }
        tag.put("RecoverableEntities", recoverable);
        return tag;
    }

    public static AbilityReversionEntityData load(CompoundTag tag) {
        AbilityReversionEntityData abilityReversionEntityData = new AbilityReversionEntityData();
        ListTag listTag = tag.getList("TrackedAndRelatedEntities", 10);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compoundTag = listTag.getCompound(i);
            UUID trackedEntity = compoundTag.getUUID("TrackedEntity");
            CompoundTag relatedEntities = compoundTag.getCompound("RelatedEntities");
            List<UUID> relatedEntityList = new ArrayList<>();
            for (String key : relatedEntities.getAllKeys())
                relatedEntityList.add(relatedEntities.getUUID(key));
            abilityReversionEntityData.trackedAndRelatedEntities.put(trackedEntity, relatedEntityList);
        }
        ListTag recoverableEntities = tag.getList("RecoverableEntities", 10);
        for (int i = 0; i < recoverableEntities.size(); i++) {
            CompoundTag compoundTag = recoverableEntities.getCompound(i);
            UUID owner = compoundTag.getUUID("UUID");
            ListTag entities = compoundTag.getList("Entities", 10);
            List<CompoundTag> entityList = new ArrayList<>();
            for (int j = 0; j < entities.size(); j++)
                entityList.add(entities.getCompound(j));
            abilityReversionEntityData.recoverableEntities.put(owner, entityList);
        }
        return abilityReversionEntityData;
    }
}
