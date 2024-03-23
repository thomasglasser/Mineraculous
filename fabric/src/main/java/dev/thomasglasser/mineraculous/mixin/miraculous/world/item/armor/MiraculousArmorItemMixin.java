package dev.thomasglasser.mineraculous.mixin.miraculous.world.item.armor;

import dev.thomasglasser.mineraculous.client.renderer.armor.MiraculousArmorItemRenderer;
import dev.thomasglasser.mineraculous.world.item.armor.MiraculousArmorItem;
import dev.thomasglasser.tommylib.api.world.item.FabricGeoItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(MiraculousArmorItem.class)
public abstract class MiraculousArmorItemMixin implements FabricGeoItem
{
    @Unique
    Supplier<Object> renderProvider = GeoItem.makeRenderer((MiraculousArmorItem)(Object)this);

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private GeoArmorRenderer<?> renderer;

            @Override
            public @NotNull HumanoidModel<LivingEntity> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<LivingEntity> original) {
                if (this.renderer == null)
                    this.renderer = new MiraculousArmorItemRenderer(((MiraculousArmorItem)(Object)MiraculousArmorItemMixin.this).getMiraculousName());

                // This prepares our GeoArmorRenderer for the current render frame.
                // These parameters may be null however, so we don't do anything further with them
                this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);

                return this.renderer;
            }
        });
    }
}
