package dev.thomasglasser.mineraculous.platform;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.network.ClientboundSyncMiraculousDataPacket;
import dev.thomasglasser.mineraculous.platform.services.DataHelper;
import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class NeoForgeDataHelper implements DataHelper
{
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Mineraculous.MOD_ID);

	private static final DeferredHolder<AttachmentType<?>, AttachmentType<ArmorData>> STORED_ARMOR = ATTACHMENT_TYPES.register("stored_armor", () -> AttachmentType.builder(ArmorData::new).serialize(ArmorData.CODEC).build());
	private static final DeferredHolder<AttachmentType<?>, AttachmentType<MiraculousData>> MIRACULOUS = ATTACHMENT_TYPES.register("miraculous", () -> AttachmentType.builder(MiraculousData::new).serialize(MiraculousData.CODEC).build());

	@Override
	public ArmorData getStoredArmor(LivingEntity entity)
	{
		return entity.getData(STORED_ARMOR);
	}

	@Override
	public void setStoredArmor(ArmorData data, LivingEntity entity)
	{
		entity.setData(STORED_ARMOR, data);
	}

	@Override
	public MiraculousData getMiraculousData(LivingEntity entity)
	{
		return entity.getData(MIRACULOUS);
	}

	@Override
	public void setMiraculousData(MiraculousData data, LivingEntity entity, boolean syncToClient)
	{
		entity.setData(MIRACULOUS, data);
		if (syncToClient) TommyLibServices.NETWORK.sendToAllClients(ClientboundSyncMiraculousDataPacket.class, ClientboundSyncMiraculousDataPacket.write(data, entity), entity.level().getServer());
	}
}
