package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.tags.MineraculousDamageTypeTags;
import dev.thomasglasser.mineraculous.world.entity.Kwami;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.client.renderer.BewlrProvider;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.Level;

// TODO: Update Curios
public class MiraculousItem extends Item implements /*ICurioItem,*/ ModeledItem {
    public static final int FIVE_MINUTES = 6000;

    public MiraculousItem(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.EPIC)
                .component(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                .component(DataComponents.DAMAGE_RESISTANT, new DamageResistant(MineraculousDamageTypeTags.RESISTED_BY_MIRACULOUS)));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (stack.has(MineraculousDataComponents.MIRACULOUS))
            tooltipComponents.add(Component.translatable(stack.get(MineraculousDataComponents.MIRACULOUS).location().toLanguageKey(stack.get(MineraculousDataComponents.MIRACULOUS).registry().getPath())).withStyle(ChatFormatting.GRAY));
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

//    @Override
//    public void curioTick(SlotContext slotContext, ItemStack stack) {
//        LivingEntity entity = slotContext.entity();
//        if (entity instanceof Player player && stack.has(MineraculousDataComponents.MIRACULOUS) && player.level().holderOrThrow(stack.get(MineraculousDataComponents.MIRACULOUS)).value().acceptableSlot().equals(slotContext.identifier())) {
//            ResourceKey<Miraculous> miraculousType = stack.get(MineraculousDataComponents.MIRACULOUS);
//            MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(miraculousType);
//            if (data.transformed()) {
//                if (data.mainPowerActivated())
//                    stack.set(MineraculousDataComponents.REMAINING_TICKS.get(), stack.getOrDefault(MineraculousDataComponents.REMAINING_TICKS.get(), 0) - 1);
//                entity.level().holderOrThrow(miraculousType).value().passiveAbilities().forEach(ability -> ability.value().perform(miraculousType, data, player.level(), player.blockPosition(), player, Ability.Context.PASSIVE));
//                if (data.mainPowerActive())
//                    entity.level().holderOrThrow(miraculousType).value().activeAbility().value().perform(miraculousType, data, player.level(), player.blockPosition(), player, Ability.Context.PASSIVE);
//                if (!entity.getMainHandItem().isEmpty()) {
//                    entity.level().holderOrThrow(miraculousType).value().passiveAbilities().forEach(ability -> ability.value().perform(miraculousType, data, player.level(), player.blockPosition(), player, Ability.Context.from(entity.getMainHandItem())));
//                    if (data.mainPowerActive()) {
//                        boolean usedPower = entity.level().holderOrThrow(miraculousType).value().activeAbility().value().perform(miraculousType, data, player.level(), player.blockPosition(), player, Ability.Context.from(entity.getMainHandItem()));
//                        if (usedPower)
//                            entity.getData(MineraculousAttachmentTypes.MIRACULOUS).put(entity, miraculousType, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), data.powerLevel(), true, false, data.name(), data.look()), true);
//                    }
//                }
//            }
//            if (entity.level().isClientSide) {
//                CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(entity);
//                int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAITTICKS);
//                if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen()) {
//                    if (MineraculousKeyMappings.TRANSFORM.isDown()) {
//                        if (data.transformed()) {
//                            TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculousType, data, false));
//                        } else {
//                            TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculousType, data, true));
//                        }
//                        playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
//                    } else if (MineraculousKeyMappings.ACTIVATE_POWER.isDown() && data.transformed() && !data.mainPowerActive() && !data.mainPowerActivated()) {
//                        TommyLibServices.NETWORK.sendToServer(new ServerboundActivatePowerPayload(miraculousType));
//                        playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
//                    }
//                }
//                TommyLibServices.ENTITY.setPersistentData(entity, playerData, false);
//            } else {
//                if (data.mainPowerActivated() && stack.getOrDefault(MineraculousDataComponents.REMAINING_TICKS.get(), 0) <= 0) {
//                    MineraculousEntityEvents.handleTransformation((ServerPlayer) player, miraculousType, data, false);
//                }
//            }
//        }
//
//        stack.inventoryTick(entity.level(), entity, -1, false);
//    }
//
//    @Override
//    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
//        LivingEntity entity = slotContext.entity();
//        if (!entity.level().isClientSide && entity instanceof Player player) {
//            MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS.get());
//            MiraculousData data = miraculousDataSet.get(stack.get(MineraculousDataComponents.MIRACULOUS));
//            if (stack.has(MineraculousDataComponents.POWERED.get()) && !data.transformed()) {
//                stack.remove(MineraculousDataComponents.POWERED.get());
//                data = new MiraculousData(false, stack, new CuriosData(slotContext.index(), slotContext.identifier()), data.tool(), data.powerLevel(), data.mainPowerActivated(), data.mainPowerActive(), data.name(), data.look());
//                MineraculousEntityEvents.summonKwami(entity.level(), stack.get(MineraculousDataComponents.MIRACULOUS), data, player);
//            }
//        }
//    }
//
//    @Override
//    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
//        return true;
//    }
}
