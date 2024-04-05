package dev.thomasglasser.mineraculous.platform;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.network.ClientboundSyncMiraculousDataSetPacket;
import dev.thomasglasser.mineraculous.platform.services.DataHelper;
import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.LivingEntity;

@SuppressWarnings({"UnstableApiUsage"})
public class FabricDataHelper implements DataHelper
{
	private static final AttachmentType<ArmorData> STORED_ARMOR = AttachmentRegistry.<ArmorData>builder().initializer(ArmorData::new).persistent(ArmorData.CODEC).buildAndRegister(Mineraculous.modLoc("stored_armor"));
	private static final AttachmentType<MiraculousDataSet> MIRACULOUS = AttachmentRegistry.<MiraculousDataSet>builder().initializer(MiraculousDataSet::new).persistent(MiraculousDataSet.CODEC).buildAndRegister(Mineraculous.modLoc("miraculous"));

	@Override
	public ArmorData getStoredArmor(LivingEntity entity)
	{
		return entity.getAttachedOrCreate(STORED_ARMOR);
	}

	@Override
	public void setStoredArmor(LivingEntity entity, ArmorData data)
	{
		entity.setAttached(STORED_ARMOR, data);
	}

	@Override
	public MiraculousDataSet getMiraculousDataSet(LivingEntity entity)
	{
		return entity.getAttachedOrCreate(MIRACULOUS);
	}

	@Override
	public void setMiraculousDataSet(LivingEntity entity, MiraculousDataSet data, boolean syncToClient)
	{
		entity.setAttached(MIRACULOUS, data);
		if (syncToClient) TommyLibServices.NETWORK.sendToAllClients(ClientboundSyncMiraculousDataSetPacket.ID, ClientboundSyncMiraculousDataSetPacket::new, ClientboundSyncMiraculousDataSetPacket.write(data, entity), entity.level().getServer());
	}
}
