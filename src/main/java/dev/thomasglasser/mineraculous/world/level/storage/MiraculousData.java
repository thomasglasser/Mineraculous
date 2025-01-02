package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.tommylib.api.network.NetworkUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public record MiraculousData(boolean transformed, ItemStack miraculousItem, CuriosData curiosData, int toolId, int powerLevel, boolean mainPowerActivated, boolean mainPowerActive, String name, CompoundTag extraData) {

    public static final Codec<MiraculousData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("transformed").forGetter(MiraculousData::transformed),
            ItemStack.OPTIONAL_CODEC.fieldOf("miraculous_item").forGetter(MiraculousData::miraculousItem),
            CuriosData.CODEC.fieldOf("curios_data").forGetter(MiraculousData::curiosData),
            Codec.INT.fieldOf("tool_id").forGetter(MiraculousData::toolId),
            Codec.INT.fieldOf("power_level").forGetter(MiraculousData::powerLevel),
            Codec.BOOL.fieldOf("main_power_activated").forGetter(MiraculousData::mainPowerActivated),
            Codec.BOOL.fieldOf("main_power_active").forGetter(MiraculousData::mainPowerActive),
            Codec.STRING.optionalFieldOf("name", "").forGetter(MiraculousData::name),
            CompoundTag.CODEC.optionalFieldOf("extra_data", new CompoundTag()).forGetter(MiraculousData::extraData)).apply(instance, MiraculousData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousData> STREAM_CODEC = NetworkUtils.composite(
            ByteBufCodecs.BOOL, MiraculousData::transformed,
            ItemStack.OPTIONAL_STREAM_CODEC, MiraculousData::miraculousItem,
            CuriosData.STREAM_CODEC, MiraculousData::curiosData,
            ByteBufCodecs.INT, MiraculousData::toolId,
            ByteBufCodecs.INT, MiraculousData::powerLevel,
            ByteBufCodecs.BOOL, MiraculousData::mainPowerActivated,
            ByteBufCodecs.BOOL, MiraculousData::mainPowerActive,
            ByteBufCodecs.STRING_UTF8, MiraculousData::name,
            ByteBufCodecs.COMPOUND_TAG, MiraculousData::extraData,
            MiraculousData::new);

    public static final String NAME_NOT_SET = "miraculous_data.name.not_set";
    public MiraculousData() {
        this(false, ItemStack.EMPTY, new CuriosData(), 0, 0, false, false, "", new CompoundTag());
    }

    public ItemStack createTool(ServerLevel level) {
        if (miraculousItem().has(MineraculousDataComponents.MIRACULOUS)) {
            Miraculous miraculous = level.holderOrThrow(miraculousItem().get(MineraculousDataComponents.MIRACULOUS)).value();
            if (miraculous.tool().isPresent()) {
                ItemStack tool = miraculous.tool().get().getDefaultInstance();
                tool.set(MineraculousDataComponents.KWAMI_DATA.get(), miraculousItem().get(MineraculousDataComponents.KWAMI_DATA.get()));
                tool.set(MineraculousDataComponents.TOOL_ID.get(), toolId());
                return tool;
            }
        }
        return ItemStack.EMPTY;
    }

    public MiraculousData equip(ItemStack miraculousItem, CuriosData curiosData) {
        return new MiraculousData(false, miraculousItem, curiosData, toolId, powerLevel, false, false, name, extraData);
    }

    public MiraculousData transform(boolean transformed, ItemStack miraculousItem, int toolId) {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, powerLevel, false, false, name, extraData);
    }

    public MiraculousData withItem(ItemStack miraculousItem) {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, powerLevel, mainPowerActivated, mainPowerActive, name, extraData);
    }

    public MiraculousData withPowerStatus(boolean activated, boolean active) {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, powerLevel + (activated ? 1 : -1), activated, active, name, extraData);
    }

    public MiraculousData withName(String name) {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, powerLevel, mainPowerActivated, mainPowerActive, name, extraData);
    }

    public MiraculousData withLevel(int level) {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, level, mainPowerActivated, mainPowerActive, name, extraData);
    }

    public MiraculousData withExtraData(CompoundTag extraData, boolean replace) {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, powerLevel, mainPowerActivated, mainPowerActive, name, replace ? extraData : this.extraData.merge(extraData));
    }
}
