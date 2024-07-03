package dev.thomasglasser.mineraculous.world.item.armor;

import dev.thomasglasser.mineraculous.client.renderer.armor.MiraculousArmorItemRenderer;
import dev.thomasglasser.tommylib.api.world.item.armor.BaseGeoArmorItem;
import java.util.function.Consumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MiraculousArmorItem extends BaseGeoArmorItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String miraculous;

    public MiraculousArmorItem(String miraculous, Type type, Properties pProperties) {
        super(MineraculousArmorMaterials.MIRACULOUS, type, pProperties
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(false)));
        this.miraculous = miraculous;
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
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private MiraculousArmorItemRenderer renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack, @Nullable EquipmentSlot equipmentSlot, @Nullable HumanoidModel<T> original) {
                if (this.renderer == null)
                    this.renderer = new MiraculousArmorItemRenderer(miraculous);

                return this.renderer;
            }
        });
    }
}
