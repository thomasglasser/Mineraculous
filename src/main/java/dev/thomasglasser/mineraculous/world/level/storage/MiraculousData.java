package dev.thomasglasser.mineraculous.world.level.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.tommylib.api.network.codec.ExtraStreamCodecs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

public record MiraculousData(boolean transformed, ItemStack miraculousItem, CuriosData curiosData, int toolId, int powerLevel, boolean mainPowerActivated, boolean mainPowerActive, String name, String miraculousLook, String suitLook, CompoundTag extraData) {

    public static final Codec<MiraculousData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("transformed").forGetter(MiraculousData::transformed),
            ItemStack.OPTIONAL_CODEC.fieldOf("miraculous_item").forGetter(MiraculousData::miraculousItem),
            CuriosData.CODEC.fieldOf("curios_data").forGetter(MiraculousData::curiosData),
            Codec.INT.fieldOf("tool_id").forGetter(MiraculousData::toolId),
            Codec.INT.fieldOf("power_level").forGetter(MiraculousData::powerLevel),
            Codec.BOOL.fieldOf("main_power_activated").forGetter(MiraculousData::mainPowerActivated),
            Codec.BOOL.fieldOf("main_power_active").forGetter(MiraculousData::mainPowerActive),
            Codec.STRING.optionalFieldOf("name", "").forGetter(MiraculousData::name),
            Codec.STRING.optionalFieldOf("miraculous_look", "").forGetter(MiraculousData::miraculousLook),
            Codec.STRING.optionalFieldOf("suit_look", "").forGetter(MiraculousData::suitLook),
            CompoundTag.CODEC.optionalFieldOf("extra_data", new CompoundTag()).forGetter(MiraculousData::extraData)).apply(instance, MiraculousData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MiraculousData> STREAM_CODEC = ExtraStreamCodecs.composite(
            ByteBufCodecs.BOOL, MiraculousData::transformed,
            ItemStack.OPTIONAL_STREAM_CODEC, MiraculousData::miraculousItem,
            CuriosData.STREAM_CODEC, MiraculousData::curiosData,
            ByteBufCodecs.INT, MiraculousData::toolId,
            ByteBufCodecs.INT, MiraculousData::powerLevel,
            ByteBufCodecs.BOOL, MiraculousData::mainPowerActivated,
            ByteBufCodecs.BOOL, MiraculousData::mainPowerActive,
            ByteBufCodecs.STRING_UTF8, MiraculousData::name,
            ByteBufCodecs.STRING_UTF8, MiraculousData::miraculousLook,
            ByteBufCodecs.STRING_UTF8, MiraculousData::suitLook,
            ByteBufCodecs.COMPOUND_TAG, MiraculousData::extraData,
            MiraculousData::new);

    public static final String NAME_NOT_SET = "miraculous_data.name.not_set";
    public MiraculousData(boolean transformed, ItemStack miraculousItem, CuriosData curiosData, int toolId, int powerLevel, boolean mainPowerActivated, boolean mainPowerActive, String name, String miraculousLook, String suitLook, CompoundTag extraData) {
        this.transformed = transformed;
        this.miraculousItem = miraculousItem;
        this.curiosData = curiosData;
        this.toolId = toolId;
        this.powerLevel = Math.clamp(powerLevel, 0, 100);
        this.mainPowerActivated = mainPowerActivated;
        this.mainPowerActive = mainPowerActive;
        this.name = name;
        this.miraculousLook = miraculousLook;
        this.suitLook = suitLook;
        this.extraData = extraData;
    }

    public MiraculousData() {
        this(false, ItemStack.EMPTY, CuriosData.EMPTY, 0, 0, false, false, "", "", "", new CompoundTag());
    }

    public boolean hasLimitedPower() {
        return MineraculousServerConfig.get().enableLimitedPower.get() && powerLevel < 100;
    }

    public boolean usedLimitedPower() {
        return mainPowerActivated() && hasLimitedPower();
    }

    public boolean shouldCountDown() {
        return MineraculousServerConfig.get().enableMiraculousTimer.get() && usedLimitedPower();
    }

    public ItemStack createTool(ServerPlayer player) {
        ResourceKey<Miraculous> key = miraculousItem().get(MineraculousDataComponents.MIRACULOUS);
        if (key != null) {
            Miraculous miraculous = player.level().holderOrThrow(key).value();
            if (miraculous.tool().isPresent()) {
                ItemStack tool = miraculous.tool().get();
                tool.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
                tool.set(MineraculousDataComponents.KWAMI_DATA.get(), miraculousItem().get(MineraculousDataComponents.KWAMI_DATA.get()));
                tool.set(MineraculousDataComponents.TOOL_ID.get(), toolId());
                return tool;
            }
        }
        return ItemStack.EMPTY;
    }

    public MiraculousData equip(ItemStack miraculousItem, CuriosData curiosData) {
        return new MiraculousData(false, miraculousItem, curiosData, toolId, powerLevel, false, false, name, miraculousLook, suitLook, extraData);
    }

    public MiraculousData unEquip() {
        return new MiraculousData(transformed, ItemStack.EMPTY, CuriosData.EMPTY, toolId, powerLevel, false, false, name, miraculousLook, suitLook, extraData);
    }

    public MiraculousData transform(boolean transformed, ItemStack miraculousItem, int toolId) {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, powerLevel, false, false, name, miraculousLook, suitLook, extraData);
    }

    public MiraculousData withItem(ItemStack miraculousItem) {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, powerLevel, mainPowerActivated, mainPowerActive, name, miraculousLook, suitLook, extraData);
    }

    public MiraculousData withCuriosData(CuriosData curiosData) {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, powerLevel, mainPowerActivated, mainPowerActive, name, miraculousLook, suitLook, extraData);
    }

    public MiraculousData withPowerStatus(boolean activated, boolean active) {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, powerLevel, activated, active, name, miraculousLook, suitLook, extraData);
    }

    public MiraculousData withUsedPower() {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, powerLevel + 1, true, false, name, miraculousLook, suitLook, extraData);
    }

    public MiraculousData withName(String name) {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, powerLevel, mainPowerActivated, mainPowerActive, name, miraculousLook, suitLook, extraData);
    }

    public MiraculousData withMiraculousLook(String miraculousLook) {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, powerLevel, mainPowerActivated, mainPowerActive, name, miraculousLook, suitLook, extraData);
    }

    public MiraculousData withSuitLook(String suitLook) {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, powerLevel, mainPowerActivated, mainPowerActive, name, miraculousLook, suitLook, extraData);
    }

    public MiraculousData withLevel(int level) {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, level, mainPowerActivated, mainPowerActive, name, miraculousLook, suitLook, extraData);
    }

    public MiraculousData withExtraData(CompoundTag extraData, boolean replace) {
        return new MiraculousData(transformed, miraculousItem, curiosData, toolId, powerLevel, mainPowerActivated, mainPowerActive, name, miraculousLook, suitLook, replace ? extraData : this.extraData.merge(extraData));
    }
}
