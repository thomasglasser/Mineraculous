package dev.thomasglasser.mineraculous.commands;

import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.command.ConfigCommand;

public class MineraculousNeoForgeCommandEvents
{
    public static void onCommandsRegister(RegisterCommandsEvent event)
    {
        MineraculousCommandEvents.onCommandsRegister(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
}
