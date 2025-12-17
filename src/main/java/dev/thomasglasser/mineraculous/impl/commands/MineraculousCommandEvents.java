package dev.thomasglasser.mineraculous.impl.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.thomasglasser.mineraculous.impl.server.commands.LookCommand;
import dev.thomasglasser.mineraculous.impl.server.commands.MiraculousCommand;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.command.ConfigCommand;

public class MineraculousCommandEvents {
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        MiraculousCommand.register(dispatcher);

        ConfigCommand.register(dispatcher);
    }

    public static void onClientCommandsRegister(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CommandBuildContext context = event.getBuildContext();

        LookCommand.register(dispatcher, context);
    }
}
