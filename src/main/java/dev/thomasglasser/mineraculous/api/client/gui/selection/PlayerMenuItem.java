package dev.thomasglasser.mineraculous.api.client.gui.selection;

import com.mojang.authlib.GameProfile;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A base implementation of a {@link SelectionMenuItem} for selecting a player,
 * firing an event to determine what to render.
 * Does not define what to do on selection.
 */
public abstract class PlayerMenuItem implements SelectionMenuItem {
    protected final GameProfile profile;
    protected final Supplier<PlayerSkin> skin;
    protected final Component name;
    protected final Optional<ItemStack> stack;

    public PlayerMenuItem(GameProfile profile) {
        this.profile = profile;
        this.skin = Minecraft.getInstance().getSkinManager().lookupInsecure(profile);
        Player player = Minecraft.getInstance().level.getPlayerByUUID(profile.getId());
        if (player != null) {
            this.name = player.getDisplayName();
            this.stack = this.getStack(player);
        } else {
            this.name = Component.empty();
            this.stack = Optional.empty();
        }
    }

    protected Optional<ItemStack> getStack(Player player) {
        // TODO: Move to an event
        List<Holder<Miraculous>> transformed = player.getData(MineraculousAttachmentTypes.MIRACULOUSES).getTransformed();
        if (!transformed.isEmpty()) {
            return Optional.of(Miraculous.createMiraculousStack(transformed.getFirst()));
        } else {
            return Optional.empty();
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
