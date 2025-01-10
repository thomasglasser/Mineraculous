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
import dev.thomasglasser.mineraculous.network.ServerboundSetPowerActivatedPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kwami;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.Ability;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
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
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
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
            if (entity instanceof Player player && (!stack.has(DataComponents.PROFILE) || !stack.get(DataComponents.PROFILE).gameProfile().equals(player.getGameProfile()))) {
                stack.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
                KwamiData kwamiData = stack.get(MineraculousDataComponents.KWAMI_DATA);
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
        } else {
            if (ClientUtils.getMainClientPlayer() == entity && (isSelected || slotId == Inventory.SLOT_OFFHAND)) {
                CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(entity);
                int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAITTICKS);
                if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen() && !MineraculousClientUtils.isCameraEntityOther()) {
                    if (MineraculousKeyMappings.ACTIVATE_POWER.get().isDown()) {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundRenounceMiraculousPayload(slotId));
                        playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
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
        ResourceKey<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
        if (entity instanceof Player player && miraculous != null && player.level().holderOrThrow(miraculous).value().acceptableSlot().equals(slotContext.identifier())) {
            MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(miraculous);
            if (data.transformed()) {
                if (player.level() instanceof ServerLevel serverLevel) {
                    Integer transformationFrames = stack.get(MineraculousDataComponents.TRANSFORMATION_FRAMES);
                    if (transformationFrames != null && transformationFrames >= 1 && player.tickCount % 2 == 0) {
                        if (transformationFrames <= 1) {
                            stack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, null);
                            entity.getArmorSlots().forEach(armorStack -> armorStack.set(MineraculousDataComponents.TRANSFORMATION_FRAMES, null));
                            ItemStack tool = data.createTool(serverLevel);
                            if (!tool.isEmpty()) {
                                if (player.level().holderOrThrow(miraculous).value().toolSlot().isPresent()) {
                                    boolean added = CuriosUtils.setStackInFirstValidSlot(player, player.level().holderOrThrow(miraculous).value().toolSlot().get(), tool, true);
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
                        MineraculousEntityEvents.handleMiraculousTransformation((ServerPlayer) player, miraculous, data, false, false);
                        serverLevel.playSound(null, player, serverLevel.holderOrThrow(miraculous).value().timerEndSound().value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
                if (data.shouldCountDown()) {
                    int ticks = stack.getOrDefault(MineraculousDataComponents.REMAINING_TICKS.get(), 0);
                    final float second = ticks / 20F;
                    final float minute = (second / 60) + 1;
                    stack.set(MineraculousDataComponents.REMAINING_TICKS.get(), ticks - 1);
                    int i = (int) minute - 1;
                    float every = (second <= 10 ? 0.25F : i < 1 ? 1F : (i * 2));
                    if (ticks > 1 && (second % every) == (i > 0 ? 1 : 0))
                        slotContext.entity().level().playSound(null, player, slotContext.entity().level().holderOrThrow(miraculous).value().timerBeepSound().value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                } else if (stack.has(MineraculousDataComponents.REMAINING_TICKS))
                    stack.remove(MineraculousDataComponents.REMAINING_TICKS);
                AtomicBoolean overrideActive = new AtomicBoolean(false);
                entity.level().holderOrThrow(miraculous).value().passiveAbilities().forEach(ability -> {
                    if (ability.value().perform(new AbilityData(data.powerLevel(), Either.left(miraculous)), player.level(), player.blockPosition(), player, Ability.Context.PASSIVE) && ability.value().overrideActive())
                        overrideActive.set(true);
                });
                if (!entity.getMainHandItem().isEmpty()) {
                    entity.level().holderOrThrow(miraculous).value().passiveAbilities().forEach(ability -> {
                        if (ability.value().perform(new AbilityData(data.powerLevel(), Either.left(miraculous)), player.level(), player.blockPosition(), player, Ability.Context.from(entity.getMainHandItem())) && ability.value().overrideActive())
                            overrideActive.set(true);
                    });
                    if (data.mainPowerActive()) {
                        if (overrideActive.get()) {
                            entity.getData(MineraculousAttachmentTypes.MIRACULOUS).put(entity, miraculous, data.withPowerStatus(false, false), true);
                        } else {
                            boolean usedPower = entity.level().holderOrThrow(miraculous).value().activeAbility().get().value().perform(new AbilityData(data.powerLevel(), Either.left(miraculous)), player.level(), player.blockPosition(), player, Ability.Context.from(entity.getMainHandItem()));
                            if (usedPower) {
                                entity.getData(MineraculousAttachmentTypes.MIRACULOUS).put(entity, miraculous, data.withUsedPower(), true);
                                if (entity instanceof ServerPlayer serverPlayer) {
                                    MineraculousCriteriaTriggers.USED_MIRACULOUS_POWER.get().trigger(serverPlayer, miraculous, MiraculousUsePowerTrigger.Context.ITEM);
                                }
                            }
                        }
                    }
                }
                if (data.mainPowerActive()) {
                    if (overrideActive.get()) {
                        entity.getData(MineraculousAttachmentTypes.MIRACULOUS).put(entity, miraculous, data.withPowerStatus(false, false), true);
                    } else {
                        entity.level().holderOrThrow(miraculous).value().activeAbility().get().value().perform(new AbilityData(data.powerLevel(), Either.left(miraculous)), player.level(), player.blockPosition(), player, Ability.Context.PASSIVE);
                    }
                }
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
                int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAITTICKS);
                if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen() && !MineraculousClientUtils.isCameraEntityOther()) {
                    if (MineraculousKeyMappings.TRANSFORM.get().isDown()) {
                        if (data.transformed()) {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculous, data, false, false));
                        } else {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculous, data, true, false));
                        }
                        playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
                    } else if (MineraculousKeyMappings.ACTIVATE_POWER.get().isDown() && data.transformed() && !data.mainPowerActive() && !data.shouldCountDown() && slotContext.entity().level().holderOrThrow(miraculous).value().activeAbility().isPresent()) {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundSetPowerActivatedPayload(miraculous, true, true));
                        playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
                    } else if (MineraculousKeyMappings.OPEN_TOOL_WHEEL.get().isDown() && player.getMainHandItem().isEmpty()) {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundPutToolInHandPayload(miraculous));
                        playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
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
        if (entity.level() instanceof ServerLevel && entity instanceof Player && !(stack.is(newStack.getItem()) && stack.get(MineraculousDataComponents.MIRACULOUS) == newStack.get(MineraculousDataComponents.MIRACULOUS))) {
            MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS);
            MiraculousData data = miraculousDataSet.get(stack.get(MineraculousDataComponents.MIRACULOUS));
            if (!data.transformed()) {
                data = data.unEquip();
                miraculousDataSet.put(entity, stack.get(MineraculousDataComponents.MIRACULOUS), data, true);
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
