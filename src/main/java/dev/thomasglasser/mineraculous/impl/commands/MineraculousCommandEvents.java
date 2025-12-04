package dev.thomasglasser.mineraculous.impl.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.impl.server.commands.MiraculousCommand;
import dev.thomasglasser.mineraculous.impl.world.entity.KamikotizedMinion;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.network.chat.Component;
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
        dispatcher.register(Commands.literal("miniontest").then(Commands.argument("kamikotization", ResourceArgument.resource(event.getBuildContext(), MineraculousRegistries.KAMIKOTIZATION)).executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            KamikotizedMinion minion = new KamikotizedMinion(player, ResourceArgument.getResource(context, "kamikotization", MineraculousRegistries.KAMIKOTIZATION), Optional.of(Component.literal("name.")));
            player.level().addFreshEntity(minion);
            return 1;
        })));

        dispatcher.register(Commands.literal("minionstop").then(Commands.argument("minion", EntityArgument.entity()).executes(context -> {
            Entity entity = EntityArgument.getEntity(context, "minion");
            if (entity instanceof KamikotizedMinion minion) {
                minion.discard();
            }
            return 1;
        })));

        dispatcher.register(Commands.literal("miniontame").then(Commands.argument("minion", EntityArgument.entity()).executes(context -> {
            ServerPlayer player = context.getSource().getPlayerOrException();
            Entity entity = EntityArgument.getEntity(context, "minion");
            if (entity instanceof KamikotizedMinion minion) {
                minion.setOwnerUUID(player.getUUID());
                return 1;
            }
            return 0;
        })));
    }
}
