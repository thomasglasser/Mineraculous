package dev.thomasglasser.mineraculous.platform.services;

import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import net.minecraft.world.entity.LivingEntity;

public interface DataHelper
{
	ArmorData getStoredArmor(LivingEntity entity);
	void setStoredArmor(LivingEntity entity, ArmorData data);
	MiraculousDataSet getMiraculousDataSet(LivingEntity entity);
	void setMiraculousDataSet(LivingEntity entity, MiraculousDataSet data, boolean syncToClient);
}
