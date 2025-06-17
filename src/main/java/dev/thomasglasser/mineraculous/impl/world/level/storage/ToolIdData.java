package dev.thomasglasser.mineraculous.impl.world.level.storage;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.entity.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.item.component.KwamiData;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

public class ToolIdData extends SavedData {
    public static final String FILE_ID = "tool_id";
    private final Object2IntMap<UUID> toolIdMap = new Object2IntOpenHashMap<>();

    public static ToolIdData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ToolIdData.factory(), ToolIdData.FILE_ID);
    }

    public static Factory<ToolIdData> factory() {
        return new Factory<>(ToolIdData::new, ToolIdData::load, DataFixTypes.LEVEL);
    }

    public void tick(Entity entity) {
        Set<ItemStack> stacks = new ReferenceOpenHashSet<>();
        stacks.addAll(EntityUtils.getInventory(entity));
        if (entity instanceof LivingEntity livingEntity) {
            stacks.addAll(CuriosUtils.getAllItems(livingEntity).values());
        }
        for (ItemStack stack : stacks) {
            Integer stackId = stack.get(MineraculousDataComponents.TOOL_ID);
            if (stackId != null) {
                int currentId = getToolId(stack.get(MineraculousDataComponents.KWAMI_DATA));
                if (currentId != -1 && currentId != stackId) {
                    stack.setCount(0);
                }
            }
        }
    }

    public int getToolId(UUID uuid) {
        return toolIdMap.computeIfAbsent(uuid, newUuid -> 0);
    }

    public int getToolId(@Nullable KwamiData kwamiData) {
        return kwamiData == null ? -1 : getToolId(kwamiData.uuid());
    }

    public int incrementToolId(KwamiData kwamiData) {
        Integer id = toolIdMap.compute(kwamiData.uuid(), (uuid, oldId) -> oldId == null ? 0 : oldId + 1);
        setDirty();
        return id;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        toolIdMap.forEach((uuid, id) -> {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("Uuid", uuid);
            compoundTag.putInt("ToolId", id);
            listTag.add(compoundTag);
        });
        tag.put("ToolIds", listTag);
        return tag;
    }

    public static ToolIdData load(CompoundTag tag, HolderLookup.Provider registries) {
        ToolIdData toolIdData = new ToolIdData();
        ListTag listTag = tag.getList("ToolIds", ListTag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compoundTag = listTag.getCompound(i);
            UUID uuid = compoundTag.getUUID("Uuid");
            int toolId = compoundTag.getInt("ToolId");
            toolIdData.toolIdMap.put(uuid, toolId);
        }
        return toolIdData;
    }
}
