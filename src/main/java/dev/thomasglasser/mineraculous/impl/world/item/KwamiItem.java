package dev.thomasglasser.mineraculous.impl.world.item;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityUtils;
import dev.thomasglasser.mineraculous.api.world.inventory.tooltip.LabeledItemTagsTooltip;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.KwamiItemRenderer;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import dev.thomasglasser.mineraculous.impl.world.item.component.KwamiFoods;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class KwamiItem extends Item implements GeoItem {
    public static final Component CHARGED = Component.translatable("item.mineraculous.kwami.charged").withStyle(ChatFormatting.GREEN);
    public static final Component NOT_CHARGED = Component.translatable("item.mineraculous.kwami.not_charged").withStyle(ChatFormatting.RED);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public KwamiItem(Properties properties) {
        super(properties);
        GeckoLibUtil.registerSyncedAnimatable(this);
    }

    public static ItemStack create(Kwami kwami) {
        ItemStack stack = MineraculousItems.KWAMI.toStack();
        stack.set(MineraculousDataComponents.KWAMI_ID, kwami.getUUID());
        stack.set(MineraculousDataComponents.KWAMI_FOODS, new KwamiFoods(kwami));
        stack.set(MineraculousDataComponents.CHARGED, kwami.isCharged());
        stack.set(MineraculousDataComponents.MIRACULOUS, kwami.getMiraculous());
        stack.set(MineraculousDataComponents.MIRACULOUS_ID, kwami.getMiraculousId());
        stack.set(MineraculousDataComponents.OWNER, kwami.getOwnerUUID());
        return stack;
    }

    public static InteractionResultHolder<ItemStack> summonKwami(ItemStack stack, LivingEntity summoner) {
        Level level = summoner.level();
        if (summoner.getUUID().equals(stack.get(MineraculousDataComponents.OWNER))) {
            boolean charged = stack.getOrDefault(MineraculousDataComponents.CHARGED, true);
            UUID miraculousId = stack.get(MineraculousDataComponents.MIRACULOUS_ID);
            Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
            if (miraculousId != null && miraculous != null) {
                if (!level.isClientSide()) {
                    Kwami kwami = MineraculousEntityUtils.summonKwami(summoner, charged, miraculousId, miraculous, false, stack.get(MineraculousDataComponents.KWAMI_ID));
                    if (kwami != null) {
                        stack.setCount(0);
                    } else {
                        MineraculousConstants.LOGGER.error("Kwami could not be created for KwamiItem {}", stack);
                    }
                }
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        return summonKwami(player.getItemInHand(usedHand), player);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Miraculous.formatItemName(stack, super.getName(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(1, stack.getOrDefault(MineraculousDataComponents.CHARGED, true) ? CHARGED : NOT_CHARGED);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.ofNullable(stack.get(MineraculousDataComponents.KWAMI_FOODS)).map(LabeledItemTagsTooltip::new);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (!level.isClientSide() && !stack.has(MineraculousDataComponents.MIRACULOUS)) {
            stack.set(MineraculousDataComponents.MIRACULOUS, level.registryAccess().registryOrThrow(MineraculousRegistries.MIRACULOUS).getAny().orElse(null));
        }

        // Inventory Interaction
        if (entity.tickCount % SharedConstants.TICKS_PER_SECOND == 0 && entity instanceof ServerPlayer player) {
            boolean interact = (MineraculousServerConfig.get().kwamiItemInventoryInteractionChance.getAsDouble() / 100.0) >= level.random.nextDouble();
            if (interact) {
                KwamiFoods kwamiFoods = stack.get(MineraculousDataComponents.KWAMI_FOODS);
                if (MineraculousServerConfig.get().enableKwamiItemCharging.getAsBoolean() && !stack.getOrDefault(MineraculousDataComponents.CHARGED, true) && kwamiFoods != null) {
                    // Eat and charge
                    for (ItemStack s : MineraculousEntityUtils.getInventoryAndCurios(entity)) {
                        if (s.is(kwamiFoods.foods()) || s.is(kwamiFoods.treats())) {
                            if (s.is(kwamiFoods.treats()) || (s.is(kwamiFoods.foods()) && level.random.nextInt(3) == 0)) {
                                stack.set(MineraculousDataComponents.CHARGED, true);
                            }
                            s.shrink(1);
                            break;
                        }
                    }
                } else {
                    if (MineraculousServerConfig.get().enableKwamiItemMoving.getAsBoolean() && level.random.nextBoolean()) {
                        // Move to random empty spot
                        int current = player.getInventory().items.indexOf(stack);
                        IntList emptySlots = new IntArrayList();
                        for (int i = 0; i < player.getInventory().items.size(); i++) {
                            if (i != current && player.getInventory().items.get(i).isEmpty()) {
                                emptySlots.add(i);
                            }
                        }
                        int slot = emptySlots.getInt(level.random.nextInt(emptySlots.size()));
                        player.getInventory().items.set(slot, stack.copy());
                        stack.setCount(0);
                    } else if (MineraculousServerConfig.get().enableKwamiItemSwapping.getAsBoolean() && level.random.nextBoolean()) {
                        // Move to random filled spot and swap item
                        int current = player.getInventory().items.indexOf(stack);
                        IntList filledSlots = new IntArrayList();
                        for (int i = 0; i < player.getInventory().items.size(); i++) {
                            if (i != current && !player.getInventory().items.get(i).isEmpty()) {
                                filledSlots.add(i);
                            }
                        }
                        int slot = filledSlots.getInt(level.random.nextInt(filledSlots.size()));
                        ItemStack other = player.getInventory().items.get(slot);
                        player.getInventory().items.set(slot, stack.copy());
                        player.getInventory().items.set(current, other.copy());
                        stack.setCount(0);
                        other.setCount(0);
                    }
                }
            }
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        summonKwami(item, player);
        return true;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private KwamiItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new KwamiItemRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
