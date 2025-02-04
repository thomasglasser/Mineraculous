package dev.thomasglasser.mineraculous.world.level.storage;

import java.util.HashMap;
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

public class MiraculousRecoveryBlockData extends SavedData {
    public static final String FILE_ID = "miraculous_recovery_block";
    private final Map<UUID, Map<BlockPos, BlockState>> recoverableBlocks = new HashMap<>();

    public static Factory<MiraculousRecoveryBlockData> factory() {
        return new Factory<>(MiraculousRecoveryBlockData::new, MiraculousRecoveryBlockData::load, DataFixTypes.LEVEL);
    }

    public void recover(UUID owner, ServerLevel level) {
        if (recoverableBlocks.containsKey(owner)) {
            for (Map.Entry<BlockPos, BlockState> entry : recoverableBlocks.get(owner).entrySet()) {
                BlockPos pos = entry.getKey();
                BlockState state = entry.getValue();
                level.setBlock(pos, state, Block.UPDATE_ALL);
            }
            recoverableBlocks.remove(owner);
            setDirty();
        }
    }

    public void putRecoverable(UUID owner, Map<BlockPos, BlockState> recoverable) {
        recoverableBlocks.computeIfAbsent(owner, p -> new HashMap<>()).putAll(recoverable);
        setDirty();
    }

    public UUID getRecoverer(BlockPos pos) {
        for (Map.Entry<UUID, Map<BlockPos, BlockState>> entry : recoverableBlocks.entrySet()) {
            if (entry.getValue().containsKey(pos))
                return entry.getKey();
        }
        return null;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag recoverable = new ListTag();
        for (Map.Entry<UUID, Map<BlockPos, BlockState>> entry : this.recoverableBlocks.entrySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", entry.getKey());
            ListTag recoverableBlocks = new ListTag();
            for (Map.Entry<BlockPos, BlockState> entry1 : entry.getValue().entrySet()) {
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

    public static MiraculousRecoveryBlockData load(CompoundTag tag, HolderLookup.Provider registries) {
        MiraculousRecoveryBlockData miraculousRecoveryEntityData = new MiraculousRecoveryBlockData();
        ListTag recoverable = tag.getList("RecoverableBlocks", 10);
        for (int i = 0; i < recoverable.size(); i++) {
            CompoundTag compoundTag = recoverable.getCompound(i);
            UUID owner = compoundTag.getUUID("UUID");
            ListTag recoverableBlocks = compoundTag.getList("Blocks", 10);
            Map<BlockPos, BlockState> recoverableMap = new HashMap<>();
            for (int j = 0; j < recoverableBlocks.size(); j++) {
                CompoundTag compoundTag1 = recoverableBlocks.getCompound(j);
                BlockPos blockPos = BlockPos.of(compoundTag1.getLong("BlockPos"));
                BlockState blockState = BlockState.CODEC.parse(NbtOps.INSTANCE, compoundTag1.get("BlockState")).result().orElseThrow();
                recoverableMap.put(blockPos, blockState);
            }
            miraculousRecoveryEntityData.recoverableBlocks.put(owner, recoverableMap);
        }
        return miraculousRecoveryEntityData;
    }
}
