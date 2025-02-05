package dev.thomasglasser.mineraculous.world.item;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.advancements.MineraculousCriteriaTriggers;
import dev.thomasglasser.mineraculous.advancements.critereon.MiraculousUsePowerTrigger;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.renderer.item.MiraculousRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundMiraculousTransformPayload;
import dev.thomasglasser.mineraculous.network.ServerboundPutToolInHandPayload;
import dev.thomasglasser.mineraculous.network.ServerboundRenounceMiraculousPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetMiraculousPowerActivatedPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kwami;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.ability.Ability;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.ChargeOverrideData;
import dev.thomasglasser.mineraculous.world.level.storage.ChargeOverrideDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ResolvableProfile;
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
    public static final int FIVE_MINUTES = 6000;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public MiraculousItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC)
                .component(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                .fireResistant());
        GeckoLibUtil.registerSyncedAnimatable(this);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ResourceKey<Miraculous> miraculousKey = stack.get(MineraculousDataComponents.MIRACULOUS);
        if (miraculousKey != null && context.registries() != null) {
            Miraculous miraculous = context.registries().holderOrThrow(miraculousKey).value();
            tooltipComponents.add(Component.translatable(miraculousKey.location().toLanguageKey(miraculousKey.registry().getPath())).withStyle(style -> style.withColor(miraculous.color())));
        }
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level instanceof ServerLevel serverLevel) {
            KwamiData kwamiData = stack.get(MineraculousDataComponents.KWAMI_DATA.get());
            if (entity instanceof Player player && (!stack.has(DataComponents.PROFILE) || !stack.get(DataComponents.PROFILE).gameProfile().equals(player.getGameProfile()))) {
                stack.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
                if (kwamiData != null) {
                    Entity e = serverLevel.getEntity(kwamiData.uuid());
                    if (e instanceof Kwami kwami) {
                        kwami.tame(player);
                    } else if (!stack.has(MineraculousDataComponents.POWERED)) {
                        stack.remove(MineraculousDataComponents.KWAMI_DATA);
                    }
                }
            }
            if (!stack.has(MineraculousDataComponents.POWERED.get()) && !stack.has(MineraculousDataComponents.KWAMI_DATA.get())) {
                stack.set(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE);
            }
            if (kwamiData != null) {
                ChargeOverrideData overrideData = ((ChargeOverrideDataHolder) serverLevel.getServer().overworld()).mineraculous$getChargeOverrideData();
                if (overrideData.has(kwamiData.uuid())) {
                    Entity e = serverLevel.getEntity(kwamiData.uuid());
                    if (e instanceof Kwami kwami) {
                        kwami.setCharged(overrideData.get(kwamiData.uuid()));
                        overrideData.remove(kwamiData.uuid());
                    }
                }
            }
            if (slotId != -1 && stack.has(MineraculousDataComponents.REMAINING_TICKS)) {
                stack.remove(MineraculousDataComponents.REMAINING_TICKS);
            }
        } else {
            if (ClientUtils.getMainClientPlayer() == entity && (isSelected || slotId == Inventory.SLOT_OFFHAND)) {
                CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(entity);
                int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAIT_TICKS);
                if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen() && !MineraculousClientUtils.isCameraEntityOther()) {
                    if (MineraculousKeyMappings.ACTIVATE_POWER.get().isDown()) {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundRenounceMiraculousPayload(slotId));
                        playerData.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, 10);
                    }
                }
                TommyLibServices.ENTITY.setPersistentData(entity, playerData, false);
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public List<Component> getSlotsTooltip(List<Component> tooltips, TooltipContext context, ItemStack stack) {
        var slotsTooltip = Component.translatable("curios.tooltip.slot").append(" ").withStyle(ChatFormatting.GOLD);
        ResourceKey<Miraculous> miraculousKey = stack.get(MineraculousDataComponents.MIRACULOUS);
        if (miraculousKey != null && context.registries() != null) {
            slotsTooltip.append(Component.translatable("curios.identifier." + context.registries().holderOrThrow(miraculousKey).value().acceptableSlot()).withStyle(ChatFormatting.YELLOW));
        }
        ArrayList<Component> newTooltips = new ArrayList<>(tooltips);
        newTooltips.removeLast();
        newTooltips.add(slotsTooltip);
        return newTooltips;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        ResourceKey<Miraculous> miraculousKey = stack.get(MineraculousDataComponents.MIRACULOUS);
        if (entity instanceof Player player && miraculousKey != null && player.level().holderOrThrow(miraculousKey).value().acceptableSlot().equals(slotContext.identifier())) {
            MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(miraculousKey);
            if (data.transformed()) {
                if (player.level() instanceof ServerLevel serverLevel) {
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    Integer transformationFrames = stack.get(MineraculousDataComponents.TRANSFORMATION_FRAMES);
                    if (transformationFrames != null && transformationFrames >= 1 && player.tickCount % 2 == 0) {
                        if (transformationFrames <= 1) {
                            stack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, null);
                            entity.getArmorSlots().forEach(armorStack -> armorStack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, null));
                            ItemStack tool = data.createTool(serverPlayer);
                            if (!tool.isEmpty()) {
                                if (serverLevel.holderOrThrow(miraculousKey).value().toolSlot().isPresent()) {
                                    boolean added = CuriosUtils.setStackInFirstValidSlot(player, serverLevel.holderOrThrow(miraculousKey).value().toolSlot().get(), tool, true);
                                    if (!added) {
                                        player.addItem(tool);
                                    }
                                } else {
                                    player.addItem(tool);
                                }
                            }
                        } else {
                            int newTransformationTicks = transformationFrames - 1;
                            stack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, newTransformationTicks);
                            entity.getArmorSlots().forEach(armorStack -> armorStack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, newTransformationTicks));
                        }
                    }
                    if (data.shouldCountDown() && stack.getOrDefault(MineraculousDataComponents.REMAINING_TICKS.get(), 0) <= 0) {
                        MineraculousEntityEvents.handleMiraculousTransformation((ServerPlayer) player, miraculousKey, data, false, false, false);
                        serverLevel.playSound(null, player, serverLevel.holderOrThrow(miraculousKey).value().timerEndSound().value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                    AtomicBoolean overrideActive = new AtomicBoolean(false);
                    AbilityData abilityData = new AbilityData(data.powerLevel(), Either.left(miraculousKey));
                    Miraculous miraculous = entity.level().holderOrThrow(miraculousKey).value();
                    miraculous.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                        if (ability.canActivate(abilityData, serverLevel, player.blockPosition(), player) && ability.perform(abilityData, serverLevel, player.blockPosition(), player, Ability.Context.PASSIVE) && ability.overrideActive())
                            overrideActive.set(true);
                    });
                    if (!entity.getMainHandItem().isEmpty()) {
                        miraculous.passiveAbilities().stream().map(Holder::value).forEach(ability -> {
                            if (ability.canActivate(abilityData, serverLevel, player.blockPosition(), player) && ability.perform(abilityData, serverLevel, player.blockPosition(), player, Ability.Context.from(entity.getMainHandItem())) && ability.overrideActive())
                                overrideActive.set(true);
                        });
                        if (data.mainPowerActive()) {
                            if (overrideActive.get()) {
                                entity.getData(MineraculousAttachmentTypes.MIRACULOUS).put(entity, miraculousKey, data.withPowerStatus(false, false), true);
                            } else {
                                boolean usedPower = miraculous.activeAbility().get().value().perform(abilityData, serverLevel, player.blockPosition(), player, Ability.Context.from(entity.getMainHandItem()));
                                if (usedPower) {
                                    entity.getData(MineraculousAttachmentTypes.MIRACULOUS).put(entity, miraculousKey, data.withUsedPower(), true);
                                    MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(serverPlayer, miraculousKey, MiraculousUsePowerTrigger.Context.ITEM);
                                }
                            }
                        }
                    }
                    if (data.mainPowerActive()) {
                        if (overrideActive.get()) {
                            entity.getData(MineraculousAttachmentTypes.MIRACULOUS).put(entity, miraculousKey, data.withPowerStatus(false, false), true);
                        } else {
                            boolean usedPower = miraculous.activeAbility().get().value().perform(abilityData, serverLevel, player.blockPosition(), player, Ability.Context.PASSIVE);
                            if (usedPower) {
                                entity.getData(MineraculousAttachmentTypes.MIRACULOUS).put(entity, miraculousKey, data.withUsedPower(), true);
                                MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(serverPlayer, miraculousKey, MiraculousUsePowerTrigger.Context.ITEM);
                            }
                        }
                    }
                }
                if (data.shouldCountDown()) {
                    int ticks = stack.getOrDefault(MineraculousDataComponents.REMAINING_TICKS.get(), 0) - 1;
                    stack.set(MineraculousDataComponents.REMAINING_TICKS.get(), ticks);
                    int minutes = ticks / SharedConstants.TICKS_PER_MINUTE;
                    if ((ticks % 5 == 0 && ticks % SharedConstants.TICKS_PER_MINUTE < (minutes * 5)) || (ticks <= SharedConstants.TICKS_PER_SECOND * 30 && ticks % 10 == 0) || (ticks <= SharedConstants.TICKS_PER_SECOND * 10 && ticks % 5 == 0)) {
                        slotContext.entity().level().playSound(null, player, slotContext.entity().level().holderOrThrow(miraculousKey).value().timerBeepSound().value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                } else if (stack.has(MineraculousDataComponents.REMAINING_TICKS))
                    stack.remove(MineraculousDataComponents.REMAINING_TICKS);
            } else {
                if (!player.level().isClientSide()) {
                    Integer detransformationFrames = stack.get(MineraculousDataComponents.DETRANSFORMATION_FRAMES);
                    if (detransformationFrames != null && detransformationFrames >= 1 && player.tickCount % 2 == 0) {
                        if (detransformationFrames <= 1) {
                            stack.set(MineraculousDataComponents.DETRANSFORMATION_FRAMES, null);
                            entity.getArmorSlots().forEach(armorStack -> armorStack.set(MineraculousDataComponents.DETRANSFORMATION_FRAMES, null));
                            stack.remove(DataComponents.ENCHANTMENTS);
                            player.getData(MineraculousAttachmentTypes.STORED_ARMOR).ifPresent(armorData -> {
                                for (EquipmentSlot slot : Arrays.stream(EquipmentSlot.values()).filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR).toArray(EquipmentSlot[]::new)) {
                                    player.setItemSlot(slot, armorData.forSlot(slot));
                                }
                            });
                        } else {
                            int newDetransformationFrames = detransformationFrames - 1;
                            stack.set(MineraculousDataComponents.DETRANSFORMATION_FRAMES, newDetransformationFrames);
                            entity.getArmorSlots().forEach(armorStack -> armorStack.set(MineraculousDataComponents.DETRANSFORMATION_FRAMES, newDetransformationFrames));
                        }
                    }
                }
            }
            if (entity.level().isClientSide && ClientUtils.getMainClientPlayer() == player) {
                CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(entity);
                int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAIT_TICKS);
                if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen() && !MineraculousClientUtils.isCameraEntityOther()) {
                    if (MineraculousKeyMappings.TRANSFORM.get().isDown()) {
                        if (data.transformed()) {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculousKey, data, false, false));
                        } else {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculousKey, data, true, false));
                        }
                        playerData.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, 10);
                    } else if (MineraculousKeyMappings.ACTIVATE_POWER.get().isDown() && data.transformed() && !data.mainPowerActive() && !data.usedLimitedPower() && slotContext.entity().level().holderOrThrow(miraculousKey).value().activeAbility().isPresent()) {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundSetMiraculousPowerActivatedPayload(miraculousKey));
                        playerData.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, 10);
                    } else if (MineraculousKeyMappings.OPEN_TOOL_WHEEL.get().isDown() && player.getMainHandItem().isEmpty()) {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundPutToolInHandPayload(miraculousKey));
                        playerData.putInt(MineraculousEntityEvents.TAG_WAIT_TICKS, 10);
                    }
                }
                TommyLibServices.ENTITY.setPersistentData(entity, playerData, false);
            }
        }
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity.level() instanceof ServerLevel serverLevel && entity instanceof Player player) {
            MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS);
            MiraculousData data = miraculousDataSet.get(stack.get(MineraculousDataComponents.MIRACULOUS));
            if (!data.transformed()) {
                data = data.equip(stack, new CuriosData(slotContext.index(), slotContext.identifier()));
                if (stack.has(MineraculousDataComponents.POWERED.get())) {
                    stack.remove(MineraculousDataComponents.POWERED.get());
                    MineraculousEntityEvents.summonKwami(serverLevel, stack.get(MineraculousDataComponents.MIRACULOUS), data, player);
                } else {
                    miraculousDataSet.put(entity, stack.get(MineraculousDataComponents.MIRACULOUS), data, true);
                }
            }
        }
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        ResourceKey<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        if (entity instanceof ServerPlayer serverPlayer && !(stack.is(newStack.getItem()) && miraculous == newStack.get(MineraculousDataComponents.MIRACULOUS))) {
            MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS);
            MiraculousData data = miraculousDataSet.get(miraculous);
            if (data.transformed()) {
                data = data.withCuriosData(CuriosData.EMPTY).withItem(stack.copy());
                MineraculousEntityEvents.handleMiraculousTransformation(serverPlayer, miraculous, data, false, true, true);
            } else {
                data = data.unEquip();
                miraculousDataSet.put(entity, miraculous, data, true);
            }
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return canEquip(slotContext, stack);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        ResourceKey<Miraculous> key = stack.get(MineraculousDataComponents.MIRACULOUS);
        return key != null && slotContext.identifier().equals(slotContext.entity().level().holderOrThrow(key).value().acceptableSlot());
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
            private MiraculousRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new MiraculousRenderer();

                return this.renderer;
            }
        });
    }
}
