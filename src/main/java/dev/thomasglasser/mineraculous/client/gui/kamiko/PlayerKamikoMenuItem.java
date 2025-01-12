package dev.thomasglasser.mineraculous.client.gui.kamiko;

import com.mojang.authlib.GameProfile;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.network.ServerboundSetPlayerAttackTargetPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public class PlayerKamikoMenuItem implements KamikoMenuItem {
    private final GameProfile profile;
    private final Supplier<PlayerSkin> skin;
    private final Player player;

    public PlayerKamikoMenuItem(GameProfile profile) {
        this.profile = profile;
        this.skin = Minecraft.getInstance().getSkinManager().lookupInsecure(profile);
        this.player = Minecraft.getInstance().level.getPlayerByUUID(profile.getId());
    }

    @Override
    public void selectItem(KamikoMenu menu) {
        if (MineraculousClientUtils.getCameraEntity() instanceof Kamiko kamiko) {
            if (player != null)
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetPlayerAttackTargetPayload(kamiko.getId(), player.getUUID()));
        }
    }

    @Override
    public Component getName() {
        return player != null ? player.getDisplayName() : Component.empty();
    }

    @Override
    public void renderIcon(GuiGraphics guiGraphics, float p_282686_, float p_361678_) {
        if (player != null) {
            MiraculousDataSet miraculousDataSet = player.getData(MineraculousAttachmentTypes.MIRACULOUS);
            List<ResourceKey<Miraculous>> transformed = miraculousDataSet.getTransformed();
            if (!transformed.isEmpty())
                guiGraphics.renderFakeItem(Miraculous.createMiraculousStack(transformed.getFirst()), 0, 0);
            else
                PlayerFaceRenderer.draw(guiGraphics, this.skin.get(), 2, 2, 12);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
