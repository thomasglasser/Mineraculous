package dev.thomasglasser.mineraculous.mixin.miraculous.world.item;

import com.google.common.collect.Multimap;
import dev.thomasglasser.mineraculous.client.renderer.item.CatStaffRenderer;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.tommylib.api.world.item.FabricGeoItem;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(CatStaffItem.class)
public abstract class CatStaffItemMixin implements FabricGeoItem, FabricItem
{
	@Unique
	Supplier<Object> renderProvider = GeoItem.makeRenderer((CatStaffItem)(Object)this);

	@Override
	public Supplier<Object> getRenderProvider() {
		return renderProvider;
	}

	@Override
	public void createRenderer(Consumer<Object> consumer) {
		consumer.accept(new RenderProvider() {
			private CatStaffRenderer renderer;

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new CatStaffRenderer();

				return this.renderer;
			}
		});
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot)
	{
		return ((CatStaffItem)(Object)this).getAttributeModifiers(slot, stack);
	}
}
