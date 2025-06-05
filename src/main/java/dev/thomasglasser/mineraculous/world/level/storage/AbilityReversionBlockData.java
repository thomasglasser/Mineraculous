package dev.thomasglasser.mineraculous.world.level.storage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

public class AbilityReversionBlockData extends SavedData {
    public static final String FILE_ID = "ability_reversion_block";
    private final Table<UUID, BlockPos, BlockState> recoverableBlocks = HashBasedTable.create();

    public static AbilityReversionBlockData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(AbilityReversionBlockData.factory(), AbilityReversionBlockData.FILE_ID);
    }

    public static Factory<AbilityReversionBlockData> factory() {
        return new Factory<>(AbilityReversionBlockData::new, AbilityReversionBlockData::load, DataFixTypes.LEVEL);
    }

    public void recover(UUID owner, ServerLevel level) {
        for (Map.Entry<BlockPos, BlockState> entry : recoverableBlocks.row(owner).entrySet()) {
            level.setBlock(entry.getKey(), entry.getValue(), Block.UPDATE_ALL);
        }
        recoverableBlocks.row(owner).clear();
        setDirty();
    }

    public void putRecoverable(UUID owner, Map<BlockPos, BlockState> recoverable) {
        recoverableBlocks.row(owner).putAll(recoverable);
        setDirty();
    }

    public UUID getRecoverer(BlockPos pos) {
        for (Table.Cell<UUID, BlockPos, BlockState> cell : recoverableBlocks.cellSet()) {
            if (cell.getColumnKey().equals(pos))
                return cell.getRowKey();
        }
        return null;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag recoverable = new ListTag();
        for (UUID uuid : this.recoverableBlocks.rowKeySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", uuid);
            ListTag recoverableBlocks = new ListTag();
            for (Map.Entry<BlockPos, BlockState> entry1 : this.recoverableBlocks.row(uuid).entrySet()) {
                CompoundTag compoundTag1 = new CompoundTag();
                compoundTag1.putLong("BlockPos", entry1.getKey().asLong());
                compoundTag1.put("BlockState", BlockState.CODEC.encodeStart(NbtOps.INSTANCE, entry1.getValue()).result().orElseThrow());
                recoverableBlocks.add(compoundTag1);
            }
            compoundTag.put("Blocks", recoverableBlocks);
            recoverable.add(compoundTag);
        }
        tag.put("RecoverableBlocks", recoverable);
        return tag;
    }

    public static AbilityReversionBlockData load(CompoundTag tag, HolderLookup.Provider registries) {
        AbilityReversionBlockData miraculousRecoveryEntityData = new AbilityReversionBlockData();
        ListTag recoverable = tag.getList("RecoverableBlocks", 10);
        for (int i = 0; i < recoverable.size(); i++) {
            CompoundTag compoundTag = recoverable.getCompound(i);
            UUID owner = compoundTag.getUUID("UUID");
            ListTag recoverableBlocks = compoundTag.getList("Blocks", 10);
            for (int j = 0; j < recoverableBlocks.size(); j++) {
                CompoundTag compoundTag1 = recoverableBlocks.getCompound(j);
                BlockPos blockPos = BlockPos.of(compoundTag1.getLong("BlockPos"));
                BlockState blockState = BlockState.CODEC.parse(NbtOps.INSTANCE, compoundTag1.get("BlockState")).result().orElseThrow();
                miraculousRecoveryEntityData.recoverableBlocks.put(owner, blockPos, blockState);
            }
        }
        return miraculousRecoveryEntityData;
    }
}
