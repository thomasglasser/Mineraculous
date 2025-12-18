package dev.thomasglasser.mineraculous.impl.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.impl.client.look.LookManager;
import dev.thomasglasser.mineraculous.impl.client.look.MiraculousLook;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetLookPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.network.chat.Component;

// TODO: Remove when screen is added
public class LookCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("look")
                .then(Commands.literal("equip")
                        .then(Commands.argument("miraculous", ResourceArgument.resource(context, MineraculousRegistries.MIRACULOUS))
                                .then(Commands.argument("hash", StringArgumentType.string())
                                        .suggests(SUGGEST_LOOKS)
                                        .executes(LookCommand::equip))
                                .executes(LookCommand::unequip))));
    }

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_LOOKS = (context, builder) -> SharedSuggestionProvider.suggest(LookManager.values().stream().map(MiraculousLook::hash), builder);

    private static int equip(CommandContext<CommandSourceStack> context) {
        try {
            var miraculous = ResourceArgument.getResource(context, "miraculous", MineraculousRegistries.MIRACULOUS);
            String hash = StringArgumentType.getString(context, "hash");

            MiraculousLook look = LookManager.getLook(hash);
            if (look != null) {
                if (look.validMiraculouses().contains(miraculous.getKey())) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetLookPayload(miraculous, Optional.of(hash)));
                    context.getSource().sendSuccess(() -> Component.literal("Equipped look: " + hash), true);
                } else {
                    context.getSource().sendFailure(Component.literal("Look " + hash + " not allowed for miraculous " + miraculous.getKey().location()));
                }
            } else {
                context.getSource().sendFailure(Component.literal("Look not found: " + hash));
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 1;
    }

    private static int unequip(CommandContext<CommandSourceStack> context) {
        try {
            var miraculous = ResourceArgument.getResource(context, "miraculous", MineraculousRegistries.MIRACULOUS);

            TommyLibServices.NETWORK.sendToServer(new ServerboundSetLookPayload(miraculous, Optional.empty()));
            context.getSource().sendSuccess(() -> Component.literal("Unequipped look."), true);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 1;
    }
}
