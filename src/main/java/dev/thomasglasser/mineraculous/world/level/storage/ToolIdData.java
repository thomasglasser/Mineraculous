package dev.thomasglasser.mineraculous.world.level.storage;

import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class ToolIdData extends SavedData {
    public static final String FILE_ID = "tool_id";
    private final Object2IntMap<UUID> toolIdMap = new Object2IntOpenHashMap<>();

    public static Factory<ToolIdData> factory() {
        return new Factory<>(ToolIdData::new, (p_294039_, p_324123_) -> load(p_294039_), DataFixTypes.LEVEL);
    }

    public int getToolId(UUID uuid) {
        return toolIdMap.computeIfAbsent(uuid, newUuid -> 0);
    }

    public int getToolId(KwamiData kwamiData) {
        return kwamiData == null ? -1 : getToolId(kwamiData.uuid());
    }

    public int incrementToolId(KwamiData kwamiData) {
        Integer id = toolIdMap.compute(kwamiData.uuid(), (uuid, oldId) -> oldId == null ? 0 : oldId + 1);
        setDirty();
        return id;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        toolIdMap.forEach((uuid, id) -> {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("Uuid", uuid);
            compoundTag.putInt("ToolId", id);
            listTag.add(compoundTag);
        });
        tag.put("ToolIds", listTag);
        return tag;
    }

    public static ToolIdData load(CompoundTag tag) {
        ToolIdData toolIdData = new ToolIdData();
        ListTag listTag = tag.getList("ToolIds", 10);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compoundTag = listTag.getCompound(i);
            UUID uuid = compoundTag.getUUID("Uuid");
            int toolId = compoundTag.getInt("ToolId");
            toolIdData.toolIdMap.put(uuid, toolId);
        }
        return toolIdData;
    }
}
