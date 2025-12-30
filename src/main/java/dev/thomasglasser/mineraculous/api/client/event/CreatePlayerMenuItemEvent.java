package dev.thomasglasser.mineraculous.api.client.event;

import com.mojang.datafixers.util.Either;
import java.util.function.Supplier;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class CreatePlayerMenuItemEvent extends PlayerEvent {
    private Component name;
    private Either<Supplier<PlayerSkin>, ItemStack> display;

    public CreatePlayerMenuItemEvent(Player player, Component name, Either<Supplier<PlayerSkin>, ItemStack> display) {
        super(player);
        this.name = name;
        this.display = display;
    }

    public Component getName() {
        return name;
    }

    public Either<Supplier<PlayerSkin>, ItemStack> getDisplay() {
        return display;
    }

    public void setName(Component name) {
        this.name = name;
    }

    public void setDisplay(Supplier<PlayerSkin> display) {
        this.display = Either.left(display);
    }

    public void setDisplay(ItemStack display) {
        this.display = Either.right(display);
    }
}
