package dev.thomasglasser.miraculous.world.item.armor;

import dev.thomasglasser.miraculous.client.renderer.armor.MiraculousArmorItemRenderer;
import dev.thomasglasser.tommylib.api.world.item.armor.GeoArmorItem;
import net.minecraft.world.item.ArmorItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MiraculousArmorItem extends ArmorItem implements GeoArmorItem
{
	private AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
	private final String miraculous;

	public MiraculousArmorItem(String miraculous, Type type, Properties pProperties) {
		super(MiraculousArmorMaterials.MIRACULOUS, type, pProperties);
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
}
