package dev.thomasglasser.mineraculous.client.gui.kamiko.categories;

import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.gui.kamiko.KamikoMenuCategory;
import dev.thomasglasser.mineraculous.client.gui.kamiko.KamikoMenuItem;
import dev.thomasglasser.mineraculous.client.gui.kamiko.PlayerMenuItem;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.spectator.categories.TeleportToPlayerMenuCategory;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class TargetPlayerMenuCategory implements KamikoMenuCategory {
    public static final Component TARGET_PROMPT = Component.translatable("kamiko_menu.teleport.prompt");
    private final List<KamikoMenuItem> items;

    public TargetPlayerMenuCategory() {
        this(Minecraft.getInstance().getConnection().getListedOnlinePlayers());
    }

    public TargetPlayerMenuCategory(Collection<PlayerInfo> players) {
        this.items = new ReferenceArrayList<>();
        List<PlayerInfo> sorted = new ReferenceArrayList<>(players);
        sorted.sort(TeleportToPlayerMenuCategory.PROFILE_ORDER);
        for (PlayerInfo playerInfo : sorted) {
            if (playerInfo.getGameMode() == GameType.SPECTATOR)
                continue;
            Player player = Minecraft.getInstance().level.getPlayerByUUID(playerInfo.getProfile().getId());
            Kamiko kamiko = MineraculousClientUtils.getCameraEntity() instanceof Kamiko k ? k : null;
            if (player != null && kamiko != null && player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isEmpty() && !EntityUtils.TARGET_TOO_FAR_PREDICATE.test(kamiko, player)) {
                this.items.add(new PlayerMenuItem(playerInfo.getProfile()));
            }
        }
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
