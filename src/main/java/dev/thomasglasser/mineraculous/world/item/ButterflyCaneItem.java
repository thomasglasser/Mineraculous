package dev.thomasglasser.mineraculous.world.item;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.client.MineraculousClientEvents;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.client.renderer.item.ButterflyCaneRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundSetButterflyCaneAbilityPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetButterflyCaneIsCoveredPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetButterflyCaneIsOpenPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetButterflyCaneShouldCloseImpulsePayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetButterflyCaneShouldCoverImpulsePayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetButterflyCaneShouldUncoverImpulsePayload;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownButterflyCane;
import dev.thomasglasser.tommylib.api.client.renderer.BewlrProvider;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ButterflyCaneItem extends SwordItem implements GeoItem, ModeledItem, ProjectileItem {
    public static final RawAnimation OPEN = RawAnimation.begin().thenPlay("animation.cane.openingLid");
    public static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("animation.cane.closingLid");
    public static final RawAnimation COVER = RawAnimation.begin().thenPlay("animation.cane.covering");
    public static final RawAnimation UNCOVER = RawAnimation.begin().thenPlay("animation.cane.uncovering");
    public static final RawAnimation BLOCK = RawAnimation.begin().thenPlay("animation.cane.spin");

    protected ButterflyCaneItem(Properties properties) {
        super(MineraculousTiers.MIRACULOUS, properties.component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                .component(MineraculousDataComponents.BUTTERFLY_CANE_OPEN_IMPULSE, true)
                .component(MineraculousDataComponents.BUTTERFLY_CANE_CLOSE_IMPULSE, false)
                .component(MineraculousDataComponents.BUTTERFLY_CANE_COVER_IMPULSE, false)
                .component(MineraculousDataComponents.BUTTERFLY_CANE_UNCOVER_IMPULSE, true)

                .component(MineraculousDataComponents.BUTTERFLY_CANE_IS_COVERED, true)
                .component(MineraculousDataComponents.BUTTERFLY_CANE_IS_OPEN, false)
                .component(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY, Ability.BLOCK));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GeoAnimatable>(this, "use_controller", state -> PlayState.CONTINUE)
                .triggerableAnim("open", OPEN)
                .triggerableAnim("close", CLOSE)
                .triggerableAnim("idle", DefaultAnimations.IDLE)
                .triggerableAnim("uncover", UNCOVER)
                .triggerableAnim("cover", COVER));

        controllers.add(new AnimationController<GeoAnimatable>(this, "shield_controller", state -> PlayState.CONTINUE)
                .triggerableAnim("use", DefaultAnimations.ATTACK_BLOCK)
                .triggerableAnim("spin", BLOCK));
    }

    @Override
    public void createBewlrProvider(Consumer<BewlrProvider> provider) {
        provider.accept(new BewlrProvider() {
            private BlockEntityWithoutLevelRenderer bewlr;

            @Override
            public BlockEntityWithoutLevelRenderer getBewlr() {
                if (bewlr == null) bewlr = new ButterflyCaneRenderer();
                return bewlr;
            }
        });
    }

    @Override
    public Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        return new ThrownButterflyCane(position.x(), position.y(), position.z(), level, itemStack, itemStack);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (entity.level() instanceof ServerLevel serverLevel && stack.has(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get())) {
            long animId = GeoItem.getOrAssignId(stack, serverLevel);
            //TODO: Change when gecko fixed
            if (stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get()) == Ability.BLOCK) {
                stopTriggeredAnim(entity, animId, "shield_controller", "use");
                stopTriggeredAnim(entity, animId, "shield_controller", "spin");
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotID, boolean isSelected) {
        if (entity instanceof Player player && !player.isUsingItem()) {
            if (player.getMainHandItem() == stack || player.getOffhandItem() == stack) {

                Ability ability = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY);
                Boolean isCovered = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_IS_COVERED);
                Boolean isOpened = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_IS_OPEN);

                Boolean shouldOpenImpulse = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_OPEN_IMPULSE);
                Boolean shouldCloseImpulse = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_CLOSE_IMPULSE);
                Boolean shouldCoverImpulse = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_COVER_IMPULSE);
                Boolean shouldUncoverImpulse = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_UNCOVER_IMPULSE);

                if (level instanceof ServerLevel serverLevel) { //ANIMATIONS :
                    long animId = GeoItem.getOrAssignId(stack, serverLevel);
                    if (isOpened && ability == Ability.OPEN) {
                        if (shouldOpenImpulse) {
                            triggerAnim(entity, animId, "use_controller", "open");
                        }
                        stack.set(MineraculousDataComponents.BUTTERFLY_CANE_OPEN_IMPULSE, false);
                    } else {
                        stack.set(MineraculousDataComponents.BUTTERFLY_CANE_OPEN_IMPULSE, true);
                    }
                    if (shouldCloseImpulse) {
                        triggerAnim(entity, animId, "use_controller", "close");
                        stack.set(MineraculousDataComponents.BUTTERFLY_CANE_CLOSE_IMPULSE, false);
                    }
                    if (shouldCoverImpulse) {
                        triggerAnim(entity, animId, "use_controller", "cover");
                        stack.set(MineraculousDataComponents.BUTTERFLY_CANE_COVER_IMPULSE, false);
                    }
                    if (shouldUncoverImpulse) {
                        triggerAnim(entity, animId, "use_controller", "uncover");
                        stack.set(MineraculousDataComponents.BUTTERFLY_CANE_UNCOVER_IMPULSE, false);
                    }
                } else {

                    InteractionHand hand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                    CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(entity);
                    int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAITTICKS);

                    if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen()) {
                        if (isCovered != null &&
                                isOpened != null &&
                                ability != null) {

                            if (MineraculousKeyMappings.OPEN_TOOL_WHEEL.get().isDown() && !isOpened) {

                                ArrayList<RadialMenuOption> radialMenuOptions = new ArrayList<>();
                                if (isCovered) {
                                    radialMenuOptions.add(Ability.BLOCK);
                                    radialMenuOptions.add(Ability.THROW);
                                    radialMenuOptions.add(Ability.UNCOVER);
                                    radialMenuOptions.add(Ability.OPEN);
                                } else {
                                    radialMenuOptions.add(Ability.BLOCK);
                                    radialMenuOptions.add(Ability.THROW);
                                    radialMenuOptions.add(Ability.COVER);
                                }

                                MineraculousClientEvents.openToolWheel(MineraculousMiraculous.BUTTERFLY, stack, radialMenuOption -> {
                                    if (radialMenuOption instanceof ButterflyCaneItem.Ability a) {
                                        switch (a) {
                                            case OPEN -> {
                                                stack.set(MineraculousDataComponents.BUTTERFLY_CANE_IS_OPEN.get(), true);
                                            }
                                            case COVER -> {
                                                stack.set(MineraculousDataComponents.BUTTERFLY_CANE_IS_COVERED.get(), true);
                                                stack.set(MineraculousDataComponents.BUTTERFLY_CANE_COVER_IMPULSE, true);
                                                TommyLibServices.NETWORK.sendToServer(new ServerboundSetButterflyCaneShouldCoverImpulsePayload(player.getInventory().findSlotMatchingItem(stack), true));
                                            }
                                            case UNCOVER -> {
                                                stack.set(MineraculousDataComponents.BUTTERFLY_CANE_IS_COVERED.get(), false);
                                                stack.set(MineraculousDataComponents.BUTTERFLY_CANE_UNCOVER_IMPULSE, true);
                                                TommyLibServices.NETWORK.sendToServer(new ServerboundSetButterflyCaneShouldUncoverImpulsePayload(player.getInventory().findSlotMatchingItem(stack), true));
                                            }
                                        }
                                        stack.set(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get(), a);
                                        TommyLibServices.NETWORK.sendToServer(new ServerboundSetButterflyCaneIsOpenPayload(player.getInventory().findSlotMatchingItem(stack), Boolean.TRUE.equals(stack.get(MineraculousDataComponents.BUTTERFLY_CANE_IS_OPEN))));
                                        TommyLibServices.NETWORK.sendToServer(new ServerboundSetButterflyCaneIsCoveredPayload(player.getInventory().findSlotMatchingItem(stack), Boolean.TRUE.equals(stack.get(MineraculousDataComponents.BUTTERFLY_CANE_IS_COVERED))));
                                        TommyLibServices.NETWORK.sendToServer(new ServerboundSetButterflyCaneAbilityPayload(player.getInventory().findSlotMatchingItem(stack), a.name()));
                                    }
                                }, radialMenuOptions.toArray(new RadialMenuOption[] {}));

                            }

                            if (isOpened && MineraculousClientUtils.isKeyReleased(MineraculousKeyMappings.OPEN_TOOL_WHEEL.get())) {
                                stack.set(MineraculousDataComponents.BUTTERFLY_CANE_IS_OPEN.get(), false);
                                stack.set(MineraculousDataComponents.BUTTERFLY_CANE_CLOSE_IMPULSE.get(), true);
                                TommyLibServices.NETWORK.sendToServer(new ServerboundSetButterflyCaneIsOpenPayload(player.getInventory().findSlotMatchingItem(stack), false));
                                TommyLibServices.NETWORK.sendToServer(new ServerboundSetButterflyCaneShouldCloseImpulsePayload(player.getInventory().findSlotMatchingItem(stack), true));
                            }

                            TommyLibServices.ENTITY.setPersistentData(entity, playerData, false);
                        }
                    }
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.has(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get())) {
            Ability ability = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get());
            if (ability == Ability.BLOCK || ability == Ability.THROW || ability == Ability.OPEN) {
                player.startUsingItem(hand);
            }
            if (level instanceof ServerLevel serverLevel) {
                long animId = GeoItem.getOrAssignId(stack, serverLevel);
                switch (ability) {
                    case BLOCK -> {
                        triggerAnim(player, animId, "shield_controller", "use");
                        if (cache.getManagerForId(animId).getAnimationControllers().get("shield_controller").getCurrentRawAnimation() != BLOCK) {
                            triggerAnim(player, animId, "shield_controller", "spin");
                        }
                    }
                    case OPEN -> {
                        triggerAnim(player, animId, "shield_controller", "use");
                    }
                    case null, default -> {}
                }
            }
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        ButterflyCaneItem.Ability ability = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get());
        switch (ability) {
            case BLOCK -> {
                return UseAnim.BLOCK;
            }
            case OPEN -> {
                if (stack.get(MineraculousDataComponents.BUTTERFLY_CANE_IS_OPEN.get())) {
                    return UseAnim.BLOCK;
                } else {
                    return UseAnim.NONE;
                }
            }
            case THROW -> {
                return UseAnim.SPEAR;
            }
            case null, default -> {
                return UseAnim.NONE;
            }
        }
    }

    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        ButterflyCaneItem.Ability ability = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get());
        if (livingEntity instanceof Player player) {
            if (ability == Ability.THROW) {
                int i = this.getUseDuration(stack, livingEntity) - timeLeft;
                if (i >= 10) {
                    if (!level.isClientSide) {
                        ThrownButterflyCane thrown = new ThrownButterflyCane(livingEntity, level, stack, stack, stack.get(MineraculousDataComponents.BUTTERFLY_CANE_IS_COVERED));
                        thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(livingEntity.getUsedItemHand()));
                        if (player.hasInfiniteMaterials()) {
                            thrown.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                        }

                        level.addFreshEntity(thrown);
                        // TODO: Custom sound
                        level.playSound(null, thrown, SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                        if (!player.hasInfiniteMaterials()) {
                            player.getInventory().removeItem(stack);
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        ButterflyCaneItem.Ability ability = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get());
        return switch (ability) {
            case BLOCK -> itemAbility == ItemAbilities.SHIELD_BLOCK;
            case null, default -> false;
        };
    }

    public enum Ability implements RadialMenuOption {
        OPEN,
        COVER,
        UNCOVER,
        THROW,
        BLOCK;

        public static final Codec<Ability> CODEC = Codec.STRING.xmap(Ability::valueOf, Ability::name);

        public static final StreamCodec<ByteBuf, ButterflyCaneItem.Ability> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(ButterflyCaneItem.Ability::valueOf, ButterflyCaneItem.Ability::name);

        private final String translationKey;

        Ability() {
            this.translationKey = MineraculousItems.BUTTERFLY_CANE.getId().toLanguageKey("ability", name().toLowerCase());
        }

        @Override
        public String translationKey() {
            return translationKey;
        }
    }
}
