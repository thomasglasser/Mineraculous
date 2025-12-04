package dev.thomasglasser.mineraculous.api.world.level.storage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import dev.thomasglasser.mineraculous.api.nbt.MineraculousNbtUtils;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

/// Data for reverting trackable block changes
public class BlockReversionData extends SavedData {
    public static final String FILE_ID = "block_reversion";
    private final Table<UUID, BlockLocation, BlockState> revertibleBlocks = HashBasedTable.create();

    public static BlockReversionData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(BlockReversionData.factory(), BlockReversionData.FILE_ID);
    }

    public static Factory<BlockReversionData> factory() {
        return new Factory<>(BlockReversionData::new, BlockReversionData::load, DataFixTypes.LEVEL);
    }

    public Multimap<ResourceKey<Level>, BlockPos> getReversionPositions(UUID uuid) {
        Multimap<ResourceKey<Level>, BlockPos> positions = HashMultimap.create();
        for (BlockLocation loc : revertibleBlocks.row(uuid).keySet()) {
            positions.put(loc.dimension(), loc.pos());
        }
        return positions;
    }

    public void revert(UUID owner, ServerLevel level, BlockPos pos) {
        BlockLocation loc = new BlockLocation(level.dimension(), pos);
        BlockState state = revertibleBlocks.remove(owner, loc);
        if (state != null) {
            level.setBlockAndUpdate(pos, state);
        }
        setDirty();
    }

    public void putRevertible(UUID owner, ResourceKey<Level> dimension, BlockPos pos, BlockState state) {
        BlockLocation location = new BlockLocation(dimension, pos);
        if (!revertibleBlocks.contains(owner, location)) {
            revertibleBlocks.put(owner, location, state);
            setDirty();
        }
    }

    public @Nullable UUID getCause(ResourceKey<Level> dimension, BlockPos pos) {
        BlockLocation location = new BlockLocation(dimension, pos);
        for (Table.Cell<UUID, BlockLocation, BlockState> cell : revertibleBlocks.cellSet()) {
            if (cell.getColumnKey().equals(location))
                return cell.getRowKey();
        }
        return null;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        tag.put("Revertible", MineraculousNbtUtils.writeStringRowKeyedTable(revertibleBlocks, UUID::toString, MineraculousNbtUtils.codecEncoder(BlockLocation.CODEC, ops), MineraculousNbtUtils.codecEncoder(BlockState.CODEC, ops)));
        return tag;
    }

    public static BlockReversionData load(CompoundTag tag, HolderLookup.Provider registries) {
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        BlockReversionData data = new BlockReversionData();
        data.revertibleBlocks.putAll(MineraculousNbtUtils.readStringRowKeyedTable(HashBasedTable::create, tag.getCompound("Revertible"), UUID::fromString, MineraculousNbtUtils.codecDecoder(BlockLocation.CODEC, ops), MineraculousNbtUtils.codecDecoder(BlockState.CODEC, ops)));
        return data;
    }
}
