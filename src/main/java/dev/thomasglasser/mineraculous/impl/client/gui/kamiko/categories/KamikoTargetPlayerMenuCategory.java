package dev.thomasglasser.mineraculous.impl.client.gui.kamiko.categories;

import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuCategory;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenuItem;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.client.gui.kamiko.KamikoPlayerMenuItem;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.tommylib.api.world.entity.EntityUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.spectator.categories.TeleportToPlayerMenuCategory;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

public class KamikoTargetPlayerMenuCategory implements SelectionMenuCategory {
    public static final Component TARGET_PROMPT = Component.translatable("kamiko_menu.teleport.prompt");
    private final List<SelectionMenuItem> items;

    public KamikoTargetPlayerMenuCategory() {
        this(Minecraft.getInstance().getConnection().getListedOnlinePlayers());
    }

    public KamikoTargetPlayerMenuCategory(Collection<PlayerInfo> players) {
        this.items = new ObjectArrayList<>();
        List<PlayerInfo> sorted = new ObjectArrayList<>(players);
        sorted.sort(TeleportToPlayerMenuCategory.PROFILE_ORDER);
        for (PlayerInfo playerInfo : sorted) {
            if (playerInfo.getGameMode() == GameType.SPECTATOR)
                continue;
            Player player = Minecraft.getInstance().level.getPlayerByUUID(playerInfo.getProfile().getId());
            Kamiko kamiko = MineraculousClientUtils.getCameraEntity() instanceof Kamiko k ? k : null;
            if (player != null && kamiko != null && player.getData(MineraculousAttachmentTypes.KAMIKOTIZATION).isEmpty() && !EntityUtils.TARGET_TOO_FAR_PREDICATE.test(kamiko, player)) {
                this.items.add(new KamikoPlayerMenuItem(playerInfo.getProfile()));
            }
        }
    }

    @Override
    public List<SelectionMenuItem> getItems() {
        return this.items;
    }

    @Override
    public Component getPrompt() {
        return TARGET_PROMPT;
    }
}
