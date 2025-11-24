package dev.thomasglasser.mineraculous.api.world.level.storage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosData;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
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
import org.jetbrains.annotations.Nullable;

/// Data for reverting trackable item changes
public class AbilityReversionItemData extends SavedData {
    public static final String FILE_ID = "ability_reversion_item";
    private final Table<UUID, UUID, ItemStack> revertibleItems = HashBasedTable.create();
    private final Map<UUID, ItemStack> revertMarkedItems = new Object2ObjectOpenHashMap<>();
    private final Set<UUID> revertedItems = new ObjectOpenHashSet<>();
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

    public ItemStack checkReverted(ItemStack stack) {
        if (stack.has(MineraculousDataComponents.REVERTIBLE_ITEM_ID)) {
            UUID id = stack.get(MineraculousDataComponents.REVERTIBLE_ITEM_ID);
            if (revertMarkedItems.containsKey(id) || revertedItems.contains(id)) {
                ItemStack recovered = revertMarkedItems.remove(id);
                if (recovered == null) {
                    recovered = ItemStack.EMPTY;
                } else {
                    revertedItems.add(id);
                    recovered = recovered.copy();
                }
                setDirty();
                return recovered;
            }
        }
        return null;
    }

    public void markReverted(UUID owner) {
        if (revertibleItems.containsRow(owner)) {
            revertMarkedItems.putAll(revertibleItems.row(owner));
            revertibleItems.row(owner).clear();
            setDirty();
        }
    }

    public void putRevertible(UUID owner, UUID item, ItemStack stack) {
        revertibleItems.put(owner, item, stack.copy());
        setDirty();
    }

    public void putRemovable(UUID owner, UUID item) {
        putRevertible(owner, item, ItemStack.EMPTY);
    }

    public void revertKamikotized(LivingEntity owner, UUID item, @Nullable ItemStack kamikotizedStack) {
        ItemStack original = kamikotizedItems.remove(item);
        if (original != null) {
            ItemStack reverted;
            if (kamikotizedStack != null) {
                reverted = original.copyWithCount(Math.max(1, kamikotizedStack.getCount()));
                reverted.copyFrom(kamikotizedStack, DataComponents.DAMAGE, DataComponents.MAX_DAMAGE);

                UUID revertibleId = UUID.randomUUID();
                reverted.set(MineraculousDataComponents.REVERTIBLE_ITEM_ID, revertibleId);
                putRevertible(owner.getUUID(), revertibleId, original);
            } else {
                reverted = original.copy();
            }
            revertedItems.add(item);
            setDirty();
            EntityUtils.addToInventoryOrDrop(owner, reverted);
        }
    }

    public void putKamikotized(UUID item, ItemStack stack) {
        kamikotizedItems.put(item, stack.copy());
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag revertible = new CompoundTag();
        for (UUID ownerId : this.revertibleItems.rowKeySet()) {
            CompoundTag entries = new CompoundTag();
            for (Map.Entry<UUID, ItemStack> entry : this.revertibleItems.row(ownerId).entrySet()) {
                entries.put(entry.getKey().toString(), ItemStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, entry.getValue()).getOrThrow());
            }
            revertible.put(ownerId.toString(), entries);
        }
        tag.put("Revertible", revertible);
        CompoundTag revertMarked = new CompoundTag();
        for (Map.Entry<UUID, ItemStack> entry : this.revertMarkedItems.entrySet()) {
            revertMarked.put(entry.getKey().toString(), ItemStack.OPTIONAL_CODEC.encodeStart(NbtOps.INSTANCE, entry.getValue()).getOrThrow());
        }
        tag.put("RevertMarked", revertMarked);
        ListTag reverted = new ListTag();
        for (UUID itemId : this.revertedItems) {
            reverted.add(NbtUtils.createUUID(itemId));
        }
        tag.put("Reverted", reverted);
        CompoundTag kamikotized = new CompoundTag();
        for (Map.Entry<UUID, ItemStack> entry : this.kamikotizedItems.entrySet()) {
            kamikotized.put(entry.getKey().toString(), ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, entry.getValue()).getOrThrow());
        }
        tag.put("Kamikotized", kamikotized);
        return tag;
    }

    public static AbilityReversionItemData load(CompoundTag tag, HolderLookup.Provider registries) {
        AbilityReversionItemData data = new AbilityReversionItemData();
        CompoundTag revertible = tag.getCompound("Revertible");
        for (String ownerString : revertible.getAllKeys()) {
            UUID ownerId = UUID.fromString(ownerString);
            CompoundTag entries = revertible.getCompound(ownerString);
            for (String itemString : entries.getAllKeys()) {
                UUID itemId = UUID.fromString(itemString);
                data.revertibleItems.put(ownerId, itemId, ItemStack.OPTIONAL_CODEC.parse(NbtOps.INSTANCE, entries.get(itemString)).getOrThrow());
            }
        }
        CompoundTag revertMarked = tag.getCompound("RevertMarked");
        for (String itemString : revertMarked.getAllKeys()) {
            UUID itemId = UUID.fromString(itemString);
            data.revertMarkedItems.put(itemId, ItemStack.OPTIONAL_CODEC.parse(NbtOps.INSTANCE, revertMarked.get(itemString)).getOrThrow());
        }
        ListTag reverted = tag.getList("Reverted", ListTag.TAG_INT_ARRAY);
        for (Tag revertedId : reverted) {
            data.revertedItems.add(NbtUtils.loadUUID(revertedId));
        }
        CompoundTag kamikotized = tag.getCompound("Kamikotized");
        for (String itemString : kamikotized.getAllKeys()) {
            UUID itemId = UUID.fromString(itemString);
            data.kamikotizedItems.put(itemId, ItemStack.CODEC.parse(NbtOps.INSTANCE, kamikotized.get(itemString)).getOrThrow());
        }
        return data;
    }
}
