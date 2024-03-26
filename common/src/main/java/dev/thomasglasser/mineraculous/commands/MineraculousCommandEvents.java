package dev.thomasglasser.mineraculous.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.thomasglasser.mineraculous.server.commands.MiraculousCommand;
import net.minecraft.commands.CommandSourceStack;

public class MineraculousCommandEvents
{
    public static void onCommandsRegister(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        MiraculousCommand.register(dispatcher);
    }
}
