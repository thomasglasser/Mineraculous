package dev.thomasglasser.mineraculous.impl.world.level.storage;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.impl.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.impl.world.item.component.LuckyCharm;
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

public class LuckyCharmIdData extends SavedData {
    public static final String FILE_ID = "lucky_charm_id";
    private final Object2IntMap<UUID> luckyCharmIdMap = new Object2IntOpenHashMap<>();

    public static LuckyCharmIdData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(LuckyCharmIdData.factory(), LuckyCharmIdData.FILE_ID);
    }

    public static Factory<LuckyCharmIdData> factory() {
        return new Factory<>(LuckyCharmIdData::new, (p_294039_, p_324123_) -> load(p_294039_), DataFixTypes.LEVEL);
    }

    public void tick(Entity entity) {
        Set<ItemStack> stacks = new ReferenceOpenHashSet<>();
        stacks.addAll(EntityUtils.getInventory(entity));
        if (entity instanceof LivingEntity livingEntity) {
            stacks.addAll(CuriosUtils.getAllItems(livingEntity).values());
        }
        for (ItemStack stack : stacks) {
            LuckyCharm luckyCharm = stack.get(MineraculousDataComponents.LUCKY_CHARM);
            if (luckyCharm != null) {
                int stackId = luckyCharm.id();
                KwamiData kwamiData = stack.get(MineraculousDataComponents.KWAMI_DATA);
                if (kwamiData != null) {
                    int currentId = getLuckyCharmId(kwamiData.uuid());
                    if (currentId != stackId) {
                        stack.setCount(0);
                    }
                } else if (stack.has(MineraculousDataComponents.KAMIKOTIZATION)) {
                    UUID owner = stack.get(MineraculousDataComponents.OWNER);
                    if (owner != null) {
                        int currentId = getLuckyCharmId(owner);
                        if (currentId != stackId) {
                            stack.setCount(0);
                        }
                    }
                }
            }
        }
    }

    public int getLuckyCharmId(UUID uuid) {
        return luckyCharmIdMap.computeIfAbsent(uuid, newUuid -> 0);
    }

    public int incrementLuckyCharmId(UUID uuid) {
        Integer id = luckyCharmIdMap.compute(uuid, (oldUUID, oldId) -> oldId == null ? 0 : oldId + 1);
        setDirty();
        return id;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        luckyCharmIdMap.forEach((uuid, id) -> {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("Uuid", uuid);
            compoundTag.putInt("LuckyCharmId", id);
            listTag.add(compoundTag);
        });
        tag.put("LuckyCharmIds", listTag);
        return tag;
    }

    public static LuckyCharmIdData load(CompoundTag tag) {
        LuckyCharmIdData luckyCharmIdData = new LuckyCharmIdData();
        ListTag listTag = tag.getList("LuckyCharmIds", 10);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compoundTag = listTag.getCompound(i);
            UUID uuid = compoundTag.getUUID("Uuid");
            int luckyCharmId = compoundTag.getInt("LuckyCharmId");
            luckyCharmIdData.luckyCharmIdMap.put(uuid, luckyCharmId);
        }
        return luckyCharmIdData;
    }
}
