package dev.thomasglasser.mineraculous.impl.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.api.client.gui.screens.RadialMenuScreen;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosUtils;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.api.world.item.RadialMenuProvider;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.impl.network.ServerboundMiraculousTransformPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundPutMiraculousToolInHandPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundRenounceMiraculousPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetKamikotizationPowerActivatedPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetMiraculousPowerActivatedPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundToggleActivePayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundTryBreakItemPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundUpdateYoyoLengthPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundWakeUpPayload;
import dev.thomasglasser.mineraculous.impl.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.impl.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.client.ExtendedKeyMapping;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MineraculousKeyMappings {
    public static final String MIRACULOUS_CATEGORY = "key.categories.mineraculous";

    public static final ExtendedKeyMapping TRANSFORM = register("transform", InputConstants.KEY_M, MIRACULOUS_CATEGORY, MineraculousKeyMappings::handleTransform);
    public static final ExtendedKeyMapping ACTIVATE_POWER = register("activate_power", InputConstants.KEY_O, MIRACULOUS_CATEGORY, MineraculousKeyMappings::handleActivatePower);
    public static final ExtendedKeyMapping TOGGLE_ACTIVE = register("toggle_active", InputConstants.KEY_I, MIRACULOUS_CATEGORY, MineraculousKeyMappings::handleToggleActive);
    public static final ExtendedKeyMapping OPEN_ITEM_RADIAL_MENU = register("open_item_radial_menu", InputConstants.KEY_R, MIRACULOUS_CATEGORY, MineraculousKeyMappings::handleOpenItemRadialMenu);
    public static final ExtendedKeyMapping TAKE_BREAK_ITEM = register("take_break_item", InputConstants.KEY_B, KeyMapping.CATEGORY_GAMEPLAY, MineraculousKeyMappings::handleTakeBreakItem, MineraculousKeyMappings::handleNoTakeBreakItem);
    public static final ExtendedKeyMapping UNWIND_YOYO = register("unwind_yoyo", InputConstants.KEY_DOWN, KeyMapping.CATEGORY_MOVEMENT, () -> handleUpdateYoyo(true));
    public static final ExtendedKeyMapping WIND_YOYO = register("wind_yoyo", InputConstants.KEY_UP, KeyMapping.CATEGORY_MOVEMENT, () -> handleUpdateYoyo(false));

    public static ExtendedKeyMapping register(String name, int key, String category, Runnable onClick) {
        return ClientUtils.registerKeyMapping(Mineraculous.modLoc(name), key, category, onClick);
    }

    public static ExtendedKeyMapping register(String name, int key, String category, Runnable onClick, Runnable onNoClick) {
        return ClientUtils.registerKeyMapping(Mineraculous.modLoc(name), key, category, onClick, onNoClick);
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
                List<RadialMenuOption> options = new ReferenceArrayList<>();
                Map<RadialMenuOption, Holder<Miraculous>> miraculousOptions = new Reference2ReferenceOpenHashMap<>();
                Map<Holder<Miraculous>, ItemStack> miraculousStacks = new Reference2ReferenceOpenHashMap<>();
                for (ItemStack stack : CuriosUtils.getAllItems(player).values()) {
                    Holder<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
                    if (miraculous != null) {
                        RadialMenuOption option = new RadialMenuOption() {
                            private Integer color;

                            @Override
                            public String translationKey() {
                                return Miraculous.toLanguageKey(miraculous.getKey());
                            }

                            @Override
                            public Integer colorOverride() {
                                if (color == null)
                                    color = miraculous.value().color().getValue();
                                return color;
                            }
                        };
                        options.add(option);
                        miraculousOptions.put(option, miraculous);
                        miraculousStacks.put(miraculous, stack);
                    }
                }
                if (options.size() > 1) {
                    Minecraft.getInstance().setScreen(new RadialMenuScreen<>(TRANSFORM.getKey().getValue(), options, -1, (selected, i) -> {
                        Holder<Miraculous> miraculous = miraculousOptions.get(selected);
                        if (miraculous != null) {
                            ItemStack stack = miraculousStacks.get(miraculous);
                            MiraculousData data = miraculousesData.get(miraculous);
                            if (data != null) {
                                KwamiData kwamiData = stack.get(MineraculousDataComponents.KWAMI_DATA);
                                if (kwamiData != null) {
                                    if (kwamiData.charged()) {
                                        TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculous, data, true));
                                    } else {
                                        Kwami kwami = player.level().getEntity(kwamiData.id()) instanceof Kwami k ? k : null;
                                        if (kwami != null) {
                                            kwami.playHurtSound(player.level().damageSources().starve());
                                        }
                                    }
                                }
                            }
                        }
                    }));
                } else if (options.size() == 1) {
                    Holder<Miraculous> miraculous = miraculousOptions.get(options.getFirst());
                    TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculous, miraculousesData.get(miraculous), true));
                }
            }
        }
    }

    private static void handleActivatePower() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            MiraculousesData miraculousesData = player.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            List<Holder<Miraculous>> transformed = miraculousesData.getTransformed();
            if (!transformed.isEmpty()) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetMiraculousPowerActivatedPayload(transformed.getFirst()));
            } else if (player.getMainHandItem().is(MineraculousItems.MIRACULOUS)) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundRenounceMiraculousPayload(InteractionHand.MAIN_HAND));
            } else if (player.getOffhandItem().is(MineraculousItems.MIRACULOUS)) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundRenounceMiraculousPayload(InteractionHand.OFF_HAND));
            } else if (player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isPresent()) {
                TommyLibServices.NETWORK.sendToServer(ServerboundSetKamikotizationPowerActivatedPayload.INSTANCE);
            }
        }
    }

    private static void handleToggleActive() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            if (player.getMainHandItem().has(MineraculousDataComponents.ACTIVE)) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundToggleActivePayload(InteractionHand.MAIN_HAND));
            } else if (player.getOffhandItem().has(MineraculousDataComponents.ACTIVE)) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundToggleActivePayload(InteractionHand.OFF_HAND));
            }
        }
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
                    if (target.isSleeping() && MineraculousServerConfig.get().wakeUpChance.get() > 0 && (MineraculousServerConfig.get().wakeUpChance.get() >= 100 || player.getRandom().nextFloat() < MineraculousServerConfig.get().wakeUpChance.get() / (20f * 5 * 100))) {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundWakeUpPayload(target.getUUID(), true));
                    }
                    if (takeTicks > (20 * MineraculousServerConfig.get().stealingDuration.get())) {
                        MineraculousClientUtils.openExternalCuriosInventoryScreen(target);
                        takeTicks = 0;
                    }
                } else if (takeTicks > 0) {
                    takeTicks = 0;
                }
            } else if (player.tickCount % 10 == 0) {
                // Holding the key down causes some issues with stack miscounts, so we slow down breaking
                TommyLibServices.NETWORK.sendToServer(ServerboundTryBreakItemPayload.INSTANCE);
            }
        }
    }

    private static void handleNoTakeBreakItem() {
        takeTicks = 0;
    }

    private static void handleUpdateYoyo(boolean increase) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            ThrownLadybugYoyoData data = player.getData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO);
            ThrownLadybugYoyo thrownYoyo = data.getThrownYoyo(player.level());
            if (thrownYoyo != null) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundUpdateYoyoLengthPayload(increase));
            }
        }
    }

    public static void init() {}
}
