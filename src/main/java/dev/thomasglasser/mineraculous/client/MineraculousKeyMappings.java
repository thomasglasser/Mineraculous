package dev.thomasglasser.mineraculous.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundRequestInventorySyncPayload;
import dev.thomasglasser.mineraculous.network.ServerboundToggleActivePayload;
import dev.thomasglasser.mineraculous.network.ServerboundTryBreakItemPayload;
import dev.thomasglasser.mineraculous.network.ServerboundWakeUpPayload;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.item.RadialMenuProvider;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.client.ExtendedKeyMapping;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MineraculousKeyMappings {
    public static final String MIRACULOUS_CATEGORY = "key.categories.mineraculous";

    // TODO: Fix
    public static final ExtendedKeyMapping TRANSFORM = register("transform", InputConstants.KEY_M, MIRACULOUS_CATEGORY, () -> {});
    public static final ExtendedKeyMapping ACTIVATE_POWER = register("activate_power", InputConstants.KEY_O, MIRACULOUS_CATEGORY, () -> {});
    public static final ExtendedKeyMapping TOGGLE_ACTIVE = register("toggle_active", InputConstants.KEY_I, MIRACULOUS_CATEGORY, MineraculousKeyMappings::handleToggleActive);
    public static final ExtendedKeyMapping CONFIGURE_TOOL = register("configure_tool", InputConstants.KEY_C, MIRACULOUS_CATEGORY, MineraculousKeyMappings::handleConfigureTool);
    public static final ExtendedKeyMapping TAKE_BREAK_ITEM = register("take_break_item", InputConstants.KEY_B, KeyMapping.CATEGORY_GAMEPLAY, MineraculousKeyMappings::handleTakeBreakItem, MineraculousKeyMappings::handleNoTakeBreakItem);
    public static final ExtendedKeyMapping UNWIND_YOYO = register("unwind_yoyo", InputConstants.KEY_DOWN, KeyMapping.CATEGORY_MOVEMENT, () -> {});
    public static final ExtendedKeyMapping WIND_YOYO = register("wind_yoyo", InputConstants.KEY_UP, KeyMapping.CATEGORY_MOVEMENT, () -> {});

    public static ExtendedKeyMapping register(String name, int key, String category, Runnable onClick) {
        return ClientUtils.registerKeyMapping(Mineraculous.modLoc(name), key, category, onClick);
    }

    public static ExtendedKeyMapping register(String name, int key, String category, Runnable onClick, Runnable onNoClick) {
        return ClientUtils.registerKeyMapping(Mineraculous.modLoc(name), key, category, onClick, onNoClick);
    }

    private static void handleToggleActive() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack mainHandItem = player.getMainHandItem();
            Boolean mainHandOpen = mainHandItem.get(MineraculousDataComponents.ACTIVE);
            if (mainHandOpen != null) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundToggleActivePayload(InteractionHand.MAIN_HAND));
            } else {
                ItemStack offhandItem = player.getOffhandItem();
                Boolean offHandOpen = offhandItem.get(MineraculousDataComponents.ACTIVE);
                if (offHandOpen != null) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundToggleActivePayload(InteractionHand.OFF_HAND));
                }
            }
        }
    }

    private static void handleConfigureTool() {
        Player player = ClientUtils.getLocalPlayer();
        if (player != null) {
            InteractionHand hand = InteractionHand.MAIN_HAND;
            ItemStack mainHandStack = player.getMainHandItem();
            if (mainHandStack.getItem() instanceof RadialMenuProvider<?> provider) {
                if (MineraculousClientEvents.tryOpenToolWheel(hand, mainHandStack, provider)) {
                    return;
                } else if (provider.handleSecondaryKeyBehavior(mainHandStack, hand)) {
                    return;
                }
            }
            hand = InteractionHand.OFF_HAND;
            ItemStack offHandStack = player.getOffhandItem();
            if (offHandStack.getItem() instanceof RadialMenuProvider<?> provider) {
                if (!MineraculousClientEvents.tryOpenToolWheel(hand, offHandStack, provider)) {
                    provider.handleSecondaryKeyBehavior(offHandStack, hand);
                }
            }
        }
    }

    private static int takeTicks = 0;

    public static int getTakeTicks() {
        return takeTicks;
    }

    private static void handleTakeBreakItem() {
        Player player = Minecraft.getInstance().player;
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.isEmpty()) {
            if (MineraculousClientUtils.getLookEntity() instanceof Player target && (MineraculousServerConfig.get().enableUniversalStealing.get() || player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || player.getData(MineraculousAttachmentTypes.MIRACULOUS.get()).isTransformed()) && (MineraculousServerConfig.get().enableSleepStealing.get() || !target.isSleeping())) {
                takeTicks++;
                if (target.isSleeping() && MineraculousServerConfig.get().wakeUpChance.get() > 0 && (MineraculousServerConfig.get().wakeUpChance.get() >= 100 || player.getRandom().nextFloat() < MineraculousServerConfig.get().wakeUpChance.get() / (20f * 5 * 100))) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundWakeUpPayload(target.getUUID(), true));
                }
                if (takeTicks > (20 * MineraculousServerConfig.get().stealingDuration.get())) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundRequestInventorySyncPayload(target.getUUID()));
                    MineraculousClientUtils.openExternalCuriosInventoryScreen(target, player);
                    takeTicks = 0;
                }
            } else if (takeTicks > 0) {
                takeTicks = 0;
            }
        } else {
            TommyLibServices.NETWORK.sendToServer(ServerboundTryBreakItemPayload.INSTANCE);
        }
    }

    private static void handleNoTakeBreakItem() {
        takeTicks = 0;
    }

    public static void init() {}
}
