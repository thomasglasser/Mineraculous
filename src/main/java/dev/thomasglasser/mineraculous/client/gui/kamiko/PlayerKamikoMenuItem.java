package dev.thomasglasser.mineraculous.client.gui.kamiko;

import com.mojang.authlib.GameProfile;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.network.ServerboundSetPlayerAttackTargetPayload;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;

public class PlayerKamikoMenuItem implements KamikoMenuItem {
    private final GameProfile profile;
    private final Supplier<PlayerSkin> skin;
    private final Component name;

    public PlayerKamikoMenuItem(GameProfile profile) {
        this.profile = profile;
        this.skin = Minecraft.getInstance().getSkinManager().lookupInsecure(profile);
        this.name = Component.literal(profile.getName());
    }

    @Override
    public void selectItem(KamikoMenu menu) {
        if (MineraculousClientUtils.getCameraEntity() instanceof Kamiko kamiko) {
            Player player = kamiko.level().getPlayerByUUID(this.profile.getId());
            if (player != null)
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetPlayerAttackTargetPayload(kamiko.getId(), player.getUUID()));
        }
    }

    @Override
    public Component getName() {
        return this.name;
    }

    @Override
    public void renderIcon(GuiGraphics p_282282_, float p_282686_, float p_361678_) {
        PlayerFaceRenderer.draw(p_282282_, this.skin.get(), 2, 2, 12, ARGB.white(p_361678_));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
