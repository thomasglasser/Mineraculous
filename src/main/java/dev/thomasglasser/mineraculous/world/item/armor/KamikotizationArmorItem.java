package dev.thomasglasser.mineraculous.world.item.armor;

import dev.thomasglasser.mineraculous.client.renderer.armor.KamikotizationArmorItemRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.entity.kamikotization.Kamikotization;
import dev.thomasglasser.tommylib.api.world.item.armor.GeoArmorItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Unbreakable;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtil;

public class KamikotizationArmorItem extends ArmorItem implements GeoArmorItem {
    protected static final Map<KamikotizationArmorItem, ArmorAnimationData> ANIMATION_DATA = new Object2ObjectOpenHashMap<>();
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

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            Entity entity = state.getData(DataTickets.ENTITY);
            boolean flying = entity instanceof Player player ? player.getAbilities().flying : entity instanceof FlyingAnimal flyingAnimal ? flyingAnimal.isFlying() : entity instanceof FlyingMob || entity.isFlapping();
            boolean swimming = entity.isSwimming();
            boolean sprinting = entity.isSprinting();
            boolean walking = state.isMoving();
            GeoModel<KamikotizationArmorItem> model = (GeoModel<KamikotizationArmorItem>) RenderUtil.getGeoModelForArmor(state.getData(DataTickets.ITEMSTACK));
            if (model != null) {
                ArmorAnimationData data = ANIMATION_DATA.computeIfAbsent(this, item -> new ArmorAnimationData(model, this));
                if (flying && data.flying())
                    return state.setAndContinue(DefaultAnimations.FLY);
                else if (swimming && data.swimming())
                    return state.setAndContinue(DefaultAnimations.SWIM);
                else if (sprinting && data.sprinting())
                    return state.setAndContinue(DefaultAnimations.RUN);
                else if (walking && data.walking())
                    return state.setAndContinue(DefaultAnimations.WALK);
                else if (data.idle())
                    return state.setAndContinue(DefaultAnimations.IDLE);
            }
            return PlayState.STOP;
        }));
    }

    public static void clearAnimationData() {
        ANIMATION_DATA.clear();
    }
}
