package dev.thomasglasser.miraculous.platform;

import dev.thomasglasser.miraculous.Miraculous;
import dev.thomasglasser.miraculous.platform.services.DataHelper;
import dev.thomasglasser.miraculous.world.level.storage.ArmorData;
import dev.thomasglasser.miraculous.world.level.storage.MiraculousData;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.world.entity.LivingEntity;

@SuppressWarnings({"UnstableApiUsage"})
public class FabricDataHelper implements DataHelper
{
	private static final AttachmentType<ArmorData> STORED_ARMOR = AttachmentRegistry.<ArmorData>builder().initializer(ArmorData::new).persistent(ArmorData.CODEC).buildAndRegister(Miraculous.modLoc("stored_armor"));
	private static final AttachmentType<MiraculousData> MIRACULOUS = AttachmentRegistry.<MiraculousData>builder().initializer(MiraculousData::new).persistent(MiraculousData.CODEC).buildAndRegister(Miraculous.modLoc("miraculous"));

	@Override
	public ArmorData getStoredArmor(LivingEntity entity)
	{
		return entity.getAttachedOrCreate(STORED_ARMOR);
	}

	@Override
	public void setStoredArmor(ArmorData data, LivingEntity entity)
	{
		entity.setAttached(STORED_ARMOR, data);
	}

	@Override
	public MiraculousData getMiraculousData(LivingEntity entity)
	{
		return entity.getAttachedOrCreate(MIRACULOUS);
	}

	@Override
	public void setMiraculousData(MiraculousData data, LivingEntity entity)
	{
		entity.setAttached(MIRACULOUS, data);
	}
}
