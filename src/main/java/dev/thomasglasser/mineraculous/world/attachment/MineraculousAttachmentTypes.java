package dev.thomasglasser.mineraculous.world.attachment;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizationData;
import dev.thomasglasser.mineraculous.world.level.storage.KamikotizedMiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class MineraculousAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Mineraculous.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ArmorData>> STORED_ARMOR = ATTACHMENT_TYPES.register("stored_armor", () -> AttachmentType.builder(ArmorData::new).serialize(ArmorData.CODEC).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MiraculousDataSet>> MIRACULOUS = ATTACHMENT_TYPES.register("miraculous", () -> AttachmentType.builder(() -> new MiraculousDataSet()).serialize(MiraculousDataSet.CODEC).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<KamikotizationData>> KAMIKOTIZATION = ATTACHMENT_TYPES.register("kamikotization", () -> AttachmentType.builder(KamikotizationData::new).serialize(KamikotizationData.CODEC).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<KamikotizedMiraculousData>> KAMIKOTIZED_MIRACULOUS = ATTACHMENT_TYPES.register("kamikotized_miraculous", () -> AttachmentType.builder(KamikotizedMiraculousData::new).serialize(KamikotizedMiraculousData.CODEC).build());

    public static void init() {}
}
