package dev.thomasglasser.mineraculous.impl.world.item;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItemUtils;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.MiraculousItemRenderer;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class FakeMiraculousItem extends Item implements ICurioItem, GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public FakeMiraculousItem(Properties properties) {
        super(properties.stacksTo(1)
                .component(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE));
        GeckoLibUtil.registerSyncedAnimatable(this);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level instanceof ServerLevel) {
            if (!stack.has(MineraculousDataComponents.MIRACULOUS)) {
                stack.set(MineraculousDataComponents.MIRACULOUS, level.registryAccess().registryOrThrow(MineraculousRegistries.MIRACULOUS).getAny().orElse(null));
            }
        }
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
    public List<Component> getSlotsTooltip(List<Component> tooltips, TooltipContext context, ItemStack stack) {
        MutableComponent slotsTooltip = Component.translatable("curios.tooltip.slot").append(" ").withStyle(ChatFormatting.GOLD);
        Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        if (miraculous != null) {
            slotsTooltip.append(Component.translatable("curios.identifier." + miraculous.value().acceptableSlot()).withStyle(ChatFormatting.YELLOW));
        }
        List<Component> newTooltips = new ReferenceArrayList<>(tooltips);
        newTooltips.removeLast();
        newTooltips.add(slotsTooltip);
        return newTooltips;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return canEquip(slotContext, stack);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        return miraculous != null && slotContext.identifier().equals(miraculous.value().acceptableSlot());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, 0, state -> MineraculousItemUtils.genericOptionalController(state, this, false)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private MiraculousItemRenderer<?> renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new MiraculousItemRenderer<>();

                return this.renderer;
            }
        });
    }
}
