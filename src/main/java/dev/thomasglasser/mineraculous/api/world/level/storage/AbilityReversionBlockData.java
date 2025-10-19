package dev.thomasglasser.mineraculous.api.world.level.storage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Table;
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
    private final Table<UUID, Pair<ResourceKey<Level>, BlockPos>, BlockState> revertibleBlocks = HashBasedTable.create();

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
        revertibleBlocks.put(owner, Pair.of(dimension, pos), state);
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
        ListTag revertible = new ListTag();
        for (UUID uuid : this.revertibleBlocks.rowKeySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", uuid);
            ListTag recoverableBlocks = new ListTag();
            for (Map.Entry<Pair<ResourceKey<Level>, BlockPos>, BlockState> entry1 : this.revertibleBlocks.row(uuid).entrySet()) {
                CompoundTag compoundTag1 = new CompoundTag();
                compoundTag1.put("Dimension", Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, entry1.getKey().left()).getOrThrow());
                compoundTag1.putLong("BlockPos", entry1.getKey().right().asLong());
                compoundTag1.put("BlockState", BlockState.CODEC.encodeStart(NbtOps.INSTANCE, entry1.getValue()).getOrThrow());
                recoverableBlocks.add(compoundTag1);
            }
            compoundTag.put("Blocks", recoverableBlocks);
            revertible.add(compoundTag);
        }
        tag.put("RevertibleBlocks", revertible);
        return tag;
    }

    public static AbilityReversionBlockData load(CompoundTag tag, HolderLookup.Provider registries) {
        AbilityReversionBlockData miraculousRecoveryEntityData = new AbilityReversionBlockData();
        ListTag revertible = tag.getList("RevertibleBlocks", ListTag.TAG_COMPOUND);
        for (int i = 0; i < revertible.size(); i++) {
            CompoundTag compoundTag = revertible.getCompound(i);
            UUID owner = compoundTag.getUUID("UUID");
            ListTag recoverableBlocks = compoundTag.getList("Blocks", ListTag.TAG_COMPOUND);
            for (int j = 0; j < recoverableBlocks.size(); j++) {
                CompoundTag compoundTag1 = recoverableBlocks.getCompound(j);
                ResourceKey<Level> dimension = Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, compoundTag1.get("Dimension")).getOrThrow();
                BlockPos blockPos = BlockPos.of(compoundTag1.getLong("BlockPos"));
                BlockState blockState = BlockState.CODEC.parse(NbtOps.INSTANCE, compoundTag1.get("BlockState")).getOrThrow();
                miraculousRecoveryEntityData.revertibleBlocks.put(owner, Pair.of(dimension, blockPos), blockState);
            }
        }
        return miraculousRecoveryEntityData;
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
