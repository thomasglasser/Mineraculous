package dev.thomasglasser.mineraculous.impl.client;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuScreen;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.item.RadialMenuProvider;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.client.gui.MineraculousGuis;
import dev.thomasglasser.mineraculous.impl.network.ServerboundActivatePowerPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundMiraculousTransformPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundPutMiraculousToolInHandPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundRenounceMiraculousPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundStartKamikotizationDetransformationPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundToggleActivePayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundToggleBuffsPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundToggleNightVisionPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundTryBreakItemPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundUpdateYoyoLengthPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundWakeUpPayload;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.client.ExtendedKeyMapping;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.SharedConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MineraculousKeyMappings {
    public static final String MIRACULOUS_CATEGORY = "key.categories.mineraculous";

    public static final ExtendedKeyMapping TRANSFORM = register("transform", InputConstants.KEY_U, MIRACULOUS_CATEGORY, MineraculousKeyMappings::handleTransform);
    public static final ExtendedKeyMapping QUICK_TRANSFORM = register("quick_transform", InputConstants.KEY_I, MIRACULOUS_CATEGORY, MineraculousKeyMappings::handleQuickTransform);
    public static final ExtendedKeyMapping ACTIVATE_POWER = register("activate_power", InputConstants.KEY_Y, MIRACULOUS_CATEGORY, MineraculousKeyMappings::handleActivatePower);
    public static final ExtendedKeyMapping REVOKE_KAMIKOTIZATION = register("revoke_kamikotization", InputConstants.KEY_K, MIRACULOUS_CATEGORY, MineraculousKeyMappings::handleRevokeKamikotization);
    public static final ExtendedKeyMapping RENOUNCE_MIRACULOUS = register("renounce_miraculous", InputConstants.KEY_N, MIRACULOUS_CATEGORY, MineraculousKeyMappings::handleRenounceMiraculous);
    public static final ExtendedKeyMapping TOGGLE_BUFFS = register("toggle_buffs", InputConstants.KEY_GRAVE, MIRACULOUS_CATEGORY, MineraculousKeyMappings::handleToggleBuffs);
    public static final ExtendedKeyMapping TOGGLE_ITEM_ACTIVE = register("toggle_item_active", InputConstants.KEY_C, KeyMapping.CATEGORY_GAMEPLAY, MineraculousKeyMappings::handleToggleActive);
    public static final ExtendedKeyMapping OPEN_ITEM_RADIAL_MENU = register("open_item_radial_menu", InputConstants.KEY_R, KeyMapping.CATEGORY_GAMEPLAY, MineraculousKeyMappings::handleOpenItemRadialMenu);
    public static final ExtendedKeyMapping TAKE_BREAK_ITEM = register("take_break_item", InputConstants.KEY_B, KeyMapping.CATEGORY_GAMEPLAY, MineraculousKeyMappings::handleTakeBreakItem, MineraculousKeyMappings::handleNoTakeBreakItem);
    public static final ExtendedKeyMapping TOGGLE_NIGHT_VISION = register("toggle_night_vision", InputConstants.KEY_V, KeyMapping.CATEGORY_GAMEPLAY, MineraculousKeyMappings::handleToggleNightVision);
    public static final ExtendedKeyMapping DESCEND_TOOL = register("descend_tool", InputConstants.KEY_Z, KeyMapping.CATEGORY_MOVEMENT, () -> handleUpdateToolMovements(true));
    public static final ExtendedKeyMapping ASCEND_TOOL = register("ascend_tool", InputConstants.KEY_X, KeyMapping.CATEGORY_MOVEMENT, () -> handleUpdateToolMovements(false));

    public static ExtendedKeyMapping register(String name, int key, String category, Runnable onClick) {
        return ClientUtils.registerKeyMapping(MineraculousConstants.modLoc(name), key, category, onClick);
    }

    public static ExtendedKeyMapping register(String name, int key, String category, Runnable onClick, Runnable onNoClick) {
        return ClientUtils.registerKeyMapping(MineraculousConstants.modLoc(name), key, category, onClick, onNoClick);
    }

    private static void handleTransform() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            MiraculousesData miraculousesData = player.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            List<Holder<Miraculous>> transformed = miraculousesData.getTransformed();
            if (!transformed.isEmpty()) {
                Holder<Miraculous> miraculous = transformed.getFirst();
                TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculous, miraculousesData.get(miraculous), false));
            } else {
                selectMiraculous(miraculousesData, TRANSFORM);
            }
        }
    }

    private static void handleQuickTransform() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            MiraculousesData miraculousesData = player.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            List<Holder<Miraculous>> transformed = miraculousesData.getTransformed();
            if (!transformed.isEmpty()) {
                Holder<Miraculous> miraculous = transformed.getFirst();
                TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculous, miraculousesData.get(miraculous), false));
            } else if (miraculousesData.getLastUsed().isPresent()) {
                Holder<Miraculous> lastTransformed = miraculousesData.getLastUsed().get();
                TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(lastTransformed, miraculousesData.get(lastTransformed), true));
            } else {
                selectMiraculous(miraculousesData, QUICK_TRANSFORM);
            }
        }
    }

    private static void selectMiraculous(MiraculousesData data, ExtendedKeyMapping keyMapping) {
        ImmutableList.Builder<RadialMenuOption> optionsBuilder = new ImmutableList.Builder<>();
        Map<RadialMenuOption, Holder<Miraculous>> miraculousOptions = new Reference2ReferenceOpenHashMap<>();
        for (ItemStack stack : CuriosUtils.getAllItems(ClientUtils.getLocalPlayer()).values()) {
            Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
            if (miraculous != null) {
                ResourceKey<Miraculous> key = miraculous.getKey();
                if (key != null) {
                    RadialMenuOption option = new RadialMenuOption() {
                        private Integer color;

                        @Override
                        public Component displayName() {
                            return Component.translatable(MineraculousConstants.toLanguageKey(key));
                        }

                        @Override
                        public Integer colorOverride() {
                            if (color == null)
                                color = miraculous.value().color().getValue();
                            return color;
                        }
                    };
                    optionsBuilder.add(option);
                    miraculousOptions.put(option, miraculous);
                }
            }
        }
        ImmutableList<RadialMenuOption> options = optionsBuilder.build();
        if (options.size() > 1) {
            Minecraft.getInstance().setScreen(new RadialMenuScreen<>(keyMapping.getKey().getValue(), options, -1, (selected, i) -> {
                Holder<Miraculous> miraculous = miraculousOptions.get(selected);
                TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculous, data.get(miraculous), true));
            }));
        } else if (options.size() == 1) {
            Holder<Miraculous> miraculous = miraculousOptions.get(options.getFirst());
            TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculous, data.get(miraculous), true));
        }
    }

    private static void handleActivatePower() {
        TommyLibServices.NETWORK.sendToServer(ServerboundActivatePowerPayload.INSTANCE);
    }

    private static void handleRevokeKamikotization() {
        if (MineraculousGuis.checkRevokeButtonActive()) {
            Button revokeButton = MineraculousGuis.getRevokeButton();
            if (revokeButton.active) {
                revokeButton.onPress();
            }
        } else {
            Player player = ClientUtils.getLocalPlayer();
            if (player != null && player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION.get()).isPresent() && MineraculousServerConfig.get().enableKamikotizationRejection.get()) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundStartKamikotizationDetransformationPayload(Optional.empty(), true, false));
            }
        }
    }

    private static void handleRenounceMiraculous() {
        TommyLibServices.NETWORK.sendToServer(ServerboundRenounceMiraculousPayload.INSTANCE);
    }

    private static void handleToggleBuffs() {
        TommyLibServices.NETWORK.sendToServer(ServerboundToggleBuffsPayload.INSTANCE);
    }

    private static void handleToggleActive() {
        TommyLibServices.NETWORK.sendToServer(ServerboundToggleActivePayload.INSTANCE);
    }

    private static void handleOpenItemRadialMenu() {
        Player player = ClientUtils.getLocalPlayer();
        if (player != null) {
            InteractionHand hand = InteractionHand.MAIN_HAND;
            ItemStack mainHandStack = player.getMainHandItem();
            if (mainHandStack.getItem() instanceof RadialMenuProvider<?> provider) {
                if (MineraculousClientUtils.tryOpenRadialMenuScreenFromProvider(hand, mainHandStack, provider)) {
                    return;
                } else if (provider.handleSecondaryKeyBehavior(mainHandStack, hand, player)) {
                    return;
                }
            }
            hand = InteractionHand.OFF_HAND;
            ItemStack offHandStack = player.getOffhandItem();
            if (offHandStack.getItem() instanceof RadialMenuProvider<?> provider) {
                if (!MineraculousClientUtils.tryOpenRadialMenuScreenFromProvider(hand, offHandStack, provider)) {
                    provider.handleSecondaryKeyBehavior(offHandStack, hand, player);
                }
                return;
            }
            MiraculousesData miraculousesData = player.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            List<Holder<Miraculous>> transformed = miraculousesData.getTransformed();
            if (!transformed.isEmpty()) {
                Holder<Miraculous> miraculous = transformed.getFirst();
                if (player.getMainHandItem().isEmpty()) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundPutMiraculousToolInHandPayload(miraculous));
                }
            }
        }
    }

    private static int takeTicks = 0;
    private static int breakCooldown = 0;

    public static int getTakeTicks() {
        return takeTicks;
    }

    private static void handleTakeBreakItem() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ItemStack mainHandItem = player.getMainHandItem();
            if (mainHandItem.isEmpty()) {
                if (MineraculousClientUtils.getLookEntity() instanceof Player target && (MineraculousServerConfig.get().enableUniversalStealing.get() || player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent() || player.getData(MineraculousAttachmentTypes.MIRACULOUSES.get()).isTransformed()) && (MineraculousServerConfig.get().enableSleepStealing.get() || !target.isSleeping())) {
                    takeTicks++;
                    if (target.isSleeping() && MineraculousServerConfig.get().wakeUpChance.get() > 0 && (MineraculousServerConfig.get().wakeUpChance.get() >= 100 || player.getRandom().nextFloat() < MineraculousServerConfig.get().wakeUpChance.get() / (SharedConstants.TICKS_PER_SECOND * MineraculousServerConfig.get().stealingDuration.getAsInt() * 100F))) {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundWakeUpPayload(target.getUUID(), true));
                    }
                    if (takeTicks > (SharedConstants.TICKS_PER_SECOND * MineraculousServerConfig.get().stealingDuration.get())) {
                        MineraculousClientUtils.openExternalCuriosInventoryScreenForStealing(target);
                        takeTicks = 0;
                    }
                } else if (takeTicks > 0) {
                    takeTicks = 0;
                }
            } else if (breakCooldown <= 0) {
                TommyLibServices.NETWORK.sendToServer(ServerboundTryBreakItemPayload.INSTANCE);
                breakCooldown = 5;
            }
        }
        if (breakCooldown > 0)
            breakCooldown--;
    }

    private static void handleNoTakeBreakItem() {
        takeTicks = 0;
        if (breakCooldown > 0)
            breakCooldown--;
    }

    private static void handleToggleNightVision() {
        TommyLibServices.NETWORK.sendToServer(ServerboundToggleNightVisionPayload.INSTANCE);
    }

    private static void handleUpdateToolMovements(boolean ascend) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO).id().isPresent() || player.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO).isPresent()) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundUpdateYoyoLengthPayload(ascend));
            }
        }
    }

    public static void init() {}
}
