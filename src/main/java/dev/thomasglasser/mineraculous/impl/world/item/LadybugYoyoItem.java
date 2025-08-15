package dev.thomasglasser.mineraculous.impl.world.item;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.api.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.api.tags.MiraculousTags;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.RadialMenuProvider;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityReversionEntityData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculouses;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.client.renderer.item.LadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.impl.network.ServerboundEquipToolPayload;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.impl.world.level.storage.LeashingLadybugYoyoData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.client.renderer.BewlrProvider;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
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

public class LadybugYoyoItem extends Item implements ModeledItem, GeoItem, ICurioItem, RadialMenuProvider<LadybugYoyoItem.Ability> {
    public static final String CONTROLLER_USE = "use_controller";
    public static final String CONTROLLER_OPEN = "open_controller";
    public static final String ANIMATION_OPEN_OUT = "open_out";
    public static final String ANIMATION_CLOSE_IN = "close_in";

    private static final RawAnimation OPEN_OUT = RawAnimation.begin().thenPlay("misc.open_out");
    private static final RawAnimation CLOSE_IN = RawAnimation.begin().thenPlay("misc.close_in");

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
                if (stack.getOrDefault(MineraculousDataComponents.ACTIVE, false) && stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY) == Ability.PURIFY && !state.isCurrentAnimation(OPEN_OUT))
                    return state.setAndContinue(DefaultAnimations.IDLE);
                else if (stack.has(MineraculousDataComponents.BLOCKING))
                    return state.setAndContinue(DefaultAnimations.ATTACK_BLOCK);
            }
            return PlayState.STOP;
        }));
        controllers.add(new AnimationController<>(this, CONTROLLER_OPEN, state -> PlayState.CONTINUE)
                .triggerableAnim(ANIMATION_OPEN_OUT, OPEN_OUT)
                .triggerableAnim(ANIMATION_CLOSE_IN, CLOSE_IN));
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
        if (entity instanceof LivingEntity livingEntity && !livingEntity.isUsingItem()) {
            if (!level.isClientSide()) {
                ThrownLadybugYoyoData data = livingEntity.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
                if (data.safeFallTicks() > 0) {
                    livingEntity.resetFallDistance();
                    data.decrementSafeFallTicks().save(livingEntity, true);
                }
            }
        }

        if (stack.has(MineraculousDataComponents.BLOCKING) && entity.getXRot() <= -75 && entity.getDeltaMovement().y <= 0) {
            entity.setDeltaMovement(entity.getDeltaMovement().x, -0.1, entity.getDeltaMovement().z);
            entity.resetFallDistance();
        }

        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!stack.getOrDefault(MineraculousDataComponents.ACTIVE, false))
            return InteractionResultHolder.fail(stack);
        Ability ability = stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY);
        if (ability != null) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                if (level instanceof ServerLevel serverLevel) {
                    ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
                    if (data.id().isPresent()) {
                        ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(serverLevel);
                        if (thrownYoyo != null) {
                            if (thrownYoyo.isRecalling() && ability == thrownYoyo.getAbility()) {
                                thrownYoyo.discard();
                                throwYoyo(stack, player, stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY), usedHand);
                                player.getCooldowns().addCooldown(this, 5);
                            } else {
                                recallYoyo(player);
                            }
                        } else {
                            data.clearId().save(player, true);
                        }
                    } else if (ability == Ability.BLOCK || ability == Ability.PURIFY) {
                        player.startUsingItem(usedHand);
                    } else if (ability == Ability.LASSO && player.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).isPresent()) {
                        removeLeash(player);
                    } else if (usedHand == InteractionHand.MAIN_HAND || ability != Ability.LASSO) {
                        throwYoyo(stack, player, ability, usedHand);
                        player.getCooldowns().addCooldown(this, 5);
                    }
                }
            }
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, usedHand);
    }

    public static void removeLeash(Entity holder) {
        holder.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).ifPresent(data -> {
            Entity leashed = holder.level().getEntity(data.leashedId());
            if (leashed instanceof Leashable leashable) {
                leashable.dropLeash(true, false);
                leashed.setData(MineraculousAttachmentTypes.YOYO_LEASH_HOLDER, Optional.empty());
                TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(leashed.getId(), MineraculousAttachmentTypes.YOYO_LEASH_HOLDER, Optional.<UUID>empty()), leashed.getServer());
            }
            LeashingLadybugYoyoData.remove(holder, true);
        });
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
        if (stack.has(MineraculousDataComponents.BLOCKING) && remainingUseDuration % 7 == 0) {
            livingEntity.playSound(MineraculousSoundEvents.LADYBUG_YOYO_SPIN.get());
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged) {
        if (stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY) == Ability.PURIFY && level instanceof ServerLevel serverLevel) {
            UUID ownerId = stack.get(MineraculousDataComponents.OWNER);
            Entity owner = ownerId != null ? serverLevel.getEntity(ownerId) : null;
            if (owner != null) {
                MiraculousesData miraculousesData = owner.getData(MineraculousAttachmentTypes.MIRACULOUSES);
                Holder<Miraculous> storingKey = miraculousesData.getFirstTransformedIn(MiraculousTags.CAN_USE_LADYBUG_YOYO);
                MiraculousData storingData = miraculousesData.get(storingKey);
                if (storingData != null) {
                    List<CompoundTag> stored = storingData.storedEntities();
                    if (!stored.isEmpty()) {
                        Set<Entity> entities = new ReferenceOpenHashSet<>();
                        for (CompoundTag tag : stored) {
                            Entity entity = EntityType.loadEntityRecursive(tag, level, loaded -> {
                                loaded.setPos(livingEntity.getX(), livingEntity.getY() + 0.5, livingEntity.getZ());
                                return loaded;
                            });
                            if (entity != null) {
                                serverLevel.addFreshEntity(entity);
                                Entity reverted = AbilityReversionEntityData.get(serverLevel).revertConversion(entity.getUUID(), serverLevel);
                                if (reverted != null) {
                                    reverted.addDeltaMovement(new Vec3(0, 0.1 * ((getUseDuration(stack, livingEntity) - timeCharged) / 10.0), 0));
                                    entities.add(reverted);
                                }
                            }
                        }
                        if (livingEntity instanceof ServerPlayer serverPlayer) {
                            MineraculousCriteriaTriggers.RELEASED_PURIFIED_ENTITIES.get().trigger(serverPlayer, entities);
                        }
                    }
                    storingData.storedEntities().clear();
                    storingData.save(storingKey, livingEntity, true);
                }
            }
        }
        super.releaseUsing(stack, level, livingEntity, timeCharged);
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
                        }
                        recallYoyo(player);
                    }
                } else if (entity.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).isEmpty()) {
                    throwYoyo(stack, player, stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get()) == Ability.PURIFY ? Ability.PURIFY : null, hand);
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

    public void throwYoyo(ItemStack stack, Player player, Ability ability, InteractionHand hand) {
        Level level = player.level();
        if (!level.isClientSide) {
            ThrownLadybugYoyo thrown = new ThrownLadybugYoyo(player, level, stack, ability);
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
            thrown.setInitialDirection(player.getDirection());
            thrown.setHand(hand);
            level.addFreshEntity(thrown);
            thrown.setNoGravity(true);
            level.playSound(null, thrown, SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        player.gameEvent(GameEvent.ITEM_INTERACT_START);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        Ability ability = stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY);
        if (ability == Ability.BLOCK) {
            return 72000;
        } else if (ability == Ability.PURIFY) {
            return 100;
        }
        return 0;
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

    @Override
    public boolean canOpenMenu(ItemStack stack, InteractionHand hand, Player holder) {
        return stack.getOrDefault(MineraculousDataComponents.ACTIVE, false);
    }

    @Override
    public int getColor(ItemStack stack, InteractionHand hand, Player holder) {
        Level level = holder.level();
        int color = level.holderOrThrow(Miraculouses.LADYBUG).value().color().getValue();
        UUID ownerId = stack.get(MineraculousDataComponents.OWNER);
        if (ownerId != null) {
            Entity owner = level.getEntities().get(ownerId);
            if (owner != null) {
                Holder<Miraculous> colorKey = owner.getData(MineraculousAttachmentTypes.MIRACULOUSES).getFirstTransformedIn(MiraculousTags.CAN_USE_LADYBUG_YOYO);
                if (colorKey != null)
                    color = colorKey.value().color().getValue();
            }
        }
        return color;
    }

    @Override
    public List<Ability> getOptions(ItemStack stack, InteractionHand hand, Player holder) {
        return Ability.valuesList();
    }

    @Override
    public boolean handleSecondaryKeyBehavior(ItemStack stack, InteractionHand hand, Player holder) {
        TommyLibServices.NETWORK.sendToServer(new ServerboundEquipToolPayload(hand));
        return true;
    }

    @Override
    public Supplier<DataComponentType<Ability>> getComponentType(ItemStack stack, InteractionHand hand, Player holder) {
        return MineraculousDataComponents.LADYBUG_YOYO_ABILITY;
    }

    @Override
    public Ability setOption(ItemStack stack, InteractionHand hand, Player holder, int index) {
        Ability old = stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY);
        Ability selected = RadialMenuProvider.super.setOption(stack, hand, holder, index);
        if (holder.level() instanceof ServerLevel level) {
            String anim = null;
            if (selected == LadybugYoyoItem.Ability.PURIFY)
                anim = ANIMATION_OPEN_OUT;
            else if (old == Ability.PURIFY)
                anim = ANIMATION_CLOSE_IN;
            if (anim != null)
                triggerAnim(holder, GeoItem.getOrAssignId(stack, level), CONTROLLER_OPEN, anim);
        }
        return selected;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged && super.shouldCauseReequipAnimation(oldStack, newStack, true);
    }

    public enum Ability implements RadialMenuOption, StringRepresentable {
        BLOCK,
        LASSO,
        PHONE((stack, player) -> Mineraculous.Dependencies.TOMMYTECH.isLoaded()),
        PURIFY((stack, player) -> stack.has(MineraculousDataComponents.OWNER)),
        TRAVEL;

        public static final Codec<Ability> CODEC = StringRepresentable.fromEnum(Ability::values);
        public static final StreamCodec<ByteBuf, Ability> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Ability::of, Ability::getSerializedName);

        private static final List<Ability> VALUES_LIST = new ReferenceArrayList<>(values());

        private final BiPredicate<ItemStack, Player> enabledPredicate;
        private final Component displayName;

        Ability() {
            this((stack, player) -> true);
        }

        Ability(BiPredicate<ItemStack, Player> enabledPredicate) {
            this.enabledPredicate = enabledPredicate;
            this.displayName = Component.translatable(MineraculousItems.LADYBUG_YOYO.getId().toLanguageKey("ability", getSerializedName()));
        }

        @Override
        public Component displayName() {
            return displayName;
        }

        @Override
        public boolean isEnabled(ItemStack stack, Player holder) {
            return enabledPredicate.test(stack, holder);
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }

        public static List<Ability> valuesList() {
            return VALUES_LIST;
        }

        public static Ability of(String name) {
            return valueOf(name.toUpperCase());
        }
    }
}
