package dev.thomasglasser.mineraculous.api.world.level.storage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

/// Data for reverting trackable block changes
public class AbilityReversionBlockData extends SavedData {
    public static final String FILE_ID = "ability_reversion_block";
    private final Table<UUID, BlockLocation, BlockState> revertibleBlocks = HashBasedTable.create();

    public static AbilityReversionBlockData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(AbilityReversionBlockData.factory(), AbilityReversionBlockData.FILE_ID);
    }

    public static Factory<AbilityReversionBlockData> factory() {
        return new Factory<>(AbilityReversionBlockData::new, AbilityReversionBlockData::load, DataFixTypes.LEVEL);
    }

    public void revert(UUID owner, ServerLevel level) {
        for (Map.Entry<BlockLocation, BlockState> entry : revertibleBlocks.row(owner).entrySet()) {
            ServerLevel targetLevel = level.getServer().getLevel(entry.getKey().dimension());
            if (targetLevel != null) {
                targetLevel.setBlock(entry.getKey().pos(), entry.getValue(), Block.UPDATE_ALL);
            }
        }
        revertibleBlocks.row(owner).clear();
        setDirty();
    }

    public void putRevertible(UUID owner, ResourceKey<Level> dimension, BlockPos pos, BlockState state) {
        BlockLocation location = new BlockLocation(dimension, pos);
        if (!revertibleBlocks.contains(owner, location)) {
            revertibleBlocks.put(owner, location, state);
            setDirty();
        }
    }

    public UUID getCause(ResourceKey<Level> dimension, BlockPos pos) {
        for (Table.Cell<UUID, BlockLocation, BlockState> cell : revertibleBlocks.cellSet()) {
            if (cell.getColumnKey().equals(new BlockLocation(dimension, pos)))
                return cell.getRowKey();
        }
        return null;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag revertible = new CompoundTag();
        for (UUID ownerId : revertibleBlocks.rowKeySet()) {
            ListTag entries = new ListTag();
            for (Map.Entry<BlockLocation, BlockState> entry : revertibleBlocks.row(ownerId).entrySet()) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.put("Location", BlockLocation.CODEC.encodeStart(NbtOps.INSTANCE, entry.getKey()).getOrThrow());
                entryTag.put("State", BlockState.CODEC.encodeStart(NbtOps.INSTANCE, entry.getValue()).getOrThrow());
                entries.add(entryTag);
            }
            revertible.put(ownerId.toString(), entries);
        }
        tag.put("Revertible", revertible);
        return tag;
    }

    public static AbilityReversionBlockData load(CompoundTag tag, HolderLookup.Provider registries) {
        AbilityReversionBlockData data = new AbilityReversionBlockData();
        CompoundTag revertible = tag.getCompound("Revertible");
        for (String ownerString : revertible.getAllKeys()) {
            UUID ownerId = UUID.fromString(ownerString);
            ListTag entries = revertible.getList(ownerString, ListTag.TAG_COMPOUND);
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag entryTag = entries.getCompound(i);
                BlockLocation location = BlockLocation.CODEC.parse(NbtOps.INSTANCE, entryTag.get("Location")).getOrThrow();
                BlockState state = BlockState.CODEC.parse(NbtOps.INSTANCE, entryTag.get("State")).getOrThrow();
                data.revertibleBlocks.put(ownerId, location, state);
            }
        }
        return data;
    }
}
