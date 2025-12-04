package dev.thomasglasser.mineraculous.impl.world.level.storage;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.nbt.MineraculousNbtUtils;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.impl.world.item.component.LuckyCharm;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

public class LuckyCharmIdData extends SavedData {
    public static final String FILE_ID = "lucky_charm_id";
    private final Object2IntMap<UUID> luckyCharmIds = new Object2IntOpenHashMap<>();

    public static LuckyCharmIdData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(LuckyCharmIdData.factory(), LuckyCharmIdData.FILE_ID);
    }

    public static Factory<LuckyCharmIdData> factory() {
        return new Factory<>(LuckyCharmIdData::new, LuckyCharmIdData::load, DataFixTypes.LEVEL);
    }

    public void tick(Entity entity) {
        for (ItemStack stack : MineraculousEntityUtils.getInventoryAndCurios(entity)) {
            LuckyCharm luckyCharm = stack.get(MineraculousDataComponents.LUCKY_CHARM);
            if (luckyCharm != null) {
                int charmId = luckyCharm.id();
                int currentId = getLuckyCharmId(luckyCharm.owner());
                if (charmId != currentId)
                    stack.setCount(0);
            }
        }
    }

    public int getLuckyCharmId(UUID uuid) {
        return luckyCharmIds.computeIfAbsent(uuid, newUuid -> 0);
    }

    public int incrementLuckyCharmId(UUID uuid) {
        Integer id = luckyCharmIds.compute(uuid, (oldUUID, oldId) -> oldId == null ? 0 : oldId + 1);
        setDirty();
        return id;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        tag.put("LuckyCharmIds", MineraculousNbtUtils.writeStringKeyedMap(luckyCharmIds, UUID::toString, MineraculousNbtUtils.codecEncoder(Codec.INT, ops)));
        return tag;
    }

    public static LuckyCharmIdData load(CompoundTag tag, HolderLookup.Provider registries) {
        RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
        LuckyCharmIdData data = new LuckyCharmIdData();
        data.luckyCharmIds.putAll(MineraculousNbtUtils.readStringKeyedMap(Object2IntOpenHashMap::new, tag.getCompound("LuckyCharmIds"), UUID::fromString, MineraculousNbtUtils.codecDecoder(Codec.INT, ops)));
        return data;
    }
}
