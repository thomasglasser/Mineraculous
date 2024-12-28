package dev.thomasglasser.mineraculous.world.item;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientEvents;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.client.renderer.item.LadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundActivateToolPayload;
import dev.thomasglasser.mineraculous.network.ServerboundEquipToolPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetLadybugYoyoAbilityPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.tommylib.api.client.renderer.BewlrProvider;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class LadybugYoyoItem extends Item implements ModeledItem, GeoItem, ICurioItem {
    public static final ResourceLocation EXTENDED_PROPERTY_ID = Mineraculous.modLoc("extended");
    public static final String CONTROLLER_USE = "use_controller";
    public static final String ANIMATION_BLOCK = "block";
    public static final String ANIMATION_EXTEND = "extend";
    public static final String ANIMATION_RETRACT = "retract";
    public static final String CONTROLLER_OPEN = "open_controller";
    public static final String ANIMATION_OPEN = "open";
    public static final String ANIMATION_CLOSE = "close";

    private static final RawAnimation EXTEND = RawAnimation.begin().thenPlay("misc.extend");
    private static final RawAnimation RETRACT = RawAnimation.begin().thenPlay("misc.retract");
    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("attack.open");
    private static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("attack.close");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public LadybugYoyoItem(Properties properties) {
        super(properties
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true)));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER_USE, state -> {
            ItemStack stack = state.getData(DataTickets.ITEMSTACK);
            if (!stack.has(MineraculousDataComponents.POWERED.get()) && !state.isCurrentAnimation(RETRACT))
                return state.setAndContinue(DefaultAnimations.IDLE);
            return PlayState.STOP;
        })
                .triggerableAnim(ANIMATION_BLOCK, DefaultAnimations.ATTACK_BLOCK)
                .triggerableAnim(ANIMATION_EXTEND, EXTEND)
                .triggerableAnim(ANIMATION_RETRACT, RETRACT));
        controllers.add(new AnimationController<>(this, CONTROLLER_OPEN, state -> PlayState.CONTINUE)
                .triggerableAnim(ANIMATION_OPEN, OPEN)
                .triggerableAnim(ANIMATION_CLOSE, CLOSE));
    }

    @Override
    public void createBewlrProvider(Consumer<BewlrProvider> provider) {
        provider.accept(new BewlrProvider() {
            private BlockEntityWithoutLevelRenderer bewlr;

            @Override
            public BlockEntityWithoutLevelRenderer getBewlr() {
                if (bewlr == null) bewlr = new LadybugYoyoRenderer();
                return bewlr;
            }
        });
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pEntity instanceof Player player && !player.isUsingItem()) {
            if (pLevel.isClientSide() && player.getMainHandItem() == pStack || player.getOffhandItem() == pStack) {
                InteractionHand hand = player.getMainHandItem() == pStack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

                CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(pEntity);
                int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAITTICKS);
                if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen()) {
                    if (MineraculousKeyMappings.ACTIVATE_TOOL.get().isDown()) {
                        boolean activate = !pStack.has(MineraculousDataComponents.POWERED.get());
                        if (activate) {
                            pStack.set(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE);
                        } else {
                            pStack.remove(MineraculousDataComponents.POWERED.get());
                        }
                        TommyLibServices.NETWORK.sendToServer(new ServerboundActivateToolPayload(activate, hand, CONTROLLER_USE, activate ? ANIMATION_EXTEND : ANIMATION_RETRACT));
                        playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
                    } else if (MineraculousKeyMappings.OPEN_TOOL_WHEEL.get().isDown()) {
                        if (pStack.has(MineraculousDataComponents.POWERED.get())) {
                            MineraculousClientEvents.openToolWheel(MineraculousMiraculous.LADYBUG, pStack, option -> {
                                if (option instanceof Ability ability) {
                                    pStack.set(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get(), ability);
                                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetLadybugYoyoAbilityPayload(player.getInventory().findSlotMatchingItem(pStack), ability.name()));
                                }
                            }, Arrays.stream(Ability.values()).filter(ability -> ability.canBePerformedBy(player, pStack)).toArray(Ability[]::new));
                        } else {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundEquipToolPayload(hand));
                        }
                        playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
                    }
                }
                TommyLibServices.ENTITY.setPersistentData(pEntity, playerData, false);
            }
        }

        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (!stack.has(MineraculousDataComponents.POWERED.get()))
            return InteractionResultHolder.fail(stack);
        Ability ability = stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get());
        if (ability != null) {
            if (!ability.canBePerformedBy(pPlayer, stack))
                return InteractionResultHolder.fail(stack);
            // TODO: Implement abilities
            if (ability == Ability.BLOCK)
                pPlayer.startUsingItem(pHand);
//            if (ability == Ability.BLOCK || ability == Ability.THROW)
//                pPlayer.startUsingItem(pHand);
//            else if (ability == Ability.TRAVEL) {
//                pPlayer.setDeltaMovement(pPlayer.getLookAngle().scale(4));
//                pPlayer.getCooldowns().addCooldown(stack.getItem(), 20);
//            } else if (ability == Ability.PERCH) {
//                if (pPlayer.getNearestViewDirection() == Direction.UP)
//                    pPlayer.setDeltaMovement(new Vec3(0, 0.5, 0));
//                else if (pPlayer.getNearestViewDirection() == Direction.DOWN) {
//                    pPlayer.setDeltaMovement(new Vec3(0, -0.5, 0));
//                    pPlayer.resetFallDistance();
//                }
//            }
            if (pLevel instanceof ServerLevel serverLevel) {
                long animId = GeoItem.getOrAssignId(stack, serverLevel);
                switch (ability) {
                    case BLOCK -> triggerAnim(pPlayer, animId, CONTROLLER_USE, ANIMATION_BLOCK);
                    default -> {}
                }
            }
            return InteractionResultHolder.consume(stack);
        }
        return super.use(pLevel, pPlayer, pHand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        if (stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get()) == Ability.BLOCK && remainingUseDuration % 10 == 0) {
            // TODO: Play ladybug yoyo sound
//            livingEntity.playSound(MineraculousSoundEvents.CAT_STAFF_SHIELD.get());
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            long animId = GeoItem.getOrAssignId(stack, serverLevel);
            if (stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get()) == Ability.BLOCK) {
                stopTriggeredAnim(entity, animId, CONTROLLER_USE, ANIMATION_BLOCK);
            }
        }
    }

    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        Ability ability = stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get());
        // TODO: Implement ability
//        if (entityLiving instanceof Player player && ability == Ability.THROW) {
//            int i = this.getUseDuration(stack, entityLiving) - timeLeft;
//            if (i >= 10) {
//                if (!level.isClientSide) {
//                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(entityLiving.getUsedItemHand()));
//                    ThrownCatStaff thrown = new ThrownCatStaff(entityLiving, level, stack, stack);
//                    thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
//                    if (player.hasInfiniteMaterials()) {
//                        thrown.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
//                    }
//
//                    level.addFreshEntity(thrown);
//                    // TODO: Custom sound
//                    level.playSound(null, thrown, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
//                    if (!player.hasInfiniteMaterials()) {
//                        player.getInventory().removeItem(stack);
//                    }
//                }
//
//                player.awardStat(Stats.ITEM_USED.get(this));
//            }
//        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        Ability ability = stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get());
        return switch (ability) {
            case BLOCK -> UseAnim.BLOCK;
            case null, default -> UseAnim.NONE;
        };
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        Ability ability = stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get());
        return switch (ability) {
            case BLOCK -> itemAbility == ItemAbilities.SHIELD_BLOCK;
            case null, default -> false;
        };
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return canEquip(slotContext, stack);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return canEquip(stack);
    }

    public boolean canEquip(ItemStack stack) {
        return !stack.has(MineraculousDataComponents.POWERED);
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, TooltipContext context, ItemStack stack) {
        if (canEquip(stack)) {
            return ICurioItem.super.getSlotsTooltip(tooltips, context, stack);
        }
        return List.of();
    }

    public enum Ability implements RadialMenuOption {
        BLOCK,
        TRAVEL,
        BIND,
        LASSO,
        KAMIKO_CAPTURE(true),
        KAMIKO_RELEASE(true);

        public static final Codec<Ability> CODEC = Codec.STRING.xmap(Ability::valueOf, Ability::name);
        public static final StreamCodec<ByteBuf, Ability> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Ability::valueOf, Ability::name);

        private final String translationKey;
        private final boolean requiresTransformed;

        Ability(boolean requiresTransformed) {
            this.translationKey = MineraculousItems.LADYBUG_YOYO.getId().toLanguageKey("ability", name().toLowerCase());
            this.requiresTransformed = requiresTransformed;
        }

        Ability() {
            this(false);
        }

        @Override
        public String translationKey() {
            return translationKey;
        }

        public boolean canBePerformedBy(Player player, ItemStack stack) {
            if (requiresTransformed) {
                KwamiData kwamiData = stack.get(MineraculousDataComponents.KWAMI_DATA.get());
                KwamiData playerKwamiData = player.getData(MineraculousAttachmentTypes.MIRACULOUS).get(MineraculousMiraculous.LADYBUG).miraculousItem().get(MineraculousDataComponents.KWAMI_DATA.get());
                return kwamiData != null && playerKwamiData != null && kwamiData.uuid().equals(playerKwamiData.uuid());
            }
            return true;
        }
    }
}
