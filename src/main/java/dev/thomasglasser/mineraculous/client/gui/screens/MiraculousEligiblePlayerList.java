package dev.thomasglasser.mineraculous.client.gui.screens;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class MiraculousEligiblePlayerList extends ContainerObjectSelectionList<MiraculousEligiblePlayerEntry> {
    private final List<MiraculousEligiblePlayerEntry> players = Lists.newArrayList();
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
        Map<UUID, MiraculousEligiblePlayerEntry> map = new HashMap<>();
        for (Player player : this.minecraft.level.players()) {
            PlayerInfo playerinfo = this.minecraft.player.connection.getPlayerInfo(player.getUUID());
            map.put(player.getUUID(), new MiraculousEligiblePlayerEntry(this.minecraft, player.getUUID(), playerinfo.getProfile().getName(), playerinfo::getSkin, this.kwamiId));
        }
        this.updateFiltersAndScroll(map.values(), scrollAmount);
    }

    private void sortPlayerEntries() {
        this.players.sort(Comparator.<MiraculousEligiblePlayerEntry, Integer>comparing(p_240744_ -> {
            if (this.minecraft.isLocalPlayer(p_240744_.getPlayerId())) {
                return 0;
            } else if (p_240744_.getPlayerId().version() == 2) {
                return 4;
            } else {
                return 3;
            }
        }).thenComparing(p_240745_ -> {
            if (!p_240745_.getPlayerName().isBlank()) {
                int i = p_240745_.getPlayerName().codePointAt(0);
                if (i == 95 || i >= 97 && i <= 122 || i >= 65 && i <= 90 || i >= 48 && i <= 57) {
                    return 0;
                }
            }

            return 1;
        }).thenComparing(MiraculousEligiblePlayerEntry::getPlayerName, String::compareToIgnoreCase));
    }

    private void updateFiltersAndScroll(Collection<MiraculousEligiblePlayerEntry> players, double scrollAmount) {
        this.players.clear();
        this.players.addAll(players);
        this.sortPlayerEntries();
        this.updateFilteredPlayers();
        this.replaceEntries(this.players);
        this.setScrollAmount(scrollAmount);
    }

    private void updateFilteredPlayers() {
        if (this.filter != null) {
            this.players.removeIf(p_100710_ -> !p_100710_.getPlayerName().toLowerCase(Locale.ROOT).contains(this.filter));
            this.replaceEntries(this.players);
        }
    }

    public void setFilter(String filter) {
        this.filter = filter;
        updatePlayerList(this.getScrollAmount());
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }
}
