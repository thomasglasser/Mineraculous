package dev.thomasglasser.mineraculous.world.attachment;

import com.google.common.collect.HashBasedTable;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationLookData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.SuitLookData;
import dev.thomasglasser.mineraculous.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import java.util.HashMap;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MineraculousAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Mineraculous.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MiraculousDataSet>> MIRACULOUS = ATTACHMENT_TYPES.register("miraculous", () -> AttachmentType.builder(() -> new MiraculousDataSet()).serialize(MiraculousDataSet.CODEC).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HashBasedTable<ResourceKey<Miraculous>, String, MiraculousLookData>>> MIRACULOUS_MIRACULOUS_LOOKS = ATTACHMENT_TYPES.register("miraculous_miraculous_looks", () -> AttachmentType.builder(() -> HashBasedTable.<ResourceKey<Miraculous>, String, MiraculousLookData>create()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HashBasedTable<ResourceKey<Miraculous>, String, SuitLookData>>> MIRACULOUS_SUIT_LOOKS = ATTACHMENT_TYPES.register("miraculous_suit_looks", () -> AttachmentType.builder(() -> HashBasedTable.<ResourceKey<Miraculous>, String, SuitLookData>create()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<ArmorData>>> STORED_ARMOR = ATTACHMENT_TYPES.register("stored_armor", () -> AttachmentType.builder(Optional::<ArmorData>empty).serialize(ArmorData.CODEC.optionalFieldOf("data").codec()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<KamikotizationData>>> KAMIKOTIZATION = ATTACHMENT_TYPES.register("kamikotization", () -> AttachmentType.builder(Optional::<KamikotizationData>empty).serialize(KamikotizationData.CODEC.optionalFieldOf("data").codec()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<ResourceKey<Kamikotization>>>> OLD_KAMIKOTIZATION = ATTACHMENT_TYPES.register("old_kamikotization", () -> AttachmentType.builder(Optional::<ResourceKey<Kamikotization>>empty).serialize(ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).optionalFieldOf("data").codec()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HashMap<ResourceKey<Kamikotization>, KamikotizationLookData>>> KAMIKOTIZATION_LOOKS = ATTACHMENT_TYPES.register("kamikotization_looks", () -> AttachmentType.builder(() -> new HashMap<ResourceKey<Kamikotization>, KamikotizationLookData>()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ThrownLadybugYoyoData>> THROWN_LADYBUG_YOYO = ATTACHMENT_TYPES.register("thrown_ladybug_yoyo", () -> AttachmentType.builder(() -> new ThrownLadybugYoyoData()).build());

    public static void init() {}
}
