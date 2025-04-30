package dev.thomasglasser.mineraculous.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.world.item.ToolWheelProvider;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.client.ExtendedKeyMapping;
import dev.thomasglasser.tommylib.api.network.BidirectionalSyncDataComponentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MineraculousKeyMappings {
    public static final String MIRACULOUS_CATEGORY = "key.categories.mineraculous";

    // TODO: Fix
    public static final ExtendedKeyMapping TRANSFORM = register("transform", InputConstants.KEY_M, MIRACULOUS_CATEGORY, () -> {});
    public static final ExtendedKeyMapping ACTIVATE_POWER = register("activate_power", InputConstants.KEY_O, MIRACULOUS_CATEGORY, () -> {});
    public static final ExtendedKeyMapping TOGGLE_ACTIVE = register("toggle_active", InputConstants.KEY_C, MIRACULOUS_CATEGORY, () -> {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack mainHandItem = player.getMainHandItem();
            Boolean mainHandOpen = mainHandItem.get(MineraculousDataComponents.ACTIVE);
            if (mainHandOpen != null) {
                mainHandItem.set(MineraculousDataComponents.ACTIVE, !mainHandOpen);
                TommyLibServices.NETWORK.sendToServer(new BidirectionalSyncDataComponentPayload<>(InteractionHand.MAIN_HAND, MineraculousDataComponents.ACTIVE, !mainHandOpen));
            } else {
                ItemStack offhandItem = player.getOffhandItem();
                Boolean offHandOpen = offhandItem.get(MineraculousDataComponents.ACTIVE);
                if (offHandOpen != null) {
                    offhandItem.set(MineraculousDataComponents.ACTIVE, !offHandOpen);
                    TommyLibServices.NETWORK.sendToServer(new BidirectionalSyncDataComponentPayload<>(InteractionHand.OFF_HAND, MineraculousDataComponents.ACTIVE, !offHandOpen));
                }
            }
        }
    });
    public static final ExtendedKeyMapping TOGGLE_OPEN = register("toggle_open", InputConstants.KEY_O, KeyMapping.CATEGORY_GAMEPLAY, () -> {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack mainHandItem = player.getMainHandItem();
            Boolean mainHandOpen = mainHandItem.get(MineraculousDataComponents.OPEN);
            if (mainHandOpen != null) {
                mainHandItem.set(MineraculousDataComponents.OPEN, !mainHandOpen);
                TommyLibServices.NETWORK.sendToServer(new BidirectionalSyncDataComponentPayload<>(InteractionHand.MAIN_HAND, MineraculousDataComponents.OPEN, !mainHandOpen));
            } else {
                ItemStack offhandItem = player.getOffhandItem();
                Boolean offHandOpen = offhandItem.get(MineraculousDataComponents.OPEN);
                if (offHandOpen != null) {
                    offhandItem.set(MineraculousDataComponents.OPEN, !offHandOpen);
                    TommyLibServices.NETWORK.sendToServer(new BidirectionalSyncDataComponentPayload<>(InteractionHand.OFF_HAND, MineraculousDataComponents.OPEN, !offHandOpen));
                }
            }
        }
    });
    public static final ExtendedKeyMapping CONFIGURE_TOOL = register("configure_tool", InputConstants.KEY_C, MIRACULOUS_CATEGORY, () -> {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack mainHandItem = player.getMainHandItem();
            if (mainHandItem.getItem() instanceof ToolWheelProvider<?> provider) {
                if (provider.canOpenToolWheel(mainHandItem)) {
                    MineraculousClientEvents.openToolWheel(provider.getColor(mainHandItem, InteractionHand.MAIN_HAND), selected -> {
                        mainHandItem.set((DataComponentType) provider.getComponentType().value(), selected);
                        TommyLibServices.NETWORK.sendToServer(new BidirectionalSyncDataComponentPayload<>(InteractionHand.MAIN_HAND, provider.getComponentType(), selected));
                    }, new ReferenceArrayList<>(provider.getOptions(mainHandItem, InteractionHand.MAIN_HAND)));
                } else {
                    provider.handleSecondaryToolKeyBehavior(mainHandItem, InteractionHand.MAIN_HAND);
                }
            } else {
                ItemStack offHandItem = player.getOffhandItem();
                if (offHandItem.getItem() instanceof ToolWheelProvider<?> provider) {
                    if (provider.canOpenToolWheel(offHandItem)) {
                        MineraculousClientEvents.openToolWheel(provider.getColor(offHandItem, InteractionHand.OFF_HAND), selected -> {
                            offHandItem.set((DataComponentType) provider.getComponentType().value(), selected);
                            TommyLibServices.NETWORK.sendToServer(new BidirectionalSyncDataComponentPayload<>(InteractionHand.OFF_HAND, provider.getComponentType(), selected));
                        }, new ReferenceArrayList<>(provider.getOptions(offHandItem, InteractionHand.OFF_HAND)));
                    } else {
                        provider.handleSecondaryToolKeyBehavior(offHandItem, InteractionHand.OFF_HAND);
                    }
                }
            }
        }
    });
    public static final ExtendedKeyMapping TAKE_BREAK_ITEM = register("take_break_item", InputConstants.KEY_I, KeyMapping.CATEGORY_GAMEPLAY, () -> {});
    public static final ExtendedKeyMapping UNWIND_YOYO = register("unwind_yoyo", InputConstants.KEY_DOWN, KeyMapping.CATEGORY_MOVEMENT, () -> {});
    public static final ExtendedKeyMapping WIND_YOYO = register("wind_yoyo", InputConstants.KEY_UP, KeyMapping.CATEGORY_MOVEMENT, () -> {});

    public static ExtendedKeyMapping register(String name, int key, String category, Runnable onClick) {
        return ClientUtils.registerKeyMapping(Mineraculous.modLoc(name), key, category, onClick);
    }

    public static void init() {}
}
