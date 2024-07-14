package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.client.MineraculousClientEvents;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundActivatePowerPayload;
import dev.thomasglasser.mineraculous.network.ServerboundMiraculousTransformPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.item.BaseModeledItem;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import java.util.function.Supplier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class MiraculousItem extends BaseModeledItem implements ICurioItem {
    public static final int FIVE_MINUTES = 6000;

    private final ArmorSet armor;
    private final Supplier<? extends Item> tool;
    private final SoundEvent transformSound;
    private final Supplier<EntityType<? extends Kwami>> kwamiType;
    private final String acceptableSlot;
    private final TextColor powerColor;
    private final MiraculousType type;

    public MiraculousItem(Properties properties, MiraculousType type, ArmorSet armor, Supplier<? extends Item> tool, SoundEvent transformSound, Supplier<EntityType<? extends Kwami>> kwamiType, String acceptableSlot, TextColor powerColor) {
        super(properties.stacksTo(1).fireResistant().rarity(Rarity.EPIC)
                .component(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true)));
        this.armor = armor;
        this.tool = tool;
        this.transformSound = transformSound;
        this.kwamiType = kwamiType;
        this.acceptableSlot = acceptableSlot;
        this.powerColor = powerColor;
        this.type = type;
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
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public BlockEntityWithoutLevelRenderer getBEWLR() {
        return MineraculousClientUtils.getBewlr();
    }

    public Item getTool() {
        return tool == null ? null : tool.get();
    }

    public ArmorSet getArmorSet() {
        return armor;
    }

    public EntityType<? extends Kwami> getKwamiType() {
        return kwamiType.get();
    }

    public String getAcceptableSlot() {
        return acceptableSlot;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    public TextColor getPowerColor() {
        return powerColor;
    }

    public MiraculousType getType() {
        return type;
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (entity instanceof Player player && stack.getItem() instanceof MiraculousItem miraculousItem && miraculousItem.getAcceptableSlot().equals(slotContext.identifier())) {
            MiraculousType miraculousType = miraculousItem.getType();
            MiraculousData data = player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).get(miraculousType);
            if (data.mainPowerActivated())
                stack.set(MineraculousDataComponents.REMAINING_TICKS.get(), stack.getOrDefault(MineraculousDataComponents.REMAINING_TICKS.get(), 0) - 1);
            if (entity.level().isClientSide) {
                CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(entity);
                int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAITTICKS);
                if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen()) {
                    if (MineraculousKeyMappings.OPEN_ABILITY_WHEEL.isDown() && data.transformed()) {
                        MineraculousClientEvents.openPowerWheel(player);
                        playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
                    } else if (MineraculousKeyMappings.TRANSFORM.isDown()) {
                        if (data.transformed()) {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculousType, data, false));
                        } else {
                            TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculousType, data, true));
                        }
                        playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
                    } else if (MineraculousKeyMappings.ACTIVATE_POWER.isDown() && data.transformed() && !data.mainPowerActive() && !data.mainPowerActivated()) {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundActivatePowerPayload(miraculousType));
                        playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
                    }
                }
                TommyLibServices.ENTITY.setPersistentData(entity, playerData, false);
            } else {
                if (data.mainPowerActivated() && stack.getOrDefault(MineraculousDataComponents.REMAINING_TICKS.get(), 0) <= 0) {
                    MineraculousEntityEvents.handleTransformation(player, miraculousType, data, false);
                }
            }
        }

        stack.inventoryTick(entity.level(), entity, -1, false);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level().isClientSide && entity instanceof Player player && stack.getItem() instanceof MiraculousItem miraculousItem) {
            MiraculousDataSet miraculousDataSet = entity.getData(MineraculousAttachmentTypes.MIRACULOUS.get());
            MiraculousData data = miraculousDataSet.get(miraculousItem.getType());
            if (stack.has(MineraculousDataComponents.POWERED.get()) && !data.transformed()) {
                stack.remove(MineraculousDataComponents.POWERED.get());
                data = new MiraculousData(false, stack, new CuriosData(slotContext.index(), slotContext.identifier()), data.tool(), data.powerLevel(), data.mainPowerActivated(), data.mainPowerActive(), data.name());
                MineraculousEntityEvents.summonKwami(entity.level(), miraculousItem.getType(), data, player);
            }
        }
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
