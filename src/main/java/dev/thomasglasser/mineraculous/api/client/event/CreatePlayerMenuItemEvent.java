package dev.thomasglasser.mineraculous.api.client.event;

import com.mojang.datafixers.util.Either;
import java.util.function.Supplier;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when a {@link dev.thomasglasser.mineraculous.api.client.gui.selection.PlayerMenuItem} is constructed.
 * This can be used to customize the name and display of the menu item.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class CreatePlayerMenuItemEvent extends PlayerEvent {
    private Component name;
    private Either<Supplier<PlayerSkin>, ItemStack> display;

    @ApiStatus.Internal
    public CreatePlayerMenuItemEvent(Player player, Component name, Either<Supplier<PlayerSkin>, ItemStack> display) {
        super(player);
        this.name = name;
        this.display = display;
    }

    /**
     * Returns the name of the menu item.
     * 
     * @return The name of the menu item
     */
    public Component getName() {
        return name;
    }

    /**
     * Returns the display of the menu item.
     * 
     * @return The display of the menu item
     */
    public Either<Supplier<PlayerSkin>, ItemStack> getDisplay() {
        return display;
    }

    /**
     * Sets the name of the menu item.
     * 
     * @param name The new name of the menu item
     */
    public void setName(Component name) {
        this.name = name;
    }

    /**
     * Sets the display of the menu item.
     * 
     * @param display The new display of the menu item
     */
    public void setDisplay(Supplier<PlayerSkin> display) {
        this.display = Either.left(display);
    }

    /**
     * Sets the display of the menu item.
     * 
     * @param display The new display of the menu item
     */
    public void setDisplay(ItemStack display) {
        this.display = Either.right(display);
    }
}
