package dev.thomasglasser.mineraculous.impl.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.thomasglasser.mineraculous.impl.server.commands.MiraculousCommand;
import dev.thomasglasser.mineraculous.impl.world.entity.KamikotizedMinion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.command.ConfigCommand;

public class MineraculousCommandEvents {
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        MiraculousCommand.register(dispatcher);

        ConfigCommand.register(dispatcher);

        // TODO: Remove when testing done
        dispatcher.register(Commands.literal("miniontest").executes(source -> {
            ServerPlayer player = source.getSource().getPlayerOrException();
            KamikotizedMinion minion = new KamikotizedMinion(player);
            player.level().addFreshEntity(minion);
            return 1;
        }));
    }
}
