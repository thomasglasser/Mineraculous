package dev.thomasglasser.mineraculous.world.item.armor;

import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.renderer.armor.MiraculousArmorItemRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.tommylib.api.client.renderer.BewlrProvider;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import dev.thomasglasser.tommylib.api.world.item.armor.BaseGeoArmorItem;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Unbreakable;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MiraculousArmorItem extends BaseGeoArmorItem implements ModeledItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MiraculousArmorItem(Type type, Properties pProperties) {
        super(MineraculousArmorMaterials.MIRACULOUS, type, pProperties
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(false)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (stack.has(MineraculousDataComponents.MIRACULOUS))
            tooltipComponents.add(Component.translatable(stack.get(MineraculousDataComponents.MIRACULOUS).location().toLanguageKey(stack.get(MineraculousDataComponents.MIRACULOUS).registry().getPath())).withStyle(ChatFormatting.GRAY));
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
                    this.renderer = new MiraculousArmorItemRenderer();

                return this.renderer;
            }
        });
    }

    @Override
    public void createBewlrProvider(Consumer<BewlrProvider> provider) {
        provider.accept(new BewlrProvider() {
            @Override
            public BlockEntityWithoutLevelRenderer getBewlr() {
                return MineraculousClientUtils.getBewlr();
            }
        });
    }
}
