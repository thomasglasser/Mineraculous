package dev.thomasglasser.mineraculous.client.gui.kamiko.categories;

import dev.thomasglasser.mineraculous.client.gui.kamiko.KamikoMenuCategory;
import dev.thomasglasser.mineraculous.client.gui.kamiko.KamikoMenuItem;
import dev.thomasglasser.mineraculous.client.gui.kamiko.PlayerKamikoMenuItem;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class TargetPlayerMenuCategory implements KamikoMenuCategory {
    public static final Component TARGET_PROMPT = Component.translatable("kamiko_menu.teleport.prompt");
    private static final Comparator<PlayerInfo> PROFILE_ORDER = Comparator.comparing(p_253335_ -> p_253335_.getProfile().getId());
    private final List<KamikoMenuItem> items;

    public TargetPlayerMenuCategory() {
        this(Minecraft.getInstance().getConnection().getListedOnlinePlayers());
    }

    public TargetPlayerMenuCategory(Collection<PlayerInfo> players) {
        this.items = players.stream()
                .filter(p_253336_ -> {
                    if (p_253336_.getGameMode() == GameType.SPECTATOR)
                        return false;
                    Player player = Minecraft.getInstance().level.getPlayerByUUID(p_253336_.getProfile().getId());
                    return player != null && player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).kamikotization().isEmpty();
                })
                .sorted(PROFILE_ORDER)
                .map(p_253334_ -> (KamikoMenuItem) new PlayerKamikoMenuItem(p_253334_.getProfile()))
                .toList();
    }

    @Override
    public List<KamikoMenuItem> getItems() {
        return this.items;
    }

    @Override
    public Component getPrompt() {
        return TARGET_PROMPT;
    }
}
