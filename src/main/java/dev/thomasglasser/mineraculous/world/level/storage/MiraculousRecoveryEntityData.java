package dev.thomasglasser.mineraculous.world.level.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

public class MiraculousRecoveryEntityData extends SavedData {
    public static final String FILE_ID = "miraculous_recovery/entity";
    private final Map<UUID, List<UUID>> trackedAndRelatedEntities = new HashMap<>();

    public static SavedData.Factory<MiraculousRecoveryEntityData> factory() {
        return new SavedData.Factory<>(MiraculousRecoveryEntityData::new, (p_294039_, p_324123_) -> load(p_294039_), DataFixTypes.LEVEL);
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
            trackedAndRelatedEntities.put(trackedEntity, new ArrayList<>());
        List<UUID> related = trackedAndRelatedEntities.get(trackedEntity);
        if (!related.contains(relatedEntity)) {
            related.add(relatedEntity);
            setDirty();
        }
    }

    public void stopTracking(UUID uuid) {
        trackedAndRelatedEntities.remove(uuid);
        setDirty();
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
        return tag;
    }

    public static MiraculousRecoveryEntityData load(CompoundTag tag) {
        MiraculousRecoveryEntityData miraculousRecoveryEntityData = new MiraculousRecoveryEntityData();
        ListTag listTag = tag.getList("TrackedAndRelatedEntities", 10);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compoundTag = listTag.getCompound(i);
            UUID trackedEntity = compoundTag.getUUID("TrackedEntity");
            CompoundTag relatedEntities = compoundTag.getCompound("RelatedEntities");
            List<UUID> relatedEntityList = new ArrayList<>();
            for (String key : relatedEntities.getAllKeys())
                relatedEntityList.add(relatedEntities.getUUID(key));
            miraculousRecoveryEntityData.trackedAndRelatedEntities.put(trackedEntity, relatedEntityList);
        }
        return miraculousRecoveryEntityData;
    }
}
