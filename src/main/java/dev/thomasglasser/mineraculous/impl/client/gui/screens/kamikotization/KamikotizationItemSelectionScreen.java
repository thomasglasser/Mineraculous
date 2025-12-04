package dev.thomasglasser.mineraculous.impl.client.gui.screens.kamikotization;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.client.gui.screens.inventory.ExternalCuriosInventoryScreen;
import dev.thomasglasser.mineraculous.api.client.gui.screens.inventory.InventorySyncTracker;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.api.world.entity.curios.CuriosData;
import dev.thomasglasser.mineraculous.api.world.kamikotization.Kamikotization;
import dev.thomasglasser.mineraculous.api.world.level.storage.abilityeffects.AbilityEffectUtils;
import dev.thomasglasser.mineraculous.impl.network.ServerboundRequestInventorySyncPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetItemKamikotizingPayload;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSpawnTamedKamikoPayload;
import dev.thomasglasser.mineraculous.impl.world.item.component.KamikoData;
import dev.thomasglasser.mineraculous.impl.world.level.storage.SlotInfo;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.common.inventory.CurioSlot;

public class KamikotizationItemSelectionScreen extends ExternalCuriosInventoryScreen implements InventorySyncTracker {
    public static final String NO_KAMIKOTIZATIONS = "gui.kamikotization_item_selection.no_kamikotizations";
    public static final Component APPLIES_TO = Component.translatable("gui.kamikotization_item_selection.applies_to").withStyle(ChatFormatting.GRAY);

    private final Multimap<ItemStack, Holder<Kamikotization>> kamikotizations = MultimapBuilder.hashKeys().linkedListValues().build();

    private final KamikoData kamikoData;

    private SlotInfo slotInfo;
    private ItemStack slotStack;

    public KamikotizationItemSelectionScreen(Player target, KamikoData kamikoData) {
        super(target, false);
        this.kamikoData = kamikoData;
        determineKamikotizations();
    }

    @Override
    public void onInventorySynced(Player player) {
        determineKamikotizations();
    }

    protected void determineKamikotizations() {
        kamikotizations.clear();
        Registry<Kamikotization> registry = target.level().registryAccess().registryOrThrow(MineraculousRegistries.KAMIKOTIZATION);
        for (ItemStack stack : getMenu().getItems()) {
            determineKamikotizations(stack, registry);
        }
        if (kamikotizations.isEmpty()) {
            ClientUtils.getLocalPlayer().displayClientMessage(Component.translatable(NO_KAMIKOTIZATIONS, target.getDisplayName()), true);
            this.minecraft = Minecraft.getInstance();
            onClose();
        }
    }

    protected void determineKamikotizations(ItemStack stack, Registry<Kamikotization> registry) {
        if (!(stack.isEmpty() || stack.is(MineraculousItemTags.KAMIKOTIZATION_IMMUNE) || stack.has(MineraculousDataComponents.KAMIKOTIZATION))) {
            for (Kamikotization kamikotization : registry) {
                if (kamikotization.itemPredicate().test(stack)) {
                    kamikotizations.put(stack, registry.wrapAsHolder(kamikotization));
                }
            }
        }
    }

    @Override
    public boolean canPickUp(Slot slot, Player target, AbstractContainerMenu menu) {
        ItemStack stack = slot.getItem();
        return super.canPickUp(slot, target, menu) && checkHasKamikotizations(stack, target);
    }

    private boolean checkHasKamikotizations(ItemStack stack, Player target) {
        if (!kamikotizations.containsKey(stack) && target.getInventory().getSelected() == stack)
            determineKamikotizations(stack, target.level().registryAccess().registryOrThrow(MineraculousRegistries.KAMIKOTIZATION));
        return kamikotizations.containsKey(stack);
    }

    @Override
    public void pickUp(Slot slot, Player target, AbstractContainerMenu menu) {
        if (slot instanceof CurioSlot curiosSlot)
            slotInfo = new SlotInfo(new CuriosData(curiosSlot.getSlotContext()));
        else
            slotInfo = new SlotInfo(slot.getSlotIndex());
        slotStack = slot.getItem();
    }

    @Override
    public void onClose(boolean cancel) {
        if (cancel) {
            TommyLibServices.NETWORK.sendToServer(new ServerboundSpawnTamedKamikoPayload(minecraft.player.getUUID(), target.blockPosition().above()));
            AbilityEffectUtils.removeFaceMaskTexture(target, kamikoData.faceMaskTexture());
        } else if (slotInfo != null) {
            Minecraft.getInstance().setScreen(new KamikotizationSelectionScreen(target, kamikoData, new ReferenceArrayList<>(kamikotizations.get(slotStack)), slotInfo, slotStack.getCount()));
            TommyLibServices.NETWORK.sendToServer(new ServerboundSetItemKamikotizingPayload(Optional.of(target.getUUID()), true, slotInfo));
        }
        TommyLibServices.NETWORK.sendToServer(new ServerboundRequestInventorySyncPayload(target.getUUID(), false));
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (hoveredSlot != null && this.canPickUp(hoveredSlot, target, menu)) {
            List<Component> components = new ReferenceArrayList<>();
            components.add(APPLIES_TO);
            for (Holder<Kamikotization> kamikotization : kamikotizations.get(hoveredSlot.getItem())) {
                components.add(CommonComponents.space().append(Component.translatable(MineraculousConstants.toLanguageKey(kamikotization.getKey())).withColor(kamikoData.nameColor())));
            }
            guiGraphics.renderTooltip(this.font, components, Optional.empty(), mouseX, mouseY);
        }
    }
}
