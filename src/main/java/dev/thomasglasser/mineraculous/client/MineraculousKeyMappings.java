package dev.thomasglasser.mineraculous.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuOption;
import dev.thomasglasser.mineraculous.client.gui.screens.RadialMenuScreen;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundMiraculousTransformPayload;
import dev.thomasglasser.mineraculous.network.ServerboundPutMiraculousToolInHandPayload;
import dev.thomasglasser.mineraculous.network.ServerboundRequestInventorySyncPayload;
import dev.thomasglasser.mineraculous.network.ServerboundSetMiraculousPowerActivatedPayload;
import dev.thomasglasser.mineraculous.network.ServerboundToggleActivePayload;
import dev.thomasglasser.mineraculous.network.ServerboundTryBreakItemPayload;
import dev.thomasglasser.mineraculous.network.ServerboundUpdateYoyoLengthPayload;
import dev.thomasglasser.mineraculous.network.ServerboundWakeUpPayload;
import dev.thomasglasser.mineraculous.server.MineraculousServerConfig;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kwami;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.mineraculous.world.item.RadialMenuProvider;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosUtils;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousesData;
import dev.thomasglasser.mineraculous.world.level.storage.ThrownLadybugYoyoData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.client.ExtendedKeyMapping;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
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
            List<ResourceKey<Miraculous>> transformed = miraculousesData.getTransformed();
            if (!transformed.isEmpty()) {
                ResourceKey<Miraculous> miraculous = transformed.getFirst();
                TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculous, miraculousesData.get(miraculous), false, false));
            } else {
                List<RadialMenuOption> options = new ReferenceArrayList<>();
                Map<RadialMenuOption, ResourceKey<Miraculous>> miraculousOptions = new Reference2ReferenceOpenHashMap<>();
                Map<ResourceKey<Miraculous>, ItemStack> miraculousStacks = new Reference2ReferenceOpenHashMap<>();
                for (ItemStack stack : CuriosUtils.getAllItems(player).values()) {
                    ResourceKey<Miraculous> miraculous = stack.get(MineraculousDataComponents.MIRACULOUS);
                    if (miraculous != null) {
                        RadialMenuOption option = new RadialMenuOption() {
                            private Integer color;

                            @Override
                            public String translationKey() {
                                return Miraculous.toLanguageKey(miraculous);
                            }

                            @Override
                            public Integer colorOverride() {
                                if (color == null)
                                    color = player.level().holderOrThrow(miraculous).value().color().getValue();
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
                        ResourceKey<Miraculous> miraculous = miraculousOptions.get(selected);
                        if (miraculous != null) {
                            ItemStack stack = miraculousStacks.get(miraculous);
                            MiraculousData data = miraculousesData.get(miraculous);
                            if (data != null) {
                                KwamiData kwamiData = stack.get(MineraculousDataComponents.KWAMI_DATA);
                                if (kwamiData != null) {
                                    if (kwamiData.charged()) {
                                        TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculous, data, true, false));
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
                    ResourceKey<Miraculous> miraculous = miraculousOptions.get(options.getFirst());
                    TommyLibServices.NETWORK.sendToServer(new ServerboundMiraculousTransformPayload(miraculous, miraculousesData.get(miraculous), true, false));
                }
            }
        }
    }

    private static void handleActivatePower() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            MiraculousesData miraculousesData = player.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            List<ResourceKey<Miraculous>> transformed = miraculousesData.getTransformed();
            if (!transformed.isEmpty()) {
                ResourceKey<Miraculous> miraculous = transformed.getFirst();
                MiraculousData data = miraculousesData.get(miraculous);
                if (data != null) {
                    // TODO: Ability overriding
                    if (!data.mainPowerActive() && !data.usedLimitedPower() && player.level().holderOrThrow(miraculous).value().activeAbility().isPresent()) {
                        TommyLibServices.NETWORK.sendToServer(new ServerboundSetMiraculousPowerActivatedPayload(miraculous));
                    }
                }
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
                } else if (provider.handleSecondaryKeyBehavior(mainHandStack, hand)) {
                    return;
                }
            }
            hand = InteractionHand.OFF_HAND;
            ItemStack offHandStack = player.getOffhandItem();
            if (offHandStack.getItem() instanceof RadialMenuProvider<?> provider) {
                if (!MineraculousClientUtils.tryOpenRadialMenuScreenFromProvider(hand, offHandStack, provider)) {
                    provider.handleSecondaryKeyBehavior(offHandStack, hand);
                }
                return;
            }
            MiraculousesData miraculousesData = player.getData(MineraculousAttachmentTypes.MIRACULOUSES);
            List<ResourceKey<Miraculous>> transformed = miraculousesData.getTransformed();
            if (!transformed.isEmpty()) {
                ResourceKey<Miraculous> miraculous = transformed.getFirst();
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
