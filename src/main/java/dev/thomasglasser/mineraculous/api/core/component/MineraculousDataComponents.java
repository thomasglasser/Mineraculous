package dev.thomasglasser.mineraculous.api.core.component;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.api.world.item.component.ActiveSettings;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.impl.world.item.ButterflyCaneItem;
import dev.thomasglasser.mineraculous.impl.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.impl.world.item.LadybugYoyoItem;
import dev.thomasglasser.mineraculous.impl.world.item.component.Active;
import dev.thomasglasser.mineraculous.impl.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.impl.world.item.component.Kamikotizing;
import dev.thomasglasser.mineraculous.impl.world.item.component.KwamiFoods;
import dev.thomasglasser.mineraculous.impl.world.item.component.LuckyCharm;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousDataComponents {
    private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MineraculousConstants.MOD_ID);

    // Shared
    /**
     * Whether the item is configured to be usable.
     * If present, can be toggled by {@link MineraculousKeyMappings#TOGGLE_ITEM_ACTIVE}.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Active>> ACTIVE = DATA_COMPONENTS.register("active", Active.STREAM_CODEC, Active.CODEC, true);
    /// Settings to use when toggling the {@link MineraculousDataComponents#ACTIVE} component.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ActiveSettings>> ACTIVE_SETTINGS = DATA_COMPONENTS.register("active_settings", ActiveSettings.STREAM_CODEC, ActiveSettings.CODEC, false);
    /**
     * Automatically applied.
     * If present, the item is currently being used to block like a shield.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> BLOCKING = DATA_COMPONENTS.registerUnit("blocking", false, true);
    /// If present, enchantments are excluded from the item tooltip.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> HIDE_ENCHANTMENTS = DATA_COMPONENTS.registerUnit("hide_enchantments", true, false);
    /**
     * Automatically applied.
     * The {@link Integer} id of the entity carrying the item in its inventory.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CARRIER = DATA_COMPONENTS.registerInteger("carrier", true, true);
    /// The {@link UUID} of the owner of the item, NOT always the {@link MineraculousDataComponents#CARRIER}.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> OWNER = DATA_COMPONENTS.register("owner", UUIDUtil.STREAM_CODEC, UUIDUtil.CODEC, false);

    // Miraculous Item
    /// If present, the item is in its powered form.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> POWERED = DATA_COMPONENTS.registerUnit("powered", true, true);
    /// If present, whether the item is charged.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CHARGED = DATA_COMPONENTS.registerBoolean("charged", true, true);
    /// The {@link KwamiData} holding information about the {@link Kwami} associated with the stack.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> KWAMI_ID = DATA_COMPONENTS.register("kwami_data", UUIDUtil.STREAM_CODEC, UUIDUtil.CODEC, false);
    /// If present, the remaining transformation frames in an ongoing transformation.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TRANSFORMATION_FRAMES = DATA_COMPONENTS.registerInteger("transformation_frames", true, true);
    /// If present, the remaining detransformation frames in an ongoing detransformation.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> DETRANSFORMATION_FRAMES = DATA_COMPONENTS.registerInteger("detransformation_frames", true, true);
    /// If present, the remaining ticks in an ongoing countdown.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> REMAINING_TICKS = DATA_COMPONENTS.registerInteger("remaining_ticks", true, true);
    /// Determines the texture to use for the item.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MiraculousItem.TextureState>> TEXTURE_STATE = DATA_COMPONENTS.register("texture_state", MiraculousItem.TextureState.STREAM_CODEC, MiraculousItem.TextureState.CODEC, true);

    // Kwami Item
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<KwamiFoods>> KWAMI_FOODS = DATA_COMPONENTS.register("kwami_foods", KwamiFoods.STREAM_CODEC, KwamiFoods.CODEC, false);

    // Miraculous Tools
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LadybugYoyoItem.Mode>> LADYBUG_YOYO_MODE = DATA_COMPONENTS.register("ladybug_yoyo_mode", LadybugYoyoItem.Mode.STREAM_CODEC, LadybugYoyoItem.Mode.CODEC, true);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CatStaffItem.Mode>> CAT_STAFF_MODE = DATA_COMPONENTS.register("cat_staff_mode", CatStaffItem.Mode.STREAM_CODEC, CatStaffItem.Mode.CODEC, true);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ButterflyCaneItem.Mode>> BUTTERFLY_CANE_MODE = DATA_COMPONENTS.register("butterfly_cane_mode", ButterflyCaneItem.Mode.STREAM_CODEC, ButterflyCaneItem.Mode.CODEC, true);

    // Abilities
    /// A {@link UUID} registered to {@link AbilityReversionItemData} for ability reversion.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> REVERTIBLE_ITEM_ID = DATA_COMPONENTS.register("revertible_item_id", UUIDUtil.STREAM_CODEC, UUIDUtil.CODEC, false);
    /// The {@link LuckyCharm} data associated with the summoned stack.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LuckyCharm>> LUCKY_CHARM = DATA_COMPONENTS.register("lucky_charm", LuckyCharm.STREAM_CODEC, LuckyCharm.CODEC, false);

    // Miraculous
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<Miraculous>>> MIRACULOUS = DATA_COMPONENTS.register("miraculous", Miraculous.STREAM_CODEC, Miraculous.CODEC, false);
    /// If present, the unique ID for the related miraculous.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> MIRACULOUS_ID = DATA_COMPONENTS.register("miraculous_id", UUIDUtil.STREAM_CODEC, UUIDUtil.CODEC, false);
    /// The {@link Integer} id of used for tool recalling via {@link ToolIdData}.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TOOL_ID = DATA_COMPONENTS.registerInteger("tool_id", true, false);

    // Kamikotization
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Holder<Kamikotization>>> KAMIKOTIZATION = DATA_COMPONENTS.register("kamikotization", Kamikotization.STREAM_CODEC, Kamikotization.CODEC, false);
    /// The {@link KamikoData} holding information about the {@link Kamiko} associated with the stack.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<KamikoData>> KAMIKO_DATA = DATA_COMPONENTS.register("kamiko_data", KamikoData.STREAM_CODEC, KamikoData.CODEC, true);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Kamikotizing>> KAMIKOTIZING = DATA_COMPONENTS.register("kamikotizing", Kamikotizing.STREAM_CODEC, false);

    @ApiStatus.Internal
    public static void init() {}
}
