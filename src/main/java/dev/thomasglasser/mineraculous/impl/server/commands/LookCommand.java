package dev.thomasglasser.mineraculous.impl.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.thomasglasser.mineraculous.api.client.gui.screens.look.LookCustomizationScreen;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContextSets;
import dev.thomasglasser.mineraculous.api.core.look.metadata.LookMetadataTypes;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetMiraculousLookDataPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;

// TODO: Remove when button is added
public class LookCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(Commands.literal("look")
                .executes(context -> {
                    Holder.Reference<Miraculous> miraculous = buildContext.lookup(MineraculousRegistries.MIRACULOUS).orElseThrow().listElements().findFirst().orElseThrow();
                    Minecraft.getInstance().setScreen(new LookCustomizationScreen<>(
                            LookContextSets.MIRACULOUS,
                            LookMetadataTypes.VALID_MIRACULOUSES,
                            miraculous,
                            player -> player.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).lookData(),
                            (player, lookData) -> player.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).withLookData(lookData).save(miraculous, player),
                            (player, lookData) -> TommyLibServices.NETWORK.sendToServer(new ServerboundSetMiraculousLookDataPayload(miraculous, lookData))));
                    return 1;
                }));
    }
}
