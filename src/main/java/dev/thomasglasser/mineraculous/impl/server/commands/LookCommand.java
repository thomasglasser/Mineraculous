package dev.thomasglasser.mineraculous.impl.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.impl.client.look.LookLoader;
import dev.thomasglasser.mineraculous.impl.client.look.LookManager;
import dev.thomasglasser.mineraculous.impl.client.look.MiraculousLook;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

// TODO: Remove when screen is added
public class LookCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("look")
                .then(Commands.literal("reload")
                        .executes(LookCommand::reload))
                .then(Commands.literal("list")
                        .executes(LookCommand::list))
                .then(Commands.literal("equip")
                        .then(Commands.argument("miraculous", ResourceArgument.resource(context, MineraculousRegistries.MIRACULOUS))
                                .then(Commands.argument("look_id", StringArgumentType.string())
                                        .suggests(SUGGEST_LOOKS)
                                        .executes(LookCommand::equip))))
                .then(Commands.literal("unequip")
                        .then(Commands.argument("miraculous", ResourceArgument.resource(context, MineraculousRegistries.MIRACULOUS))
                                .executes(LookCommand::unequip))));
    }

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_LOOKS = (context, builder) -> SharedSuggestionProvider.suggest(LookManager.values().stream().map(MiraculousLook::id), builder);

    private static int reload(CommandContext<CommandSourceStack> context) {
        LookLoader.load();
        context.getSource().sendSuccess(() -> Component.literal("Reloading looks..."), true);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("Available Looks:"), false);
        for (MiraculousLook look : LookManager.values()) {
            context.getSource().sendSuccess(() -> Component.literal("- " + look.id() + " (" + look.displayName() + ")"), false);
        }
        return 1;
    }

    private static int equip(CommandContext<CommandSourceStack> context) {
        try {
            Player player = context.getSource().getPlayerOrException();
            var miraculous = ResourceArgument.getResource(context, "miraculous", MineraculousRegistries.MIRACULOUS);
            String lookId = StringArgumentType.getString(context, "look_id");

            LookManager.assign(player.getUUID(), miraculous, lookId);
            context.getSource().sendSuccess(() -> Component.literal("Equipped look: " + lookId), true);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 1;
    }

    private static int unequip(CommandContext<CommandSourceStack> context) {
        try {
            Player player = context.getSource().getPlayerOrException();
            var miraculous = ResourceArgument.getResource(context, "miraculous", MineraculousRegistries.MIRACULOUS);

            LookManager.unassign(player.getUUID(), miraculous);
            context.getSource().sendSuccess(() -> Component.literal("Unequipped look."), true);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 1;
    }
}
