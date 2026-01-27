package dev.thomasglasser.mineraculous.impl.world.item.armor;

import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.api.world.item.armor.MineraculousArmorMaterials;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.renderer.armor.MiraculousArmorItemRenderer;
import dev.thomasglasser.tommylib.api.world.item.armor.GeoArmorItem;
import java.util.function.Consumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

public class FakeMiraculousArmorItem extends ArmorItem implements GeoArmorItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FakeMiraculousArmorItem(Type type, Properties pProperties) {
        super(MineraculousArmorMaterials.COSTUME, type, pProperties);
        GeckoLibUtil.registerSyncedAnimatable(this);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Miraculous.formatItemName(stack, super.getName(stack));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isSkintight() {
        return true;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, 0, state -> MineraculousItemUtils.genericOptionalController(state, this, true)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private MiraculousArmorItemRenderer<?> renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack, @Nullable EquipmentSlot equipmentSlot, @Nullable HumanoidModel<T> original) {
                if (this.renderer == null)
                    this.renderer = new MiraculousArmorItemRenderer<>();

                return this.renderer;
            }
        });
    }
}
