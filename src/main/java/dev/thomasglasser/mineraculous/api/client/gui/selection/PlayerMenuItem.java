package dev.thomasglasser.mineraculous.api.client.gui.selection;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.api.client.event.CreatePlayerMenuItemEvent;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

/**
 * A base implementation of a {@link SelectionMenuItem} for selecting a player,
 * firing an event to determine what to render.
 * Does not define what to do on selection.
 */
public abstract class PlayerMenuItem implements SelectionMenuItem {
    protected final UUID id;
    protected final Component name;
    protected final Either<Supplier<PlayerSkin>, ItemStack> display;

    public PlayerMenuItem(GameProfile profile) {
        this.id = profile.getId();
        Player player = ClientUtils.getLevel().getPlayerByUUID(profile.getId());
        var event = NeoForge.EVENT_BUS.post(new CreatePlayerMenuItemEvent(player, player.getDisplayName(), Either.left(Minecraft.getInstance().getSkinManager().lookupInsecure(profile))));
        this.name = event.getName();
        this.display = event.getDisplay();
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public void renderIcon(GuiGraphics guiGraphics, float alpha) {
        guiGraphics.setColor(1, 1, 1, alpha);
        display.ifLeft(skin -> PlayerFaceRenderer.draw(guiGraphics, skin.get(), 2, 2, 12))
                .ifRight(stack -> guiGraphics.renderFakeItem(stack, 0, 0));
        guiGraphics.setColor(1, 1, 1, 1);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
