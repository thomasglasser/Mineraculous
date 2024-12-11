package dev.thomasglasser.mineraculous.world.item;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundMiraculousTransformPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetPowerActivatedPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kwami;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.miraculous.ability.Ability;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.level.storage.AbilityData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.client.renderer.BewlrProvider;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.item.BaseModeledItem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class MiraculousItem extends BaseModeledItem implements ICurioItem {
    public static final int FIVE_MINUTES = 6000;

    public MiraculousItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC)
                .component(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                .fireResistant());
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
        if (!level.isClientSide) {
            if (entity instanceof Player player && (!stack.has(DataComponents.PROFILE) || !stack.get(DataComponents.PROFILE).gameProfile().equals(player.getGameProfile()))) {
                stack.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));
            }
            if (!stack.has(MineraculousDataComponents.POWERED.get()) && !stack.has(MineraculousDataComponents.KWAMI_DATA.get())) {
                stack.set(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE);
            }
        }
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (!player.level().isClientSide && usedHand == InteractionHand.MAIN_HAND && interactionTarget instanceof Kwami kwami) {
            if (MineraculousEntityEvents.renounceMiraculous(stack, kwami))
                return InteractionResult.SUCCESS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void createBewlrProvider(Consumer<BewlrProvider> provider) {
        provider.accept(new BewlrProvider() {
            @Override
            public BlockEntityWithoutLevelRenderer getBewlr() {
                return MineraculousClientUtils.getBewlr();
            }
        });
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
        if (entity instanceof Player player && stack.has(MineraculousDataComponents.MIRACULOUS) && player.level().holderOrThrow(stack.get(MineraculousDataComponents.MIRACULOUS)).value().acceptableSlot().equals(slotContext.identifier())) {
            ResourceKey<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
            MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(miraculous);
            if (data.transformed()) {
                if (data.mainPowerActivated())
                    stack.set(MineraculousDataComponents.REMAINING_TICKS.get(), stack.getOrDefault(MineraculousDataComponents.REMAINING_TICKS.get(), 0) - 1);
                entity.level().holderOrThrow(miraculous).value().passiveAbilities().forEach(ability -> ability.value().perform(new AbilityData(data.powerLevel(), Either.left(miraculous)), player.level(), player.blockPosition(), player, Ability.Context.PASSIVE));
                if (data.mainPowerActive())
                    entity.level().holderOrThrow(miraculous).value().activeAbility().get().value().perform(new AbilityData(data.powerLevel(), Either.left(miraculous)), player.level(), player.blockPosition(), player, Ability.Context.PASSIVE);
                if (!entity.getMainHandItem().isEmpty()) {
                    entity.level().holderOrThrow(miraculous).value().passiveAbilities().forEach(ability -> ability.value().perform(new AbilityData(data.powerLevel(), Either.left(miraculous)), player.level(), player.blockPosition(), player, Ability.Context.from(entity.getMainHandItem())));
                    if (data.mainPowerActive()) {
                        boolean usedPower = entity.level().holderOrThrow(miraculous).value().activeAbility().get().value().perform(new AbilityData(data.powerLevel(), Either.left(miraculous)), player.level(), player.blockPosition(), player, Ability.Context.from(entity.getMainHandItem()));
                        if (usedPower)
                            entity.getData(MineraculousAttachmentTypes.MIRACULOUS).put(entity, miraculous, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), data.powerLevel(), true, false, data.name()), true);
                    }
                }
            }
            if (entity.level().isClientSide) {
                CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(entity);
                int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAITTICKS);
                if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen()) {
                    if (MineraculousKeyMappings.TRANSFORM.get().isDown()) {
                        if (data.transformed()) {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculous, data, false));
                        } else {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculous, data, true));
                        }
                        playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
                    } else if (MineraculousClientUtils.getCameraEntity() == player && MineraculousKeyMappings.ACTIVATE_POWER.get().isDown() && data.transformed() && !data.mainPowerActive() && !data.mainPowerActivated() && slotContext.entity().level().holderOrThrow(miraculous).value().activeAbility().isPresent()) {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundSetPowerActivatedPayload(miraculous, true, true));
                        playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
                    }
                }
                TommyLibServices.ENTITY.setPersistentData(entity, playerData, false);
            } else {
                if (data.mainPowerActivated() && stack.getOrDefault(MineraculousDataComponents.REMAINING_TICKS.get(), 0) <= 0) {
                    MineraculousEntityEvents.handleMiraculousTransformation((ServerPlayer) player, miraculous, data, false);
                }
            }
        }

        stack.inventoryTick(entity.level(), entity, -1, false);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level().isClientSide && entity instanceof Player player) {
            MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS.get());
            MiraculousData data = miraculousDataSet.get(stack.get(MineraculousDataComponents.MIRACULOUS));
            if (stack.has(MineraculousDataComponents.POWERED.get()) && !data.transformed()) {
                stack.remove(MineraculousDataComponents.POWERED.get());
                data = new MiraculousData(false, stack, new CuriosData(slotContext.index(), slotContext.identifier()), data.tool(), data.powerLevel(), data.mainPowerActivated(), data.mainPowerActive(), data.name());
                MineraculousEntityEvents.summonKwami(entity.level(), stack.get(MineraculousDataComponents.MIRACULOUS), data, player);
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
}
