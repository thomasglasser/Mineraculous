package dev.thomasglasser.mineraculous.world.item.curio;

import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CatMiraculousItemCurio extends MiraculousItemCurio
{
	@Override
	public void tick(ItemStack stack, CuriosData curiosData, LivingEntity entity)
	{
		super.tick(stack, curiosData, entity);
		MiraculousData miraculousData = Services.DATA.getMiraculousData(entity);
		if (!entity.level().isClientSide && miraculousData.powerActive())
		{
			if (!entity.getMainHandItem().isEmpty())
			{
				entity.setItemInHand(InteractionHand.MAIN_HAND, MineraculousEntityEvents.convertToMiraculousDust(entity.getMainHandItem()));
				Services.DATA.setMiraculousData(new MiraculousData(miraculousData.transformed(), miraculousData.miraculous(), miraculousData.miraculousData(), miraculousData.tool(), miraculousData.powerLevel(), true, false), entity, true);
			}
		}
	}
}
