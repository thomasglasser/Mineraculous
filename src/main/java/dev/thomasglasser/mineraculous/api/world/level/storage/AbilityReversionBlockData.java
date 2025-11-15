package dev.thomasglasser.mineraculous.api.world.level.storage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.Pair;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

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

    public Multimap<ResourceKey<Level>, BlockPos> getReversionPositions(UUID uuid) {
        Multimap<ResourceKey<Level>, BlockPos> positions = MultimapBuilder.hashKeys().arrayListValues().build();
        for (Pair<ResourceKey<Level>, BlockPos> pos : revertibleBlocks.row(uuid).keySet()) {
            positions.put(pos.left(), pos.right());
        }
        return positions;
    }

    public void revert(UUID owner, ServerLevel level, BlockPos pos) {
        Pair<ResourceKey<Level>, BlockPos> loc = Pair.of(level.dimension(), pos);
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
        for (Table.Cell<UUID, Pair<ResourceKey<Level>, BlockPos>, BlockState> cell : revertibleBlocks.cellSet()) {
            if (cell.getColumnKey().equals(Pair.of(dimension, pos)))
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

    private record BlockLocation(ResourceKey<Level> dimension, BlockPos pos) {
        private static final Codec<BlockLocation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(BlockLocation::dimension),
                BlockPos.CODEC.fieldOf("pos").forGetter(BlockLocation::pos)).apply(instance, BlockLocation::new));
    }

    public ArrayList<BlockPos> getRevertibleBlocks(UUID owner) {
        Set<Pair<ResourceKey<Level>, BlockPos>> keys = this.revertibleBlocks.row(owner).keySet();
        ArrayList<BlockPos> positions = new ArrayList<>(keys.size());
        for (Pair<ResourceKey<Level>, BlockPos> pair : keys) {
            positions.add(pair.second());
        }
        return positions;
    }
}
