package dev.thomasglasser.mineraculous.api.world.attachment;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.kamikotization.KamikotizationData;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityEffectData;
import dev.thomasglasser.mineraculous.api.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.world.level.storage.LeashingLadybugYoyoData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.PerchCatStaffData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.TravelCatStaffData;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousAttachmentTypes {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Mineraculous.MOD_ID);

    // Shared
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<ArmorData>>> STORED_ARMOR = ATTACHMENT_TYPES.register("stored_armor", () -> AttachmentType.builder(Optional::<ArmorData>empty).serialize(optionalCodec(ArmorData.CODEC)).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AbilityEffectData>> ABILITY_EFFECTS = ATTACHMENT_TYPES.register("ability_effects", () -> AttachmentType.builder(AbilityEffectData::new).build());
    /// Stores a list of clients to sync the holder's inventory to
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ReferenceArrayList<UUID>>> INVENTORY_TRACKERS = ATTACHMENT_TYPES.register("inventory_trackers", () -> AttachmentType.builder(() -> new ReferenceArrayList<UUID>()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ThrownLadybugYoyoData>> THROWN_LADYBUG_YOYO = ATTACHMENT_TYPES.register("thrown_ladybug_yoyo", () -> AttachmentType.builder(() -> new ThrownLadybugYoyoData()).build());
    /// If true, overrides leash rendering and snapping
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<UUID>>> YOYO_LEASH_HOLDER = ATTACHMENT_TYPES.register("yoyo_leash_holder", () -> AttachmentType.builder(Optional::<UUID>empty).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<LeashingLadybugYoyoData>>> LEASHING_LADYBUG_YOYO = ATTACHMENT_TYPES.register("lasso_ladybug_yoyo", () -> AttachmentType.builder(Optional::<LeashingLadybugYoyoData>empty).build());

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PerchCatStaffData>> PERCH_CAT_STAFF = ATTACHMENT_TYPES.register("cat_staff_perch", () -> AttachmentType.builder(() -> new PerchCatStaffData()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<TravelCatStaffData>> TRAVEL_CAT_STAFF = ATTACHMENT_TYPES.register("cat_staff_travel", () -> AttachmentType.builder(() -> new TravelCatStaffData()).build());

    // Miraculous
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MiraculousesData>> MIRACULOUSES = ATTACHMENT_TYPES.register("miraculouses", () -> AttachmentType.builder(() -> new MiraculousesData()).serialize(MiraculousesData.CODEC).copyOnDeath().build());

    // Kamikotization
    /// If present, stores the holder's actively transformed {@link KamikotizationData}.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<KamikotizationData>>> KAMIKOTIZATION = ATTACHMENT_TYPES.register("kamikotization", () -> AttachmentType.builder(Optional::<KamikotizationData>empty).serialize(optionalCodec(KamikotizationData.CODEC)).build());
    /// If present, stores the previously transformed {@link Kamikotization} of the detransformed holder.
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<Holder<Kamikotization>>>> OLD_KAMIKOTIZATION = ATTACHMENT_TYPES.register("old_kamikotization", () -> AttachmentType.builder(Optional::<Holder<Kamikotization>>empty).serialize(optionalCodec(Kamikotization.CODEC)).build());

    private static <T> Codec<Optional<T>> optionalCodec(Codec<T> codec) {
        return codec.optionalFieldOf("data").codec();
    }

    @ApiStatus.Internal
    public static void init() {}
}
