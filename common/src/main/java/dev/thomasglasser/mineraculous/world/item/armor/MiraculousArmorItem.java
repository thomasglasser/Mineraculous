package dev.thomasglasser.mineraculous.world.item.armor;

import dev.thomasglasser.mineraculous.client.renderer.armor.MiraculousArmorItemRenderer;
import dev.thomasglasser.tommylib.api.world.item.armor.BaseGeoArmorItem;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MiraculousArmorItem extends BaseGeoArmorItem
{
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final String miraculous;

	public MiraculousArmorItem(String miraculous, Type type, Properties pProperties) {
		super(MineraculousArmorMaterials.MIRACULOUS.asHolder(), type, pProperties);
		this.miraculous = miraculous;
	}

	@Override
	public GeoArmorRenderer<?> newRenderer() {
		return new MiraculousArmorItemRenderer(miraculous);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	@Override
	public boolean isSkintight() {
		return true;
	}

	public String getMiraculousName() {
		return miraculous;
	}

	@Override
	public boolean isEnchantable(ItemStack stack)
	{
		return false;
	}

	@Override
	public boolean isFoil(ItemStack stack)
	{
		return false;
	}
}
