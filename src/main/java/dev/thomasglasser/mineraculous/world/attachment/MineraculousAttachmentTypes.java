package dev.thomasglasser.mineraculous.world.attachment;

import com.google.common.collect.HashBasedTable;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityEffectData;
import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationLookData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import dev.thomasglasser.mineraculous.world.level.storage.SuitLookData;
import dev.thomasglasser.mineraculous.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MineraculousAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Mineraculous.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MiraculousesData>> MIRACULOUSES = ATTACHMENT_TYPES.register("miraculouses", () -> AttachmentType.builder(() -> new MiraculousesData()).serialize(MiraculousesData.CODEC).copyOnDeath().build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HashBasedTable<ResourceKey<Miraculous>, String, MiraculousLookData>>> MIRACULOUS_MIRACULOUS_LOOKS = ATTACHMENT_TYPES.register("miraculous_miraculous_looks", () -> AttachmentType.builder(() -> HashBasedTable.<ResourceKey<Miraculous>, String, MiraculousLookData>create()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<HashBasedTable<ResourceKey<Miraculous>, String, SuitLookData>>> MIRACULOUS_SUIT_LOOKS = ATTACHMENT_TYPES.register("miraculous_suit_looks", () -> AttachmentType.builder(() -> HashBasedTable.<ResourceKey<Miraculous>, String, SuitLookData>create()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<ArmorData>>> STORED_ARMOR = ATTACHMENT_TYPES.register("stored_armor", () -> AttachmentType.builder(Optional::<ArmorData>empty).serialize(ArmorData.CODEC.optionalFieldOf("data").codec()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<KamikotizationData>>> KAMIKOTIZATION = ATTACHMENT_TYPES.register("kamikotization", () -> AttachmentType.builder(Optional::<KamikotizationData>empty).serialize(KamikotizationData.CODEC.optionalFieldOf("data").codec()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<ResourceKey<Kamikotization>>>> OLD_KAMIKOTIZATION = ATTACHMENT_TYPES.register("old_kamikotization", () -> AttachmentType.builder(Optional::<ResourceKey<Kamikotization>>empty).serialize(ResourceKey.codec(MineraculousRegistries.KAMIKOTIZATION).optionalFieldOf("data").codec()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Reference2ReferenceOpenHashMap<ResourceKey<Kamikotization>, KamikotizationLookData>>> KAMIKOTIZATION_LOOKS = ATTACHMENT_TYPES.register("kamikotization_looks", () -> AttachmentType.builder(() -> new Reference2ReferenceOpenHashMap<ResourceKey<Kamikotization>, KamikotizationLookData>()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ThrownLadybugYoyoData>> THROWN_LADYBUG_YOYO = ATTACHMENT_TYPES.register("thrown_ladybug_yoyo", () -> AttachmentType.builder(() -> new ThrownLadybugYoyoData()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AbilityEffectData>> ABILITY_EFFECTS = ATTACHMENT_TYPES.register("ability_effects", () -> AttachmentType.builder(AbilityEffectData::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ReferenceArrayList<UUID>>> INVENTORY_TRACKERS = ATTACHMENT_TYPES.register("inventory_trackers", () -> AttachmentType.builder(() -> new ReferenceArrayList<UUID>()).build());

    public static void init() {}
}
