package dev.thomasglasser.mineraculous.api.core.component;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.impl.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.impl.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.impl.world.item.component.Active;
import dev.thomasglasser.mineraculous.impl.world.item.component.EatingItem;
import dev.thomasglasser.mineraculous.impl.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.impl.world.item.component.KwamiFoods;
import dev.thomasglasser.mineraculous.impl.world.item.component.LuckyCharm;
import dev.thomasglasser.mineraculous.impl.world.level.storage.SlotInfo;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousDataComponents {
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MineraculousConstants.MOD_ID);

    // Shared
    /// If present, allows toggling {@link Active#active()} via {@link dev.thomasglasser.mineraculous.impl.client.MineraculousKeyMappings#TOGGLE_ITEM_ACTIVE}.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Active>> ACTIVE = DATA_COMPONENTS.register("active", Active.STREAM_CODEC, Active.CODEC, true);
    /// Automatically applied to {@link net.minecraft.world.entity.LivingEntity#getUseItem()} while {@link net.minecraft.world.entity.LivingEntity#isBlocking()} is true (i.e. the item is being used to block).
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> BLOCKING = DATA_COMPONENTS.registerUnit("blocking", false, true);
    /// If present, {@link net.minecraft.core.component.DataComponents#ENCHANTMENTS} is excluded from the item tooltip.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> HIDE_ENCHANTMENTS = DATA_COMPONENTS.registerUnit("hide_enchantments", true, false);
    /// The automatically applied {@link net.minecraft.world.entity.Entity#getId()} of the last entity to carry the item in its inventory.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CARRIER = DATA_COMPONENTS.register("carrier", ByteBufCodecs.VAR_INT, ExtraCodecs.NON_NEGATIVE_INT, true);
    /// The {@link UUID} of the owner of the item, NOT ALWAYS the {@link MineraculousDataComponents#CARRIER}.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> OWNER = DATA_COMPONENTS.register("owner", UUIDUtil.STREAM_CODEC, UUIDUtil.CODEC, false);

    // Miraculous Item
    /// If present, the item is in its powered form.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> POWERED = DATA_COMPONENTS.registerUnit("powered", true, true);
    /// If present, whether the item is charged.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CHARGED = DATA_COMPONENTS.registerBoolean("charged", true, true);
    /// The {@link UUID} of the {@link dev.thomasglasser.mineraculous.impl.world.entity.Kwami} associated with the stack.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> KWAMI_ID = DATA_COMPONENTS.register("kwami_id", UUIDUtil.STREAM_CODEC, UUIDUtil.CODEC, false);
    /// If present, the {@link dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData.TransformationState} of an ongoing transformation.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MiraculousData.TransformationState>> TRANSFORMATION_STATE = DATA_COMPONENTS.register("transformation_state", MiraculousData.TransformationState.STREAM_CODEC, MiraculousData.TransformationState.CODEC, true);
    /// If present, the remaining ticks in an ongoing countdown.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> REMAINING_TICKS = DATA_COMPONENTS.register("remaining_ticks", ByteBufCodecs.VAR_INT, ExtraCodecs.NON_NEGATIVE_INT, true);
    /// Determines the texture to use for a {@link MiraculousItem}.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MiraculousItem.TextureState>> TEXTURE_STATE = DATA_COMPONENTS.register("texture_state", MiraculousItem.TextureState.STREAM_CODEC, MiraculousItem.TextureState.CODEC, true);

    // Kwami Item
    /// The food data of the {@link dev.thomasglasser.mineraculous.impl.world.entity.Kwami} of a {@link dev.thomasglasser.mineraculous.impl.world.item.KwamiItem}.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<KwamiFoods>> KWAMI_FOODS = DATA_COMPONENTS.register("kwami_foods", KwamiFoods.STREAM_CODEC, KwamiFoods.CODEC, false);
    /// The eating data of a {@link dev.thomasglasser.mineraculous.impl.world.item.KwamiItem}.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EatingItem>> EATING_ITEM = DATA_COMPONENTS.register("eating_item", EatingItem.STREAM_CODEC, EatingItem.CODEC, true);

    // Miraculous Tools
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LadybugYoyoItem.Mode>> LADYBUG_YOYO_MODE = DATA_COMPONENTS.register("ladybug_yoyo_mode", LadybugYoyoItem.Mode.STREAM_CODEC, LadybugYoyoItem.Mode.CODEC, true);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CatStaffItem.Mode>> CAT_STAFF_MODE = DATA_COMPONENTS.register("cat_staff_mode", CatStaffItem.Mode.STREAM_CODEC, CatStaffItem.Mode.CODEC, true);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ButterflyCaneItem.Mode>> BUTTERFLY_CANE_MODE = DATA_COMPONENTS.register("butterfly_cane_mode", ButterflyCaneItem.Mode.STREAM_CODEC, ButterflyCaneItem.Mode.CODEC, true);

    // Abilities
    /// A {@link UUID} registered to {@link dev.thomasglasser.mineraculous.api.world.level.storage.ItemReversionData} for ability reversion.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> REVERTIBLE_ITEM_ID = DATA_COMPONENTS.register("revertible_item_id", UUIDUtil.STREAM_CODEC, UUIDUtil.CODEC, false);
    /// The {@link LuckyCharm} data associated with the summoned stack.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LuckyCharm>> LUCKY_CHARM = DATA_COMPONENTS.register("lucky_charm", LuckyCharm.STREAM_CODEC, LuckyCharm.CODEC, false);

    // Miraculous
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<Miraculous>>> MIRACULOUS = DATA_COMPONENTS.register("miraculous", Miraculous.STREAM_CODEC, Miraculous.CODEC, false);
    /// If present, the unique ID for the related miraculous item.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> MIRACULOUS_ID = DATA_COMPONENTS.register("miraculous_id", UUIDUtil.STREAM_CODEC, UUIDUtil.CODEC, false);
    /// The {@link Integer} id of used for tool recalling via {@link dev.thomasglasser.mineraculous.impl.world.level.storage.ToolIdData}.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TOOL_ID = DATA_COMPONENTS.register("tool_id", ByteBufCodecs.VAR_INT, ExtraCodecs.NON_NEGATIVE_INT, false);

    // Kamikotization
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<Kamikotization>>> KAMIKOTIZATION = DATA_COMPONENTS.register("kamikotization", Kamikotization.STREAM_CODEC, Kamikotization.CODEC, false);
    /// The {@link KamikoData} holding information about the {@link dev.thomasglasser.mineraculous.impl.world.entity.Kamiko} associated with the stack.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<KamikoData>> KAMIKO_DATA = DATA_COMPONENTS.register("kamiko_data", KamikoData.STREAM_CODEC, KamikoData.CODEC, true);
    /// If present, keeps the item in its assigned {@link dev.thomasglasser.mineraculous.impl.world.level.storage.SlotInfo} and applies the kamikotizing shader.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SlotInfo>> KAMIKOTIZING = DATA_COMPONENTS.register("kamikotizing", SlotInfo.STREAM_CODEC, false);

    @ApiStatus.Internal
    public static void init() {}
}
