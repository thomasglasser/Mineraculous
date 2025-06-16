package dev.thomasglasser.mineraculous.world.attachment;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityEffectData;
import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import dev.thomasglasser.mineraculous.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.mineraculous.world.level.storage.YoyoLeashData;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MineraculousAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Mineraculous.MOD_ID);

    // Shared
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<ArmorData>>> STORED_ARMOR = ATTACHMENT_TYPES.register("stored_armor", () -> AttachmentType.builder(Optional::<ArmorData>empty).serialize(optionalCodec(ArmorData.CODEC)).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AbilityEffectData>> ABILITY_EFFECTS = ATTACHMENT_TYPES.register("ability_effects", () -> AttachmentType.builder(AbilityEffectData::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ReferenceArrayList<UUID>>> INVENTORY_TRACKERS = ATTACHMENT_TYPES.register("inventory_trackers", () -> AttachmentType.builder(() -> new ReferenceArrayList<UUID>()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ThrownLadybugYoyoData>> THROWN_LADYBUG_YOYO = ATTACHMENT_TYPES.register("thrown_ladybug_yoyo", () -> AttachmentType.builder(() -> new ThrownLadybugYoyoData()).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<YoyoLeashData>>> YOYO_LEASH = ATTACHMENT_TYPES.register("yoyo_leash", () -> AttachmentType.builder(Optional::<YoyoLeashData>empty).build());

    // Miraculous
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MiraculousesData>> MIRACULOUSES = ATTACHMENT_TYPES.register("miraculouses", () -> AttachmentType.builder(() -> new MiraculousesData()).serialize(MiraculousesData.CODEC).copyOnDeath().build());

    // Kamikotization
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<KamikotizationData>>> KAMIKOTIZATION = ATTACHMENT_TYPES.register("kamikotization", () -> AttachmentType.builder(Optional::<KamikotizationData>empty).serialize(optionalCodec(KamikotizationData.CODEC)).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Optional<Holder<Kamikotization>>>> OLD_KAMIKOTIZATION = ATTACHMENT_TYPES.register("old_kamikotization", () -> AttachmentType.builder(Optional::<Holder<Kamikotization>>empty).serialize(optionalCodec(Kamikotization.CODEC)).build());

    public static void init() {}

    private static <T> Codec<Optional<T>> optionalCodec(Codec<T> codec) {
        return codec.optionalFieldOf("data").codec();
    }
}
