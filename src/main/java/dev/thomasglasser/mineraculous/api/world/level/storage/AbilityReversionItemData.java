package dev.thomasglasser.mineraculous.api.world.level.storage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosData;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
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

/// Data for reverting trackable item changes
public class AbilityReversionItemData extends SavedData {
    public static final String FILE_ID = "ability_reversion_item";
    private final Table<UUID, UUID, ItemStack> revertableItems = HashBasedTable.create();
    private final Map<UUID, ItemStack> revertedItems = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, ItemStack> kamikotizedItems = new Object2ObjectOpenHashMap<>();

    public static AbilityReversionItemData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(AbilityReversionItemData.factory(), AbilityReversionItemData.FILE_ID);
    }

    public static Factory<AbilityReversionItemData> factory() {
        return new Factory<>(AbilityReversionItemData::new, AbilityReversionItemData::load, DataFixTypes.LEVEL);
    }

    public void tick(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof Player player) {
                Inventory inventory = player.getInventory();
                checkReverted(inventory.items, inventory);
            } else if (livingEntity instanceof InventoryCarrier carrier) {
                SimpleContainer inventory = carrier.getInventory();
                checkReverted(inventory.getItems(), inventory);
            }
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = livingEntity.getItemBySlot(slot);
                ItemStack recovered = checkReverted(stack);
                if (recovered != null) {
                    livingEntity.setItemSlot(slot, recovered);
                    stack.setCount(0);
                }
            }
            List<Map.Entry<CuriosData, ItemStack>> curios = new ReferenceArrayList<>(CuriosUtils.getAllItems(livingEntity).entrySet());
            checkReverted(curios.size(), i -> curios.get(i).getValue(), (i, stack) -> {
                CuriosData curiosData = curios.get(i).getKey();
                CuriosUtils.setStackInSlot(livingEntity, curiosData, stack);
            });
        } else if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            ItemStack recovered = checkReverted(stack);
            if (recovered != null) {
                itemEntity.setItem(recovered);
                stack.setCount(0);
            }
        }
    }

    private void checkReverted(int size, Function<Integer, ItemStack> getter, BiConsumer<Integer, ItemStack> setter) {
        for (int i = 0; i < size; i++) {
            ItemStack stack = getter.apply(i);
            ItemStack recovered = checkReverted(stack);
            if (recovered != null) {
                setter.accept(i, recovered);
                stack.setCount(0);
            }
        }
    }

    private void checkReverted(NonNullList<ItemStack> items, Container container) {
        checkReverted(items.size(), container::getItem, container::setItem);
    }

    public ItemStack checkReverted(ItemStack itemStack) {
        if (itemStack.has(MineraculousDataComponents.RECOVERABLE_ITEM_ID)) {
            UUID id = itemStack.get(MineraculousDataComponents.RECOVERABLE_ITEM_ID);
            if (revertedItems.containsKey(id)) {
                ItemStack recovered = revertedItems.get(id).copy();
                revertedItems.remove(id);
                setDirty();
                return recovered;
            }
        }
        return null;
    }

    public void markReverted(UUID owner) {
        if (revertableItems.containsRow(owner)) {
            revertedItems.putAll(revertableItems.row(owner));
            revertableItems.row(owner).clear();
        }
        setDirty();
    }

    public void putRevertable(UUID owner, UUID item, ItemStack stack) {
        revertableItems.put(owner, item, stack.copy());
        setDirty();
    }

    public void putRemovable(UUID owner, UUID item) {
        putRevertable(owner, item, ItemStack.EMPTY);
    }

    public void revertKamikotized(UUID owner, ServerLevel level) {
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
        ListTag revertableItems = new ListTag();
        for (UUID uuid : this.revertableItems.rowKeySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", uuid);
            ListTag revertable = new ListTag();
            for (Map.Entry<UUID, ItemStack> entry1 : this.revertableItems.row(uuid).entrySet()) {
                CompoundTag compoundTag1 = new CompoundTag();
                compoundTag1.putUUID("UUID", entry1.getKey());
                compoundTag1.put("ItemStack", entry1.getValue().saveOptional(registries));
                revertable.add(compoundTag1);
            }
            compoundTag.put("Items", revertable);
            revertableItems.add(compoundTag);
        }
        tag.put("RevertableItems", revertableItems);
        ListTag revertedItems = new ListTag();
        for (Map.Entry<UUID, ItemStack> entry : this.revertedItems.entrySet()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("UUID", entry.getKey());
            compoundTag.put("ItemStack", entry.getValue().saveOptional(registries));
            revertedItems.add(compoundTag);
        }
        tag.put("RevertedItems", revertedItems);
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

    public static AbilityReversionItemData load(CompoundTag tag, HolderLookup.Provider registries) {
        AbilityReversionItemData miraculousRecoveryEntityData = new AbilityReversionItemData();
        ListTag revertableItems = tag.getList("RevertableItems", 10);
        for (int i = 0; i < revertableItems.size(); ++i) {
            CompoundTag compoundTag = revertableItems.getCompound(i);
            UUID owner = compoundTag.getUUID("UUID");
            ListTag recoverable = compoundTag.getList("Items", 10);
            for (int j = 0; j < recoverable.size(); ++j) {
                CompoundTag compoundTag1 = recoverable.getCompound(j);
                UUID item = compoundTag1.getUUID("UUID");
                ItemStack itemStack = ItemStack.parseOptional(registries, compoundTag1.getCompound("ItemStack"));
                miraculousRecoveryEntityData.revertableItems.put(owner, item, itemStack);
            }
        }
        ListTag revertedItems = tag.getList("RevertedItems", 10);
        for (int i = 0; i < revertedItems.size(); i++) {
            CompoundTag compoundTag = revertedItems.getCompound(i);
            UUID uuid = compoundTag.getUUID("UUID");
            ItemStack itemStack = ItemStack.parseOptional(registries, compoundTag.getCompound("ItemStack"));
            miraculousRecoveryEntityData.revertedItems.put(uuid, itemStack);
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
