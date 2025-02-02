package dev.thomasglasser.mineraculous.world.level.storage;

import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

public class MiraculousRecoveryItemData extends SavedData {
    public static final String FILE_ID = "miraculous_recovery/item";
    private final Map<UUID, Map<UUID, ItemStack>> recoverableItems = new HashMap<>();
    private final Map<UUID, ItemStack> recoveredItems = new HashMap<>();

    public static Factory<MiraculousRecoveryItemData> factory() {
        return new Factory<>(MiraculousRecoveryItemData::new, MiraculousRecoveryItemData::load, DataFixTypes.LEVEL);
    }

    public void checkRecovered(Inventory inventory, ItemStack itemStack) {
        if (itemStack.has(MineraculousDataComponents.RECOVERABLE_ITEM_ID)) {
            UUID id = itemStack.get(MineraculousDataComponents.RECOVERABLE_ITEM_ID);
            if (recoveredItems.containsKey(id)) {
                ItemStack recovered = recoveredItems.get(id);
                if (inventory.contains(itemStack))
                    inventory.setItem(inventory.findSlotMatchingItem(itemStack), recovered);
                else
                    inventory.add(recovered);
                itemStack.setCount(0);
                recoveredItems.remove(id);
            }
        }
    }

    public void markRecovered(UUID owner) {
        if (recoverableItems.containsKey(owner))
            recoveredItems.putAll(recoverableItems.get(owner));
    }

    public void putRecoverable(UUID owner, UUID item, ItemStack stack) {
        recoverableItems.computeIfAbsent(owner, p -> new HashMap<>()).put(item, stack.copy());
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag recoverableItems = new ListTag();
        for (Map.Entry<UUID, Map<UUID, ItemStack>> entry : this.recoverableItems.entrySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", entry.getKey());
            ListTag recoverable = new ListTag();
            for (Map.Entry<UUID, ItemStack> entry1 : entry.getValue().entrySet()) {
                CompoundTag compoundTag1 = new CompoundTag();
                compoundTag1.putUUID("UUID", entry1.getKey());
                compoundTag1.put("ItemStack", entry1.getValue().save(registries));
                recoverable.add(compoundTag1);
            }
            compoundTag.put("Items", recoverable);
            recoverableItems.add(compoundTag);
        }
        tag.put("RecoverableItems", recoverableItems);
        ListTag recoveredItems = new ListTag();
        for (Map.Entry<UUID, ItemStack> entry : this.recoveredItems.entrySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", entry.getKey());
            compoundTag.put("ItemStack", entry.getValue().save(registries));
            recoveredItems.add(compoundTag);
        }
        tag.put("RecoveredItems", recoveredItems);
        return tag;
    }

    public static MiraculousRecoveryItemData load(CompoundTag tag, HolderLookup.Provider registries) {
        MiraculousRecoveryItemData miraculousRecoveryEntityData = new MiraculousRecoveryItemData();
        ListTag recoverableItems = tag.getList("RecoverableItems", 10);
        for (int i = 0; i < recoverableItems.size(); i++) {
            CompoundTag compoundTag = recoverableItems.getCompound(i);
            UUID owner = compoundTag.getUUID("UUID");
            ListTag recoverable = compoundTag.getList("Items", 10);
            Map<UUID, ItemStack> recoverableMap = new HashMap<>();
            for (int j = 0; j < recoverable.size(); j++) {
                CompoundTag compoundTag1 = recoverable.getCompound(j);
                UUID item = compoundTag1.getUUID("UUID");
                ItemStack itemStack = ItemStack.parseOptional(registries, compoundTag1.getCompound("ItemStack"));
                recoverableMap.put(item, itemStack);
            }
            miraculousRecoveryEntityData.recoverableItems.put(owner, recoverableMap);
        }
        ListTag recoveredItems = tag.getList("RecoveredItems", 10);
        for (int i = 0; i < recoveredItems.size(); i++) {
            CompoundTag compoundTag = recoveredItems.getCompound(i);
            UUID uuid = compoundTag.getUUID("UUID");
            ItemStack itemStack = ItemStack.parseOptional(registries, compoundTag.getCompound("ItemStack"));
            miraculousRecoveryEntityData.recoveredItems.put(uuid, itemStack);
        }
        return miraculousRecoveryEntityData;
    }
}
