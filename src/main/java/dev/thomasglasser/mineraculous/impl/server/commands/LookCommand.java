package dev.thomasglasser.mineraculous.impl.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.impl.client.look.LookManager;
import dev.thomasglasser.mineraculous.impl.client.look.MiraculousLook;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetLookPayload;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
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
                .then(Commands.literal("equip")
                        .then(Commands.argument("miraculous", ResourceArgument.resource(context, MineraculousRegistries.MIRACULOUS))
                                .then(Commands.argument("look_id", StringArgumentType.string())
                                        .suggests(SUGGEST_LOOKS)
                                        .executes(LookCommand::equip))
                                .executes(LookCommand::unequip))));
    }

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_LOOKS = (context, builder) -> SharedSuggestionProvider.suggest(LookManager.values().stream().map(MiraculousLook::id), builder);

    private static int equip(CommandContext<CommandSourceStack> context) {
        try {
            var miraculous = ResourceArgument.getResource(context, "miraculous", MineraculousRegistries.MIRACULOUS);
            String lookId = StringArgumentType.getString(context, "look_id");

            MiraculousLook look = LookManager.getLook(lookId);
            if (look != null) {
                if (look.validMiraculouses().contains(miraculous.getKey())) {
                    TommyLibServices.NETWORK.sendToServer(new ServerboundSetLookPayload(miraculous, lookId, look.hash()));
                    context.getSource().sendSuccess(() -> Component.literal("Equipped look: " + lookId), true);
                } else {
                    context.getSource().sendFailure(Component.literal("Look " + lookId + " not allowed for miraculous " + miraculous.getKey().location()));
                }
            } else {
                context.getSource().sendFailure(Component.literal("Look not found: " + lookId));
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 1;
    }

    private static int unequip(CommandContext<CommandSourceStack> context) {
        try {
            Player player = ClientUtils.getLocalPlayer();
            var miraculous = ResourceArgument.getResource(context, "miraculous", MineraculousRegistries.MIRACULOUS);

            LookManager.unassign(player.getUUID(), miraculous);
            context.getSource().sendSuccess(() -> Component.literal("Unequipped look."), true);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 1;
    }
}
