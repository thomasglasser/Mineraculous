package dev.thomasglasser.mineraculous.world.level.storage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import net.minecraft.world.level.saveddata.SavedData;

public class AffectedChunksData extends SavedData {
    public static final String FILE_ID = "affected_chunks";
    private final Table<UUID, ChunkPos, CompoundTag> affectedChunksTable = HashBasedTable.create();

    public static SavedData.Factory<AffectedChunksData> factory() {
        return new SavedData.Factory<>(AffectedChunksData::new, (p_294039_, p_324123_) -> load(p_294039_), DataFixTypes.LEVEL);
    }

    public boolean isBeingTracked(UUID uuid) {
        return affectedChunksTable.containsRow(uuid);
    }

    public void startTracking(UUID uuid, ServerLevel level, ChunkPos chunkPos, BlockPos blockPos) {
        if (!isBeingTracked(uuid))
            putAffectedChunk(uuid, chunkPos, ChunkSerializer.write(level, level.getChunk(blockPos)));
    }

    public void stopTracking(UUID uuid) {
        affectedChunksTable.row(uuid).clear();
    }

    public Map<ChunkPos, CompoundTag> getAffectedChunks(UUID uuid) {
        return affectedChunksTable.row(uuid);
    }

    public void putAffectedChunk(UUID uuid, ChunkPos chunkPos, CompoundTag compoundTag) {
        affectedChunksTable.put(uuid, chunkPos, compoundTag);
    }

    public void putAffectedChunkIfAbsent(UUID uuid, ChunkPos chunkPos, CompoundTag compoundTag) {
        if (!affectedChunksTable.contains(uuid, chunkPos))
            putAffectedChunk(uuid, chunkPos, compoundTag);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        for (Table.Cell<UUID, ChunkPos, CompoundTag> cell : affectedChunksTable.cellSet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("Uuid", cell.getRowKey());
            compoundTag.putInt("ChunkX", cell.getColumnKey().x);
            compoundTag.putInt("ChunkZ", cell.getColumnKey().z);
            compoundTag.put("ChunkData", cell.getValue());
            listTag.add(compoundTag);
        }
        tag.put("AffectedChunks", listTag);
        return tag;
    }

    public static AffectedChunksData load(CompoundTag tag) {
        AffectedChunksData affectedChunksData = new AffectedChunksData();
        ListTag listTag = tag.getList("AffectedChunks", 10);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compoundTag = listTag.getCompound(i);
            UUID uuid = compoundTag.getUUID("Uuid");
            ChunkPos chunkPos = new ChunkPos(compoundTag.getInt("ChunkX"), compoundTag.getInt("ChunkZ"));
            CompoundTag chunkData = compoundTag.getCompound("ChunkData");
            affectedChunksData.putAffectedChunk(uuid, chunkPos, chunkData);
        }
        return affectedChunksData;
    }
}
