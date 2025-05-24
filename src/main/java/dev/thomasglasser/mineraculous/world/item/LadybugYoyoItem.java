package dev.thomasglasser.mineraculous.world.item;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.client.renderer.item.LadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.tags.MineraculousMiraculousTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.mineraculous.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.client.renderer.BewlrProvider;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
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
    public static final String TAG_STORED_KAMIKOS = "StoredKamikos";
    public static final String CONTROLLER_USE = "use_controller";
    public static final String CONTROLLER_OPEN = "open_controller";
    public static final String ANIMATION_OPEN = "open";
    public static final String ANIMATION_CLOSE = "close";

    private static final RawAnimation OPEN = RawAnimation.begin().thenPlay("misc.open");
    private static final RawAnimation CLOSE = RawAnimation.begin().thenPlay("misc.close");

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
            if (stack != null) {
                if (stack.getOrDefault(MineraculousDataComponents.ACTIVE, false) && stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY) == Ability.PURIFY && !state.isCurrentAnimation(OPEN))
                    return state.setAndContinue(DefaultAnimations.IDLE);
                else if (stack.has(MineraculousDataComponents.BLOCKING))
                    return state.setAndContinue(DefaultAnimations.ATTACK_BLOCK);
            }
            return PlayState.STOP;
        }));
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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof Player player && !player.isUsingItem()) {
            if (!level.isClientSide()) {
                ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
                if (data.safeFallTicks() > 0) {
                    player.resetFallDistance();
                    data.decrementSafeFallTicks().save(player, true);
                }
            }
            if (level.isClientSide() && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {
                InteractionHand hand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

                // TODO: Fix
                CompoundTag playerData = /*TommyLibServices.ENTITY.getPersistentData(entity)*/new CompoundTag();
                int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAIT_TICKS);
//                if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen()) {
//                    if (MineraculousKeyMappings.CONFIGURE_TOOL.isDown()) {
//                        if (stack.has(MineraculousDataComponents.ACTIVE)) {
//                            int color = level.holderOrThrow(MineraculousMiraculous.LADYBUG).value().color().getValue();
//                            ResolvableProfile resolvableProfile = stack.get(DataComponents.PROFILE);
//                            if (resolvableProfile != null) {
//                                Player yoyoOwner = player.level().getPlayerByUUID(resolvableProfile.id().orElse(resolvableProfile.gameProfile().getId()));
//                                if (yoyoOwner != null) {
//                                    ResourceKey<Miraculous> colorKey = yoyoOwner.getData(MineraculousAttachmentTypes.MIRACULOUS).getFirstKeyIn(MineraculousMiraculousTags.CAN_USE_LADYBUG_YOYO, level);
//                                    if (colorKey != null)
//                                        color = level.holderOrThrow(colorKey).value().color().getValue();
//                                }
//                            }
//                            MineraculousClientEvents.openToolWheel(color, stack, option -> {
//                                if (option instanceof Ability ability) {
//                                    stack.set(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get(), ability);
//                                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetLadybugYoyoAbilityPayload(hand, ability));
//                                }
//                            }, Arrays.stream(Ability.values()).filter(ability -> {
//                                if (ability == Ability.PURIFY)
//                                    return stack.has(DataComponents.PROFILE);
//                                return true;
//                            }).toArray(Ability[]::new));
//                        } else {
//                            TommyLibServices.NETWORK.sendToServer(new ServerboundEquipToolPayload(hand));
//                        }
//                        playerData.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, 10);
//                    }
//                TommyLibServices.ENTITY.setPersistentData(entity, playerData, false);
            }
        }

        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (!stack.getOrDefault(MineraculousDataComponents.ACTIVE, false))
            return InteractionResultHolder.fail(stack);
        Ability ability = stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get());
        if (ability != null) {
            if (!pPlayer.getCooldowns().isOnCooldown(this)) {
                if (level instanceof ServerLevel serverLevel) {
                    ThrownLadybugYoyoData data = pPlayer.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
                    if (data.id().isPresent()) {
                        ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(serverLevel);
                        if (thrownYoyo != null) {
                            if (thrownYoyo.isRecalling() && ability == thrownYoyo.getAbility()) {
                                thrownYoyo.discard();
                                throwYoyo(stack, pPlayer, stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY));
                                pPlayer.getCooldowns().addCooldown(this, 5);
                            } else if (ability == LadybugYoyoItem.Ability.LASSO) {
                                List<Entity> entities = serverLevel.getEntities(thrownYoyo.getOwner(), thrownYoyo.getBoundingBox().inflate(2, 1, 2), entity -> entity != thrownYoyo);
                                for (Entity entity : entities) {
                                    // TODO: Fix
//                                    CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(entity);
//                                    entityData.remove(MineraculousEntityEvents.TAG_YOYO_BOUND_POS);
//                                    TommyLibServices.ENTITY.setPersistentData(entity, entityData, true);
                                }
                                thrownYoyo.clearBoundPos();
                                recallYoyo(pPlayer);
                            } else {
                                recallYoyo(pPlayer);
                            }
                        } else {
                            data.clearId().save(pPlayer, true);
                        }
                    } else if (ability == Ability.BLOCK) {
                        pPlayer.startUsingItem(pHand);
                    } else if (ability == Ability.PURIFY) {
                        triggerAnim(pPlayer, GeoItem.getOrAssignId(stack, serverLevel), CONTROLLER_USE, ANIMATION_OPEN);
                        MiraculousDataSet miraculousDataSet = pPlayer.getData(MineraculousAttachmentTypes.MIRACULOUS);
                        ResourceKey<Miraculous> storingKey = miraculousDataSet.getFirstKeyIn(MineraculousMiraculousTags.CAN_USE_LADYBUG_YOYO, serverLevel);
                        MiraculousData storingData = miraculousDataSet.get(storingKey);
                        if (storingData != null) {
                            CompoundTag extraData = storingData.extraData();
                            ListTag kamikos = extraData.getList(LadybugYoyoItem.TAG_STORED_KAMIKOS, 10);
                            if (!kamikos.isEmpty()) {
                                MineraculousCriteriaTriggers.RELEASED_PURIFIED_KAMIKO.get().trigger((ServerPlayer) pPlayer, kamikos.size());
                                for (Tag tag : kamikos) {
                                    Kamiko kamiko = MineraculousEntityTypes.KAMIKO.get().create(serverLevel);
                                    if (kamiko != null && tag instanceof CompoundTag compoundTag) {
                                        kamiko.load(compoundTag);
                                        kamiko.setOwnerUUID(null);
                                        kamiko.setPos(pPlayer.getX(), pPlayer.getY() + 0.5, pPlayer.getZ());
                                        kamiko.addDeltaMovement(new Vec3(0, 1, 0));
                                        serverLevel.addFreshEntity(kamiko);
                                    }
                                }
                                extraData.remove(LadybugYoyoItem.TAG_STORED_KAMIKOS);
                            }
                            pPlayer.getData(MineraculousAttachmentTypes.MIRACULOUS).put(pPlayer, storingKey, storingData, true);
                        }
                    } else {
                        throwYoyo(stack, pPlayer, stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY));
                        pPlayer.getCooldowns().addCooldown(this, 5);
                    }
                }
            }
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, pPlayer, pHand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        if (stack.has(MineraculousDataComponents.BLOCKING) && remainingUseDuration % 7 == 0) {
            livingEntity.playSound(MineraculousSoundEvents.LADYBUG_YOYO_SHIELD.get());
        }
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (stack.getOrDefault(MineraculousDataComponents.ACTIVE, false) && entity instanceof Player player && !player.getCooldowns().isOnCooldown(this)) {
            if (entity.level() instanceof ServerLevel serverLevel) {
                ThrownLadybugYoyoData data = entity.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
                if (data.id().isPresent()) {
                    ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(serverLevel);
                    if (thrownYoyo != null) {
                        if (thrownYoyo.getAbility() == Ability.TRAVEL) {
                            if (thrownYoyo.inGround()) {
                                Vec3 fromPlayerToYoyo = new Vec3(thrownYoyo.getX() - player.getX(), thrownYoyo.getY() - player.getY() + 2, thrownYoyo.getZ() - player.getZ());
                                player.setDeltaMovement(fromPlayerToYoyo.scale(0.2).add(player.getDeltaMovement()));
                                player.hurtMarked = true;
                                data.startSafeFall().save(player, true);
                            }
                        } else if (thrownYoyo.getAbility() == LadybugYoyoItem.Ability.LASSO) {
                            List<Entity> entities = serverLevel.getEntities(thrownYoyo.getOwner(), thrownYoyo.getBoundingBox().inflate(2, 1, 2), e -> e != thrownYoyo);
                            for (Entity e : entities) {
                                // TODO: Fix
//                                CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(e);
//                                entityData.remove(MineraculousEntityEvents.TAG_YOYO_BOUND_POS);
//                                TommyLibServices.ENTITY.setPersistentData(e, entityData, true);
                                Vec3 fromEntityToPlayer = new Vec3(entity.getX() - e.getX(), entity.getY() - e.getY(), entity.getZ() - e.getZ());
                                e.setDeltaMovement(fromEntityToPlayer.scale(0.2));
                                e.hurtMarked = true;
                            }
                            thrownYoyo.clearBoundPos();
                        }
                        recallYoyo(player);
                    }
                } else {
                    throwYoyo(stack, player, stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get()) == Ability.PURIFY ? Ability.PURIFY : null);
                    player.getCooldowns().addCooldown(this, 5);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return stack.getOrDefault(MineraculousDataComponents.ACTIVE, false);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }

    public void recallYoyo(Player player) {
        ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
        if (data.id().isPresent()) {
            Level level = player.level();
            if (!level.isClientSide) {
                ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(level);
                if (thrownYoyo != null) {
                    thrownYoyo.recall();
                    data.startSafeFall().save(player, true);
                }
            }
            level.playSound(null, player, SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 1.0F, 1.0F);
            player.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        }
    }

    public void throwYoyo(ItemStack stack, Player player, Ability ability) {
        Level level = player.level();
        if (!level.isClientSide) {
            ThrownLadybugYoyo thrown = new ThrownLadybugYoyo(player, level, stack, ability);
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
            thrown.setInitialDirection(player.getDirection());
            level.addFreshEntity(thrown);
            thrown.setNoGravity(true);
            level.playSound(null, thrown, SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        player.gameEvent(GameEvent.ITEM_INTERACT_START);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if (stack.has(MineraculousDataComponents.BLOCKING))
            return UseAnim.BLOCK;
        return UseAnim.NONE;
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
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return canEquip(stack);
    }

    public boolean canEquip(ItemStack stack) {
        return !stack.getOrDefault(MineraculousDataComponents.ACTIVE, false);
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, TooltipContext context, ItemStack stack) {
        if (canEquip(stack)) {
            return ICurioItem.super.getSlotsTooltip(tooltips, context, stack);
        }
        return List.of();
    }

    @Override
    public double getBoneResetTime() {
        return 0;
    }

    public enum Ability implements RadialMenuOption, StringRepresentable {
        BLOCK,
        LASSO,
        PURIFY,
        TRAVEL;

        public static final Codec<Ability> CODEC = StringRepresentable.fromEnum(Ability::values);
        public static final StreamCodec<ByteBuf, Ability> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Ability::of, Ability::getSerializedName);

        private final String translationKey;

        Ability() {
            this.translationKey = MineraculousItems.LADYBUG_YOYO.getId().toLanguageKey("ability", getSerializedName());
        }

        @Override
        public String translationKey() {
            return translationKey;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }

        public static Ability of(String name) {
            return valueOf(name.toUpperCase());
        }
    }
}
