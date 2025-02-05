package dev.thomasglasser.mineraculous.world.item;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.MineraculousClientEvents;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.client.renderer.item.ButterflyCaneRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundSetButterflyCaneAbilityPayload;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.tags.MineraculousMiraculousTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownButterflyCane;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.client.renderer.BewlrProvider;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ResolvableProfile;
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

public class ButterflyCaneItem extends SwordItem implements GeoItem, ModeledItem, ProjectileItem {
    public static final ResourceLocation BLADE_PROPERTY_ID = Mineraculous.modLoc("blade");
    public static final ResourceLocation BASE_ENTITY_INTERACTION_RANGE_ID = ResourceLocation.withDefaultNamespace("base_entity_interaction_range");
    public static final String CONTROLLER_USE = "use_controller";
    public static final String ANIMATION_OPEN = "open";
    public static final String ANIMATION_CLOSE = "close";
    public static final String ANIMATION_SHEATHE = "sheathe";
    public static final String ANIMATION_UNSHEATHE = "unsheathe";
    public static final String ANIMATION_BLOCK = "block";
    public static final String TAG_STORED_KAMIKO = "StoredKamiko";

    public static final RawAnimation BLADE_IDLE = RawAnimation.begin().thenPlay("misc.blade_idle");

    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("misc.open");
    private static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("misc.close");
    private static final RawAnimation SHEATH = RawAnimation.begin().thenPlay("attack.sheathe");
    private static final RawAnimation UNSHEATHE = RawAnimation.begin().thenPlay("attack.unsheathe");

    private static final ItemAttributeModifiers BLADE = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 15, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, 0.5, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(BASE_ENTITY_INTERACTION_RANGE_ID, 2, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();
    private static final ItemAttributeModifiers COVERED = ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 8, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
            .build();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected ButterflyCaneItem(Properties properties) {
        super(MineraculousTiers.MIRACULOUS, properties
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true)));
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER_USE, state -> {
            if (state.getData(DataTickets.ITEMSTACK).get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY) == Ability.BLADE && !state.isCurrentAnimation(UNSHEATHE))
                return state.setAndContinue(BLADE_IDLE);
            return PlayState.STOP;
        })
                .triggerableAnim(ANIMATION_OPEN, OPEN)
                .triggerableAnim(ANIMATION_CLOSE, CLOSE)
                .triggerableAnim(ANIMATION_SHEATHE, SHEATH)
                .triggerableAnim(ANIMATION_UNSHEATHE, UNSHEATHE)
                .triggerableAnim(ANIMATION_BLOCK, DefaultAnimations.ATTACK_BLOCK));
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
        return new ThrownButterflyCane(position.x(), position.y(), position.z(), level, itemStack);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof Player player && !player.isUsingItem()) {
            if (level.isClientSide() && player.getMainHandItem() == stack || player.getOffhandItem() == stack) {
                CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(entity);
                int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAIT_TICKS);
                if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen() && MineraculousKeyMappings.OPEN_TOOL_WHEEL.get().isDown()) {
                    int color = level.holderOrThrow(MineraculousMiraculous.BUTTERFLY).value().color().getValue();
                    ResolvableProfile resolvableProfile = stack.get(DataComponents.PROFILE);
                    if (resolvableProfile != null) {
                        Player caneOwner = player.level().getPlayerByUUID(resolvableProfile.id().orElse(resolvableProfile.gameProfile().getId()));
                        if (caneOwner != null) {
                            ResourceKey<Miraculous> colorKey = caneOwner.getData(MineraculousAttachmentTypes.MIRACULOUS).getFirstKeyIn(MineraculousMiraculousTags.CAN_USE_BUTTERFLY_CANE, level);
                            if (colorKey != null)
                                color = level.holderOrThrow(colorKey).value().color().getValue();
                        }
                    }
                    MineraculousClientEvents.openToolWheel(color, stack, option -> {
                        if (option instanceof Ability ability) {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundSetButterflyCaneAbilityPayload(player.getInventory().findSlotMatchingItem(stack), ability.name()));
                            stack.set(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get(), ability);
                        }
                    }, Arrays.stream(Ability.values()).filter(ability -> {
                        if (ability == Ability.KAMIKO_STORE)
                            return stack.has(DataComponents.PROFILE);
                        return true;
                    }).toArray(Ability[]::new));
                    playerData.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, 10);
                    TommyLibServices.ENTITY.setPersistentData(entity, playerData, false);
                }
            }
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        ResolvableProfile resolvableProfile = stack.get(DataComponents.PROFILE);
        if (stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY) == Ability.KAMIKO_STORE && interactionTarget instanceof Kamiko kamiko && resolvableProfile != null) {
            Player caneOwner = player.level().getPlayerByUUID(resolvableProfile.id().orElse(resolvableProfile.gameProfile().getId()));
            if (caneOwner != null) {
                MiraculousDataSet miraculousDataSet = caneOwner.getData(MineraculousAttachmentTypes.MIRACULOUS);
                ResourceKey<Miraculous> storingKey = miraculousDataSet.getFirstKeyIn(MineraculousMiraculousTags.CAN_USE_BUTTERFLY_CANE, player.level());
                MiraculousData storingData = miraculousDataSet.get(storingKey);
                if (storingData != null && !storingData.extraData().contains(TAG_STORED_KAMIKO)) {
                    if (player.level() instanceof ServerLevel serverLevel) {
                        long animId = GeoItem.getOrAssignId(stack, serverLevel);
                        triggerAnim(player, animId, CONTROLLER_USE, ANIMATION_CLOSE);
                        storingData.extraData().put(TAG_STORED_KAMIKO, kamiko.saveWithoutId(new CompoundTag()));
                        miraculousDataSet.put(caneOwner, storingKey, storingData, true);
                    }
                    kamiko.discard();
                    player.setItemInHand(usedHand, stack);
                    return InteractionResult.sidedSuccess(player.level().isClientSide);
                }
            }
        }
        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.has(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get())) {
            Ability ability = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get());
            ResolvableProfile resolvableProfile = stack.get(DataComponents.PROFILE);
            if (ability == Ability.BLOCK || ability == Ability.THROW || ability == Ability.BLADE) {
                player.startUsingItem(hand);
            } else if (resolvableProfile != null) {
                Player caneOwner = player.level().getPlayerByUUID(resolvableProfile.id().orElse(resolvableProfile.gameProfile().getId()));
                if (caneOwner != null) {
                    MiraculousDataSet miraculousDataSet = caneOwner.getData(MineraculousAttachmentTypes.MIRACULOUS);
                    ResourceKey<Miraculous> storingKey = miraculousDataSet.getFirstKeyIn(MineraculousMiraculousTags.CAN_USE_BUTTERFLY_CANE, player.level());
                    MiraculousData storingData = miraculousDataSet.get(storingKey);
                    if (ability == Ability.KAMIKO_STORE && level instanceof ServerLevel serverLevel && storingData != null && storingData.extraData().contains(TAG_STORED_KAMIKO)) {
                        Kamiko kamiko = MineraculousEntityTypes.KAMIKO.get().create(serverLevel);
                        if (kamiko != null) {
                            kamiko.load(storingData.extraData().getCompound(TAG_STORED_KAMIKO));
                            kamiko.setPos(player.position().add(0, 1, 0));
                            serverLevel.addFreshEntity(kamiko);
                            triggerAnim(player, GeoItem.getOrAssignId(stack, serverLevel), CONTROLLER_USE, ANIMATION_OPEN);
                            storingData.extraData().remove(TAG_STORED_KAMIKO);
                            miraculousDataSet.put(caneOwner, storingKey, storingData, true);
                        }
                    }
                } else
                    return InteractionResultHolder.fail(stack);
            }
            if (level instanceof ServerLevel serverLevel) {
                long animId = GeoItem.getOrAssignId(stack, serverLevel);
                switch (ability) {
                    case BLOCK -> triggerAnim(player, animId, CONTROLLER_USE, ANIMATION_BLOCK);
                    case null, default -> {}
                }
            }
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        if (stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get()) == Ability.BLOCK && remainingUseDuration % 10 == 0) {
            livingEntity.playSound(MineraculousSoundEvents.GENERIC_SHIELD.get());
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        if (entity.level() instanceof ServerLevel serverLevel && stack.has(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get())) {
            long animId = GeoItem.getOrAssignId(stack, serverLevel);
            if (stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get()) == Ability.BLOCK) {
                stopTriggeredAnim(entity, animId, CONTROLLER_USE, ANIMATION_BLOCK);
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (livingEntity instanceof Player player) {
            if (stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get()) == Ability.THROW || stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get()) == Ability.BLADE) {
                int i = this.getUseDuration(stack, livingEntity) - timeLeft;
                if (i >= 10) {
                    if (!level.isClientSide) {
                        ThrownButterflyCane thrown = new ThrownButterflyCane(livingEntity, level, stack);
                        thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(livingEntity.getUsedItemHand()));
                        if (player.hasInfiniteMaterials()) {
                            thrown.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                        }

                        level.addFreshEntity(thrown);
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
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return switch (stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get())) {
            case BLOCK -> UseAnim.BLOCK;
            case THROW, BLADE -> UseAnim.SPEAR;
            case null, default -> UseAnim.NONE;
        };
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        ButterflyCaneItem.Ability ability = stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY.get());
        return switch (ability) {
            case BLOCK -> itemAbility == ItemAbilities.SHIELD_BLOCK;
            case null, default -> false;
        };
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        if (stack.get(MineraculousDataComponents.BUTTERFLY_CANE_ABILITY) == Ability.BLADE)
            return BLADE;
        return COVERED;
    }

    public enum Ability implements RadialMenuOption {
        BLADE,
        BLOCK,
        KAMIKO_STORE,
        THROW;

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
