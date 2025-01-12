package dev.thomasglasser.mineraculous.world.item.armor;

import dev.thomasglasser.mineraculous.client.renderer.armor.KamikotizationArmorItemRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.tommylib.api.world.item.armor.GeoArmorItem;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Unbreakable;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

public class KamikotizationArmorItem extends ArmorItem implements GeoArmorItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public KamikotizationArmorItem(Type type, Properties pProperties) {
        super(MineraculousArmorMaterials.MIRACULOUS, type, pProperties
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(false)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ResourceKey<Kamikotization> kamikotizationKey = stack.get(MineraculousDataComponents.KAMIKOTIZATION);
        if (kamikotizationKey != null) {
            tooltipComponents.add(Component.translatable(kamikotizationKey.location().toLanguageKey(kamikotizationKey.registry().getPath())));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean isSkintight() {
        return true;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private KamikotizationArmorItemRenderer renderer;

            @Override
            public @Nullable <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack, @Nullable EquipmentSlot equipmentSlot, @Nullable HumanoidModel<T> original) {
                if (this.renderer == null)
                    this.renderer = new KamikotizationArmorItemRenderer();

                return this.renderer;
            }
        });
    }
}
