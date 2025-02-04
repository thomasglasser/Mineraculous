package dev.thomasglasser.mineraculous.world.level.storage;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public class ChargeOverrideData extends SavedData {
    public static final String FILE_ID = "charge_override";
    private final Object2BooleanArrayMap<UUID> map = new Object2BooleanArrayMap<>();

    public static Factory<ChargeOverrideData> factory() {
        return new Factory<>(ChargeOverrideData::new, (p_294039_, p_324123_) -> load(p_294039_), DataFixTypes.LEVEL);
    }

    public boolean has(UUID uuid) {
        return map.containsKey(uuid);
    }

    public boolean get(UUID uuid) {
        return map.getBoolean(uuid);
    }

    public void put(UUID uuid, boolean chargeOverride) {
        map.put(uuid, chargeOverride);
        setDirty();
    }

    public void remove(UUID uuid) {
        map.removeBoolean(uuid);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        map.forEach((uuid, override) -> {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putUUID("Uuid", uuid);
            compoundTag.putBoolean("Override", override);
            listTag.add(compoundTag);
        });
        tag.put("ChargeOverrides", listTag);
        return tag;
    }

    public static ChargeOverrideData load(CompoundTag tag) {
        ChargeOverrideData chargeOverrideData = new ChargeOverrideData();
        ListTag listTag = tag.getList("ChargeOverrides", 10);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag compoundTag = listTag.getCompound(i);
            UUID uuid = compoundTag.getUUID("Uuid");
            boolean override = compoundTag.getBoolean("Override");
            chargeOverrideData.map.put(uuid, override);
        }
        return chargeOverrideData;
    }
}
