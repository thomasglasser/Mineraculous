package dev.thomasglasser.mineraculous.client.gui.kamiko;

import com.mojang.authlib.GameProfile;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.network.ServerboundSetPlayerAttackTargetPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerMenuItem implements KamikoMenuItem {
    private final GameProfile profile;
    private final Supplier<PlayerSkin> skin;
    private final Component name;
    private final Optional<ItemStack> stack;

    public PlayerMenuItem(GameProfile profile) {
        this.profile = profile;
        this.skin = Minecraft.getInstance().getSkinManager().lookupInsecure(profile);
        Player player = Minecraft.getInstance().level.getPlayerByUUID(profile.getId());
        if (player != null) {
            this.name = player.getDisplayName();
            List<ResourceKey<Miraculous>> transformed = player.getData(MineraculousAttachmentTypes.MIRACULOUS).getTransformed();
            if (!transformed.isEmpty()) {
                ResourceKey<Miraculous> key = transformed.getFirst();
                this.stack = Optional.of(Miraculous.createMiraculousStack(key));
            } else {
                this.stack = Optional.empty();
            }
        } else {
            this.name = Component.empty();
            this.stack = Optional.empty();
        }
    }

    @Override
    public void selectItem(KamikoMenu menu) {
        if (MineraculousClientUtils.getCameraEntity() instanceof Kamiko kamiko) {
            TommyLibServices.NETWORK.sendToServer(new ServerboundSetPlayerAttackTargetPayload(kamiko.getId(), profile.getId()));
        }
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public void renderIcon(GuiGraphics guiGraphics, float alpha) {
        guiGraphics.setColor(1, 1, 1, alpha);
        stack.ifPresentOrElse(stack -> guiGraphics.renderFakeItem(stack, 0, 0),
                () -> PlayerFaceRenderer.draw(guiGraphics, this.skin.get(), 2, 2, 12));
        guiGraphics.setColor(1, 1, 1, 1);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
