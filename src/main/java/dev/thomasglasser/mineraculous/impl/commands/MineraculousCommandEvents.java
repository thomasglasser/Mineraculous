package dev.thomasglasser.mineraculous.impl.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.thomasglasser.mineraculous.impl.server.commands.MiraculousCommand;
import dev.thomasglasser.mineraculous.impl.world.entity.KamikotizedMinion;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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

        dispatcher.register(Commands.literal("tameminion").then(Commands.argument("minion", EntityArgument.entity()).executes(source -> {
            ServerPlayer player = source.getSource().getPlayerOrException();
            Entity entity = EntityArgument.getEntity(source, "minion");
            if (entity instanceof KamikotizedMinion minion) {
                minion.setOwnerUUID(player.getUUID());
                return 1;
            }
            return 0;
        })));
    }
}
