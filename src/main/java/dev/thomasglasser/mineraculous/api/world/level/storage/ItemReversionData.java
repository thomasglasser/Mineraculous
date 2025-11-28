package dev.thomasglasser.mineraculous.api.world.level.storage;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.particles.MineraculousParticleTypes;
import dev.thomasglasser.mineraculous.api.nbt.MineraculousNbtUtils;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosData;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.impl.util.MineraculousMathUtils;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/// Data for reverting trackable item changes
public class ItemReversionData extends SavedData {
    public static final String FILE_ID = "item_reversion";
    private final Table<UUID, UUID, ItemStack> revertibleItems = HashBasedTable.create();
    private final Map<UUID, ItemStack> revertMarkedItems = new Object2ObjectOpenHashMap<>();
    private final Set<UUID> revertedItems = new ObjectOpenHashSet<>();
    private final Map<UUID, ItemStack> kamikotizedItems = new Object2ObjectOpenHashMap<>();

    public static ItemReversionData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(ItemReversionData.factory(), ItemReversionData.FILE_ID);
    }

    public static Factory<ItemReversionData> factory() {
        return new Factory<>(ItemReversionData::new, ItemReversionData::load, DataFixTypes.LEVEL);
    }

    public void tick(Entity entity) {
        if (entity.tickCount % 10 == 0) {
            if (checkReverted(entity)) {
                Level level = entity.level();
                if (level instanceof ServerLevel serverLevel) {
                    List<Vec3> spiral = MineraculousMathUtils.spinAround(
                            entity.position(),
                            entity.getBbWidth(),
                            entity.getBbWidth(),
                            entity.getBbHeight(),
                            Math.PI / 2d,
                            entity.getBbHeight() / 16d);
                    for (Vec3 pos : spiral) {
                        double x = pos.x;
                        double y = pos.y;
                        double z = pos.z;
                        serverLevel.sendParticles(
                                MineraculousParticleTypes.REVERTING_LADYBUG.get(),
                                x,
                                y,
                                z,
                                5, 0, 0, 0, 0.1);
                    }
                }
            }
        }
    }

    public boolean checkReverted(Entity entity) {
        boolean reverted = false;
        if (entity instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof Player player) {
                Inventory inventory = player.getInventory();
                if (checkReverted(inventory.items, inventory))
                    reverted = true;
            } else if (livingEntity instanceof InventoryCarrier carrier) {
                SimpleContainer inventory = carrier.getInventory();
                if (checkReverted(inventory.getItems(), inventory))
                    reverted = true;
            }
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack stack = livingEntity.getItemBySlot(slot);
                ItemStack recovered = checkReverted(stack);
                if (recovered != null) {
                    livingEntity.setItemSlot(slot, recovered);
                    reverted = true;
                }
            }
            List<Map.Entry<CuriosData, ItemStack>> curios = new ReferenceArrayList<>(CuriosUtils.getAllItems(livingEntity).entrySet());
            if (checkReverted(curios.size(), i -> curios.get(i).getValue(), (i, stack) -> {
                CuriosData curiosData = curios.get(i).getKey();
                CuriosUtils.setStackInSlot(livingEntity, curiosData, stack);
            }))
                reverted = true;
        } else if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            ItemStack recovered = checkReverted(stack);
            if (recovered != null) {
                itemEntity.setItem(recovered);
                reverted = true;
            }
        }
        return reverted;
    }

    private boolean checkReverted(int size, Function<Integer, ItemStack> getter, BiConsumer<Integer, ItemStack> setter) {
        boolean reverted = false;
        for (int i = 0; i < size; i++) {
            ItemStack stack = getter.apply(i);
            ItemStack recovered = checkReverted(stack);
            if (recovered != null) {
                setter.accept(i, recovered);
                reverted = true;
            }
        }
        return reverted;
    }

    private boolean checkReverted(NonNullList<ItemStack> items, Container container) {
        return checkReverted(items.size(), container::getItem, container::setItem);
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
        Function<ItemStack, Tag> itemEncoder = MineraculousNbtUtils.codecEncoder(ItemStack.OPTIONAL_CODEC);
        tag.put("Revertible", MineraculousNbtUtils.writeStringRowKeyedTable(revertibleItems, UUID::toString, NbtUtils::createUUID, itemEncoder));
        tag.put("RevertMarked", MineraculousNbtUtils.writeStringKeyedMap(revertMarkedItems, UUID::toString, itemEncoder));
        tag.put("Reverted", MineraculousNbtUtils.writeCollection(revertedItems, NbtUtils::createUUID));
        tag.put("Kamikotized", MineraculousNbtUtils.writeStringKeyedMap(kamikotizedItems, UUID::toString, itemEncoder));
        return tag;
    }

    public static ItemReversionData load(CompoundTag tag, HolderLookup.Provider registries) {
        Function<Tag, ItemStack> itemDecoder = MineraculousNbtUtils.codecDecoder(ItemStack.OPTIONAL_CODEC);
        ItemReversionData data = new ItemReversionData();
        data.revertibleItems.putAll(MineraculousNbtUtils.readStringRowKeyedTable(HashBasedTable::create, tag.getCompound("Revertible"), UUID::fromString, NbtUtils::loadUUID, itemDecoder));
        data.revertMarkedItems.putAll(MineraculousNbtUtils.readStringKeyedMap(Reference2ReferenceOpenHashMap::new, tag.getCompound("RevertMarked"), UUID::fromString, itemDecoder));
        data.revertedItems.addAll(MineraculousNbtUtils.readCollection(ReferenceOpenHashSet::new, tag.getList("Reverted", Tag.TAG_INT_ARRAY), NbtUtils::loadUUID));
        data.kamikotizedItems.putAll(MineraculousNbtUtils.readStringKeyedMap(Reference2ReferenceOpenHashMap::new, tag.getCompound("Kamikotized"), UUID::fromString, itemDecoder));
        return data;
    }
}
