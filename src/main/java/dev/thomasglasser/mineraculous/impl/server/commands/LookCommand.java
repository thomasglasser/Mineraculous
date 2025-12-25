package dev.thomasglasser.mineraculous.impl.server.commands;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.thomasglasser.mineraculous.api.client.look.Look;
import dev.thomasglasser.mineraculous.api.client.look.LookManager;
import dev.thomasglasser.mineraculous.api.client.look.asset.BuiltInLookAssets;
import dev.thomasglasser.mineraculous.api.core.look.LookData;
import dev.thomasglasser.mineraculous.api.core.look.context.LookContext;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousBuiltInRegistries;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetMiraculousLookDataPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

// TODO: Remove when screen is added
public class LookCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("look")
                .then(Commands.literal("equip")
                        .then(Commands.argument("miraculous", ResourceArgument.resource(context, MineraculousRegistries.MIRACULOUS))
                                .then(Commands.argument("hash", StringArgumentType.string())
                                        .suggests(SUGGEST_LOADED)
                                        .executes(LookCommand::equipLoaded))
                                .then(Commands.argument("id", ResourceLocationArgument.id())
                                        .suggests(SUGGEST_BUILT_IN)
                                        .executes(LookCommand::equipBuiltIn))
                                .executes(LookCommand::unequip))));
    }

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_LOADED = (context, builder) -> SharedSuggestionProvider.suggest(LookManager.getEquippable(), builder);
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_BUILT_IN = (context, builder) -> SharedSuggestionProvider.suggest(LookManager.getBuiltIn().stream().map(ResourceLocation::toString).toList(), builder);

    private static int equipLoaded(CommandContext<CommandSourceStack> context) {
        try {
            var miraculous = ResourceArgument.getResource(context, "miraculous", MineraculousRegistries.MIRACULOUS);
            String hash = StringArgumentType.getString(context, "hash");

            Look<?> look = LookManager.getEquippableLook(hash);
            if (look != null) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetMiraculousLookDataPayload(miraculous, new LookData(Optional.empty(), Util.make(new ImmutableMap.Builder<ResourceKey<LookContext>, ResourceLocation>(), builder -> {
                    for (ResourceKey<LookContext> lookContext : MineraculousBuiltInRegistries.LOOK_CONTEXT.registryKeySet())
                        builder.put(lookContext, ResourceLocation.withDefaultNamespace(hash));
                }).build())));
                context.getSource().sendSuccess(() -> Component.literal("Equipped look: " + hash), true);
            } else {
                context.getSource().sendFailure(Component.literal("Look not found: " + hash));
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 1;
    }

    private static int equipBuiltIn(CommandContext<CommandSourceStack> context) {
        try {
            var miraculous = ResourceArgument.getResource(context, "miraculous", MineraculousRegistries.MIRACULOUS);
            ResourceLocation id = ResourceLocationArgument.getId(context, "id");

            Look<BuiltInLookAssets> look = LookManager.getBuiltInLook(id);
            if (look != null) {
                TommyLibServices.NETWORK.sendToServer(new ServerboundSetMiraculousLookDataPayload(miraculous, new LookData(Optional.empty(), Util.make(new ImmutableMap.Builder<ResourceKey<LookContext>, ResourceLocation>(), builder -> {
                    for (ResourceKey<LookContext> lookContext : MineraculousBuiltInRegistries.LOOK_CONTEXT.registryKeySet())
                        builder.put(lookContext, id);
                }).build())));
                context.getSource().sendSuccess(() -> Component.literal("Equipped look: " + id), true);
            } else {
                context.getSource().sendFailure(Component.literal("Look not found: " + id));
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 1;
    }

    private static int unequip(CommandContext<CommandSourceStack> context) {
        try {
            var miraculous = ResourceArgument.getResource(context, "miraculous", MineraculousRegistries.MIRACULOUS);

            TommyLibServices.NETWORK.sendToServer(new ServerboundSetMiraculousLookDataPayload(miraculous, new LookData(Optional.empty(), ImmutableMap.of())));
            context.getSource().sendSuccess(() -> Component.literal("Unequipped look."), true);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
        }
        return 1;
    }
}
