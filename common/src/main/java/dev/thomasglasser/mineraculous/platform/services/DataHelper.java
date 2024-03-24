package dev.thomasglasser.mineraculous.platform.services;

import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import net.minecraft.world.entity.LivingEntity;

public interface DataHelper
{
	ArmorData getStoredArmor(LivingEntity entity);
	void setStoredArmor(ArmorData data, LivingEntity entity);
	MiraculousData getMiraculousData(LivingEntity entity);
	void setMiraculousData(MiraculousData data, LivingEntity entity, boolean syncToClient);
}
