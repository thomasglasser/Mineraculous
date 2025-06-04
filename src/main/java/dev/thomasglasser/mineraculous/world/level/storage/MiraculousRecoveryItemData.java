package dev.thomasglasser.mineraculous.world.level.storage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

public class MiraculousRecoveryItemData extends SavedData {
    public static final String FILE_ID = "miraculous_recovery_item";
    private final Table<UUID, UUID, ItemStack> recoverableItems = HashBasedTable.create();
    private final Map<UUID, ItemStack> recoveredItems = new HashMap<>();
    private final Map<UUID, ItemStack> kamikotizedItems = new HashMap<>();

    public static MiraculousRecoveryItemData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(MiraculousRecoveryItemData.factory(), MiraculousRecoveryItemData.FILE_ID);
    }

    public static Factory<MiraculousRecoveryItemData> factory() {
        return new Factory<>(MiraculousRecoveryItemData::new, MiraculousRecoveryItemData::load, DataFixTypes.LEVEL);
    }

    public void tick(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof Player player) {
                Inventory inventory = player.getInventory();
                checkRecovered(inventory.items, inventory);
            } else if (livingEntity instanceof InventoryCarrier carrier) {
                SimpleContainer inventory = carrier.getInventory();
                checkRecovered(inventory.getItems(), inventory);
            }
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = livingEntity.getItemBySlot(slot);
                ItemStack recovered = checkRecovered(stack);
                if (recovered != null) {
                    livingEntity.setItemSlot(slot, recovered);
                    stack.setCount(0);
                }
            }
            List<Map.Entry<CuriosData, ItemStack>> curios = new ReferenceArrayList<>(CuriosUtils.getAllItems(livingEntity).entrySet());
            checkRecovered(curios.size(), i -> curios.get(i).getValue(), (i, stack) -> {
                CuriosData curiosData = curios.get(i).getKey();
                CuriosUtils.setStackInSlot(livingEntity, curiosData, stack);
            });
        } else if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            ItemStack recovered = checkRecovered(stack);
            if (recovered != null) {
                itemEntity.setItem(recovered);
                stack.setCount(0);
            }
        }
    }

    private void checkRecovered(int size, Function<Integer, ItemStack> getter, BiConsumer<Integer, ItemStack> setter) {
        for (int i = 0; i < size; i++) {
            ItemStack stack = getter.apply(i);
            ItemStack recovered = checkRecovered(stack);
            if (recovered != null) {
                setter.accept(i, recovered);
                stack.setCount(0);
            }
        }
    }

    private void checkRecovered(NonNullList<ItemStack> items, Container container) {
        checkRecovered(items.size(), container::getItem, container::setItem);
    }

    public ItemStack checkRecovered(ItemStack itemStack) {
        if (itemStack.has(MineraculousDataComponents.RECOVERABLE_ITEM_ID)) {
            UUID id = itemStack.get(MineraculousDataComponents.RECOVERABLE_ITEM_ID);
            if (recoveredItems.containsKey(id)) {
                ItemStack recovered = recoveredItems.get(id).copy();
                recoveredItems.remove(id);
                setDirty();
                return recovered;
            }
        }
        return null;
    }

    public void markRecovered(UUID owner) {
        if (recoverableItems.containsRow(owner)) {
            recoveredItems.putAll(recoverableItems.row(owner));
            recoverableItems.row(owner).clear();
        }
        setDirty();
    }

    public void putRecoverable(UUID owner, UUID item, ItemStack stack) {
        recoverableItems.put(owner, item, stack.copy());
        setDirty();
    }

    public void putRemovable(UUID owner, UUID item) {
        putRecoverable(owner, item, ItemStack.EMPTY);
    }

    public void recoverKamikotized(UUID owner, ServerLevel level) {
        if (kamikotizedItems.containsKey(owner)) {
            Player player = level.getPlayerByUUID(owner);
            if (player != null) {
                player.addItem(kamikotizedItems.get(owner));
            }
            kamikotizedItems.remove(owner);
            setDirty();
        }
    }

    public void putKamikotized(UUID owner, ItemStack stack) {
        kamikotizedItems.put(owner, stack.copy());
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag recoverableItems = new ListTag();
        for (UUID uuid : this.recoverableItems.rowKeySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", uuid);
            ListTag recoverable = new ListTag();
            for (Map.Entry<UUID, ItemStack> entry1 : this.recoverableItems.row(uuid).entrySet()) {
                CompoundTag compoundTag1 = new CompoundTag();
                compoundTag1.putUUID("UUID", entry1.getKey());
                compoundTag1.put("ItemStack", entry1.getValue().saveOptional(registries));
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
            compoundTag.put("ItemStack", entry.getValue().saveOptional(registries));
            recoveredItems.add(compoundTag);
        }
        tag.put("RecoveredItems", recoveredItems);
        ListTag kamikotizedItems = new ListTag();
        for (Map.Entry<UUID, ItemStack> entry : this.kamikotizedItems.entrySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", entry.getKey());
            compoundTag.put("ItemStack", entry.getValue().saveOptional(registries));
            kamikotizedItems.add(compoundTag);
        }
        tag.put("KamikotizedItems", kamikotizedItems);
        return tag;
    }

    public static MiraculousRecoveryItemData load(CompoundTag tag, HolderLookup.Provider registries) {
        MiraculousRecoveryItemData miraculousRecoveryEntityData = new MiraculousRecoveryItemData();
        ListTag recoverableItems = tag.getList("RecoverableItems", 10);
        for (int i = 0; i < recoverableItems.size(); ++i) {
            CompoundTag compoundTag = recoverableItems.getCompound(i);
            UUID owner = compoundTag.getUUID("UUID");
            ListTag recoverable = compoundTag.getList("Items", 10);
            for (int j = 0; j < recoverable.size(); ++j) {
                CompoundTag compoundTag1 = recoverable.getCompound(j);
                UUID item = compoundTag1.getUUID("UUID");
                ItemStack itemStack = ItemStack.parseOptional(registries, compoundTag1.getCompound("ItemStack"));
                miraculousRecoveryEntityData.recoverableItems.put(owner, item, itemStack);
            }
        }
        ListTag recoveredItems = tag.getList("RecoveredItems", 10);
        for (int i = 0; i < recoveredItems.size(); i++) {
            CompoundTag compoundTag = recoveredItems.getCompound(i);
            UUID uuid = compoundTag.getUUID("UUID");
            ItemStack itemStack = ItemStack.parseOptional(registries, compoundTag.getCompound("ItemStack"));
            miraculousRecoveryEntityData.recoveredItems.put(uuid, itemStack);
        }
        ListTag kamikotizedItems = tag.getList("KamikotizedItems", 10);
        for (int i = 0; i < kamikotizedItems.size(); i++) {
            CompoundTag compoundTag = kamikotizedItems.getCompound(i);
            UUID uuid = compoundTag.getUUID("UUID");
            ItemStack itemStack = ItemStack.parseOptional(registries, compoundTag.getCompound("ItemStack"));
            miraculousRecoveryEntityData.kamikotizedItems.put(uuid, itemStack);
        }
        return miraculousRecoveryEntityData;
    }
}
