package dev.thomasglasser.mineraculous.world.item;

import com.mojang.serialization.Codec;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.client.MineraculousClientEvents;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.client.renderer.item.LadybugYoyoRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundActivateToolPayload;
import dev.thomasglasser.mineraculous.network.ServerboundEquipToolPayload;
import dev.thomasglasser.mineraculous.network.ServerboundJumpMidSwingingPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetLadybugYoyoAbilityPayload;
import dev.thomasglasser.mineraculous.network.ServerboundWalkMidSwingingPayload;
import dev.thomasglasser.mineraculous.sounds.MineraculousSoundEvents;
import dev.thomasglasser.mineraculous.tags.MineraculousMiraculousTags;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.entity.miraculous.MineraculousMiraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.client.renderer.BewlrProvider;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ResolvableProfile;
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
    public static final ResourceLocation EXTENDED_PROPERTY_ID = Mineraculous.modLoc("extended");
    public static final RawAnimation OPEN_IDLE = RawAnimation.begin().thenPlay("misc.open_idle");
    public static final String TAG_STORED_KAMIKOS = "StoredKamikos";
    public static final String CONTROLLER_USE = "use_controller";
    public static final String ANIMATION_BLOCK = "block";
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
            if (stack.has(MineraculousDataComponents.ACTIVE) && stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY) == Ability.PURIFY && !state.isCurrentAnimation(OPEN))
                return state.setAndContinue(OPEN_IDLE);
            return state.setAndContinue(DefaultAnimations.IDLE);
        })
                .triggerableAnim(ANIMATION_BLOCK, DefaultAnimations.ATTACK_BLOCK));
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
            if (level.isClientSide() && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)) {
                InteractionHand hand = player.getMainHandItem() == stack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

                CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(entity);
                int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAIT_TICKS);
                if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen()) {
                    if (MineraculousKeyMappings.ACTIVATE_TOOL.get().isDown()) {
                        boolean activate = !stack.has(MineraculousDataComponents.ACTIVE);
                        if (activate) {
                            stack.set(MineraculousDataComponents.ACTIVE, Unit.INSTANCE);
                        } else {
                            stack.remove(MineraculousDataComponents.ACTIVE);
                        }
                        TommyLibServices.NETWORK.sendToServer(new ServerboundActivateToolPayload(activate, hand));
                        playerData.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, 10);
                    } else if (MineraculousKeyMappings.OPEN_TOOL_WHEEL.get().isDown()) {
                        if (stack.has(MineraculousDataComponents.ACTIVE)) {
                            int color = level.holderOrThrow(MineraculousMiraculous.LADYBUG).value().color().getValue();
                            ResolvableProfile resolvableProfile = stack.get(DataComponents.PROFILE);
                            if (resolvableProfile != null) {
                                Player yoyoOwner = player.level().getPlayerByUUID(resolvableProfile.id().orElse(resolvableProfile.gameProfile().getId()));
                                if (yoyoOwner != null) {
                                    ResourceKey<Miraculous> colorKey = yoyoOwner.getData(MineraculousAttachmentTypes.MIRACULOUS).getFirstKeyIn(MineraculousMiraculousTags.CAN_USE_LADYBUG_YOYO, level);
                                    if (colorKey != null)
                                        color = level.holderOrThrow(colorKey).value().color().getValue();
                                }
                            }
                            MineraculousClientEvents.openToolWheel(color, stack, option -> {
                                if (option instanceof Ability ability) {
                                    stack.set(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get(), ability);
                                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetLadybugYoyoAbilityPayload(hand, ability.name()));
                                }
                            }, Arrays.stream(Ability.values()).filter(ability -> {
                                if (ability == Ability.PURIFY)
                                    return stack.has(DataComponents.PROFILE);
                                return true;
                            }).toArray(Ability[]::new));
                        } else {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundEquipToolPayload(hand));
                        }
                        playerData.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, 10);
                    } else if (Minecraft.getInstance().player.input.jumping && stack.has(MineraculousDataComponents.ACTIVE)) {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundJumpMidSwingingPayload());
                    } else if (Minecraft.getInstance().player != null &&
                            (Minecraft.getInstance().player.input.up ||
                                    Minecraft.getInstance().player.input.down ||
                                    Minecraft.getInstance().player.input.left ||
                                    Minecraft.getInstance().player.input.right ||
                                    MineraculousKeyMappings.WEAPON_DOWN_ARROW.get().isDown() ||
                                    MineraculousKeyMappings.WEAPON_UP_ARROW.get().isDown())) {
                                        boolean front = Minecraft.getInstance().player.input.up;
                                        boolean back = Minecraft.getInstance().player.input.down;
                                        boolean left = Minecraft.getInstance().player.input.left;
                                        boolean right = Minecraft.getInstance().player.input.right;
                                        boolean up = MineraculousKeyMappings.WEAPON_UP_ARROW.get().isDown();
                                        boolean down = MineraculousKeyMappings.WEAPON_DOWN_ARROW.get().isDown();
                                        TommyLibServices.NETWORK.sendToServer(new ServerboundWalkMidSwingingPayload(front, back, left, right, up, down));
                                    }
                }
                TommyLibServices.ENTITY.setPersistentData(entity, playerData, false);
            }
        }

        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (!stack.has(MineraculousDataComponents.ACTIVE))
            return InteractionResultHolder.fail(stack);
        Ability ability = stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get());
        if (ability != null) {
            if (!pPlayer.getCooldowns().isOnCooldown(this)) {
                if (level instanceof ServerLevel serverLevel) {
                    Optional<Integer> id = pPlayer.getData(MineraculousAttachmentTypes.LADYBUG_YOYO);
                    if (id.isPresent()) {
                        if (serverLevel.getEntity(id.get()) instanceof ThrownLadybugYoyo thrownLadybugYoyo) {
                            if (thrownLadybugYoyo.isRecalling() && ability == thrownLadybugYoyo.getAbility()) {
                                thrownLadybugYoyo.discard();
                                throwYoyo(stack, pPlayer, stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY));
                                pPlayer.getCooldowns().addCooldown(this, 5);
                            } else if (ability == LadybugYoyoItem.Ability.LASSO) {
                                List<Entity> entities = serverLevel.getEntities(thrownLadybugYoyo.getOwner(), thrownLadybugYoyo.getBoundingBox().inflate(2, 1, 2), entity -> entity != thrownLadybugYoyo);
                                for (Entity entity : entities) {
                                    CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(entity);
                                    entityData.remove(MineraculousEntityEvents.TAG_YOYO_BOUND_POS);
                                    TommyLibServices.ENTITY.setPersistentData(entity, entityData, true);
                                }
                                thrownLadybugYoyo.clearBoundPos();
                                recallYoyo(pPlayer);
                            } else {
                                recallYoyo(pPlayer);
                            }
                        }
                    } else if (ability == Ability.BLOCK) {
                        pPlayer.startUsingItem(pHand);
                        long animId = GeoItem.getOrAssignId(stack, serverLevel);
                        triggerAnim(pPlayer, animId, CONTROLLER_USE, ANIMATION_BLOCK);
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
        if (stack.get(MineraculousDataComponents.LADYBUG_YOYO_ABILITY.get()) == Ability.BLOCK && remainingUseDuration % 7 == 0) {
            livingEntity.playSound(MineraculousSoundEvents.LADYBUG_YOYO_SHIELD.get());
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

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (stack.has(MineraculousDataComponents.ACTIVE) && entity instanceof Player player && !player.getCooldowns().isOnCooldown(this)) {
            if (entity.level() instanceof ServerLevel serverLevel) {
                Optional<Integer> id = entity.getData(MineraculousAttachmentTypes.LADYBUG_YOYO);
                if (id.isPresent()) {
                    if (serverLevel.getEntity(id.get()) instanceof ThrownLadybugYoyo thrownLadybugYoyo) {
                        if (thrownLadybugYoyo.getAbility() == Ability.TRAVEL) {
                            if (thrownLadybugYoyo.inGround()) {
                                Vec3 fromPlayerToYoyo = new Vec3(thrownLadybugYoyo.getX() - player.getX(), thrownLadybugYoyo.getY() - player.getY() + 2, thrownLadybugYoyo.getZ() - player.getZ());
                                player.setDeltaMovement(fromPlayerToYoyo.scale(0.2).add(player.getDeltaMovement()));
                                player.hurtMarked = true;
                            }
                        } else if (thrownLadybugYoyo.getAbility() == LadybugYoyoItem.Ability.LASSO) {
                            List<Entity> entities = serverLevel.getEntities(thrownLadybugYoyo.getOwner(), thrownLadybugYoyo.getBoundingBox().inflate(2, 1, 2), e -> e != thrownLadybugYoyo);
                            for (Entity e : entities) {
                                CompoundTag entityData = TommyLibServices.ENTITY.getPersistentData(e);
                                entityData.remove(MineraculousEntityEvents.TAG_YOYO_BOUND_POS);
                                TommyLibServices.ENTITY.setPersistentData(e, entityData, true);
                                Vec3 fromEntityToPlayer = new Vec3(entity.getX() - e.getX(), entity.getY() - e.getY(), entity.getZ() - e.getZ());
                                e.setDeltaMovement(fromEntityToPlayer.scale(0.2));
                                e.hurtMarked = true;
                            }
                            thrownLadybugYoyo.clearBoundPos();
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
        return stack.has(MineraculousDataComponents.ACTIVE);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return false;
    }

    public void recallYoyo(Player player) {
        Optional<Integer> id = player.getData(MineraculousAttachmentTypes.LADYBUG_YOYO);
        if (id.isPresent()) {
            Level level = player.level();
            if (level instanceof ServerLevel serverLevel && serverLevel.getEntity(id.get()) instanceof ThrownLadybugYoyo thrownLadybugYoyo) {
                thrownLadybugYoyo.recall();
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
            level.addFreshEntity(thrown);
            thrown.setNoGravity(true);
            level.playSound(null, thrown, SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        player.gameEvent(GameEvent.ITEM_INTERACT_START);
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
        return !stack.has(MineraculousDataComponents.ACTIVE);
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
        LASSO,
        PURIFY,
        TRAVEL;

        public static final Codec<Ability> CODEC = Codec.STRING.xmap(Ability::valueOf, Ability::name);
        public static final StreamCodec<ByteBuf, Ability> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Ability::valueOf, Ability::name);

        private final String translationKey;

        Ability() {
            this.translationKey = MineraculousItems.LADYBUG_YOYO.getId().toLanguageKey("ability", name().toLowerCase());
        }

        @Override
        public String translationKey() {
            return translationKey;
        }
    }
}
