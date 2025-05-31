package dev.thomasglasser.mineraculous.client.gui.screens;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class MiraculousEligiblePlayerList extends ContainerObjectSelectionList<MiraculousEligiblePlayerEntry> {
    private final List<MiraculousEligiblePlayerEntry> entries = new ReferenceArrayList<>();
    private final int kwamiId;
    @Nullable
    private String filter;

    public MiraculousEligiblePlayerList(Minecraft minecraft, int width, int height, int y, int itemHeight, int kwamiId) {
        super(minecraft, width, height, y, itemHeight);
        this.kwamiId = kwamiId;
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {}

    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {}

    @Override
    protected void enableScissor(GuiGraphics guiGraphics) {
        guiGraphics.enableScissor(this.getX(), this.getY() + 4, this.getRight(), this.getBottom());
    }

    public void updatePlayerList(double scrollAmount) {
        Set<MiraculousEligiblePlayerEntry> entries = new ReferenceOpenHashSet<>();
        for (Player player : this.minecraft.level.players()) {
            PlayerInfo playerinfo = this.minecraft.player.connection.getPlayerInfo(player.getUUID());
            entries.add(new MiraculousEligiblePlayerEntry(this.minecraft, player.getUUID(), playerinfo.getProfile().getName(), playerinfo::getSkin, this.kwamiId));
        }
        this.updateFiltersAndScroll(entries, scrollAmount);
    }

    private void updateFiltersAndScroll(Collection<MiraculousEligiblePlayerEntry> entries, double scrollAmount) {
        this.entries.clear();
        this.entries.addAll(entries);
        this.sortPlayerEntries();
        this.updateFilteredPlayers();
        this.replaceEntries(this.entries);
        this.setScrollAmount(scrollAmount);
    }

    private void sortPlayerEntries() {
        this.entries.sort(Comparator.<MiraculousEligiblePlayerEntry, Integer>comparing(entry -> {
            // First show the local player, then players without DCE security UUIDs, then the rest
            if (this.minecraft.isLocalPlayer(entry.getPlayerId())) {
                return 0;
            } else if (entry.getPlayerId().version() == 2) {
                return 4;
            } else {
                return 3;
            }
        }).thenComparing(entry -> {
            // Players with normal characters in the beginning (underscored, letters, and numbers) take priority
            if (!entry.getPlayerName().isBlank()) {
                int i = entry.getPlayerName().codePointAt(0);
                if (i == 95 || i >= 97 && i <= 122 || i >= 65 && i <= 90 || i >= 48 && i <= 57) {
                    return 0;
                }
            }

            return 1;
        }).thenComparing(MiraculousEligiblePlayerEntry::getPlayerName, String::compareToIgnoreCase));
    }

    private void updateFilteredPlayers() {
        if (this.filter != null) {
            this.entries.removeIf(entry -> !entry.getPlayerName().toLowerCase().contains(this.filter));
            this.replaceEntries(this.entries);
        }
    }

    public void setFilter(String filter) {
        this.filter = filter;
        updatePlayerList(this.getScrollAmount());
    }

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }
}
