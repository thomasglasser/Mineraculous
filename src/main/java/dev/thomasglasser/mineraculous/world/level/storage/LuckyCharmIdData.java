package dev.thomasglasser.mineraculous.world.level.storage;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class LuckyCharmIdData extends SavedData {
    public static final String FILE_ID = "lucky_charm_id";
    private final Object2IntMap<UUID> luckyCharmIdMap = new Object2IntOpenHashMap<>();

    public static LuckyCharmIdData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(LuckyCharmIdData.factory(), LuckyCharmIdData.FILE_ID);
    }

    public static Factory<LuckyCharmIdData> factory() {
        return new Factory<>(LuckyCharmIdData::new, (p_294039_, p_324123_) -> load(p_294039_), DataFixTypes.LEVEL);
    }

    public int getLuckyCharmId(UUID uuid) {
        return luckyCharmIdMap.computeIfAbsent(uuid, newUuid -> 0);
    }

    public int incrementLuckyCharmId(UUID uuid) {
        Integer id = luckyCharmIdMap.compute(uuid, (oldUUID, oldId) -> oldId == null ? 0 : oldId + 1);
        setDirty();
        return id;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        luckyCharmIdMap.forEach((uuid, id) -> {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("Uuid", uuid);
            compoundTag.putInt("LuckyCharmId", id);
            listTag.add(compoundTag);
        });
        tag.put("LuckyCharmIds", listTag);
        return tag;
    }

    public static LuckyCharmIdData load(CompoundTag tag) {
        LuckyCharmIdData luckyCharmIdData = new LuckyCharmIdData();
        ListTag listTag = tag.getList("LuckyCharmIds", 10);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compoundTag = listTag.getCompound(i);
            UUID uuid = compoundTag.getUUID("Uuid");
            int luckyCharmId = compoundTag.getInt("LuckyCharmId");
            luckyCharmIdData.luckyCharmIdMap.put(uuid, luckyCharmId);
        }
        return luckyCharmIdData;
    }
}
