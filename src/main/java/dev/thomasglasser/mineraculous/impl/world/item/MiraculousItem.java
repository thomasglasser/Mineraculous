package dev.thomasglasser.mineraculous.impl.world.item;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.MiraculousItemRenderer;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class MiraculousItem extends Item implements ICurioItem, GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MiraculousItem(Properties properties) {
        super(properties.stacksTo(1)
                .component(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                .fireResistant());
        GeckoLibUtil.registerSyncedAnimatable(this);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Miraculous.formatItemName(stack, super.getName(stack));
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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level instanceof ServerLevel serverLevel) {
            UUID owner = stack.get(MineraculousDataComponents.OWNER);
            if (owner == null || !owner.equals(entity.getUUID())) {
                stack.set(MineraculousDataComponents.OWNER, entity.getUUID());
                UUID kwamiId = stack.get(MineraculousDataComponents.KWAMI_ID);
                if (kwamiId != null) {
                    if (serverLevel.getEntity(kwamiId) instanceof Kwami kwami) {
                        if (entity instanceof Player player) {
                            kwami.tame(player);
                        } else {
                            kwami.setTame(true, true);
                            kwami.setOwnerUUID(entity.getUUID());
                        }
                    } else if (!stack.has(MineraculousDataComponents.POWERED)) {
                        stack.remove(MineraculousDataComponents.KWAMI_ID);
                    }
                }
            }
            if (!stack.has(MineraculousDataComponents.POWERED) && !stack.has(MineraculousDataComponents.KWAMI_ID)) {
                stack.set(MineraculousDataComponents.POWERED, Unit.INSTANCE);
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        if (entity.level() instanceof ServerLevel level && miraculous != null && !(stack.is(prevStack.getItem()) && miraculous == prevStack.get(MineraculousDataComponents.MIRACULOUS))) {
            MiraculousData data = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous);
            if (!data.transformed()) {
                UUID miraculousId = stack.get(MineraculousDataComponents.MIRACULOUS_ID);
                if (miraculousId == null) {
                    miraculousId = UUID.randomUUID();
                    stack.set(MineraculousDataComponents.MIRACULOUS_ID, miraculousId);
                }
                if (stack.has(MineraculousDataComponents.POWERED)) {
                    stack.remove(MineraculousDataComponents.POWERED);
                    Kwami kwami = MineraculousEntityUtils.summonKwami(stack.getOrDefault(MineraculousDataComponents.CHARGED, true), miraculousId, level, miraculous, entity);
                    if (kwami != null) {
                        stack.set(MineraculousDataComponents.KWAMI_ID, kwami.getUUID());
                    } else {
                        Mineraculous.LOGGER.error("Kwami could not be created for entity {}", entity.getName().plainCopy().getString());
                    }
                }
                data.equip(new CuriosData(slotContext)).save(miraculous, entity, true);
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        if (miraculous != null && entity.level() instanceof ServerLevel level && !(stack.is(newStack.getItem()) && miraculous == newStack.get(MineraculousDataComponents.MIRACULOUS))) {
            MiraculousData data = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous);
            if (data.transformed()) {
                data.detransform(entity, level, miraculous, true);
            } else {
                data.unequip().save(miraculous, entity, true);
            }
        }
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private MiraculousItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new MiraculousItemRenderer();

                return this.renderer;
            }
        });
    }
}
