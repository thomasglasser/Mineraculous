package dev.thomasglasser.mineraculous.impl.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.thomasglasser.mineraculous.api.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousData;
import dev.thomasglasser.mineraculous.api.world.miraculous.MiraculousesData;
import dev.thomasglasser.mineraculous.impl.world.entity.Kwami;
import dev.thomasglasser.mineraculous.impl.world.item.component.KwamiData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class MiraculousCommand {
    public static final int COMMANDS_ENABLED_PERMISSION_LEVEL = 2;
    // Customize
    public static final String CUSTOMIZE_OPEN_SUCCESS_SELF = "commands.miraculous.customize.open.success.self";
    public static final String CUSTOMIZE_OPEN_SUCCESS_OTHER = "commands.miraculous.customize.open.success.other";
    // Charged
    public static final Component CHARGED_TRUE = Component.translatable("commands.miraculous.charged.true");
    public static final Component CHARGED_FALSE = Component.translatable("commands.miraculous.charged.false");
    public static final String CHARGED_QUERY_SUCCESS_SELF = "commands.miraculous.charged.query.success.self";
    public static final String CHARGED_QUERY_SUCCESS_OTHER = "commands.miraculous.charged.query.success.other";
    public static final String CHARGED_SET_SUCCESS_SELF = "commands.miraculous.charged.set.success.self";
    public static final String CHARGED_SET_SUCCESS_OTHER = "commands.miraculous.charged.set.success.other";
    public static final String CHARGED_FAILURE_TRANSFORMED = "commands.miraculous.charged.failure.transformed";
    public static final String CHARGED_FAILURE_KWAMI_NOT_FOUND_SELF = "commands.miraculous.charged.failure.kwami_not_found.self";
    public static final String CHARGED_FAILURE_KWAMI_NOT_FOUND_OTHER = "commands.miraculous.charged.failure.kwami_not_found.other";
    // Power Level
    public static final String POWER_LEVEL_QUERY_SUCCESS_SELF = "commands.miraculous.power_level.query.success.self";
    public static final String POWER_LEVEL_QUERY_SUCCESS_OTHER = "commands.miraculous.power_level.query.success.other";
    public static final String POWER_LEVEL_SET_SUCCESS_SELF = "commands.miraculous.power_level.set.success.self";
    public static final String POWER_LEVEL_SET_SUCCESS_OTHER = "commands.miraculous.power_level.set.success.other";
    // Exceptions
    public static final String EXCEPTION_MIRACULOUS_INVALID = "commands.miraculous.miraculous.invalid";
    public static final DynamicCommandExceptionType ERROR_INVALID_MIRACULOUS = new DynamicCommandExceptionType(
            arg -> Component.translatableEscape(EXCEPTION_MIRACULOUS_INVALID, arg));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("miraculous")
                .then(Commands.argument("miraculous", ResourceKeyArgument.key(MineraculousRegistries.MIRACULOUS))
                        .then(Commands.literal("charged")
                                .requires(source -> source.hasPermission(COMMANDS_ENABLED_PERMISSION_LEVEL))
                                .then(Commands.literal("query")
                                        .executes(context -> getKwamiCharged(context.getSource().getEntityOrException(), context, true))
                                        .then(Commands.argument("target", EntityArgument.entity())
                                                .executes(context -> {
                                                    Entity target = EntityArgument.getEntity(context, "target");
                                                    return getKwamiCharged(EntityArgument.getEntity(context, "target"), context, target == context.getSource().getEntity());
                                                })))
                                .then(Commands.argument("charged", BoolArgumentType.bool())
                                        .executes(context -> setKwamiCharged(context.getSource().getEntityOrException(), context, true))
                                        .then(Commands.argument("target", EntityArgument.entity())
                                                .executes(context -> {
                                                    Entity target = EntityArgument.getEntity(context, "target");
                                                    return setKwamiCharged(target, context, target == context.getSource().getEntity());
                                                }))))
                        .then(Commands.literal("power_level")
                                .requires(source -> source.hasPermission(COMMANDS_ENABLED_PERMISSION_LEVEL))
                                .then(Commands.literal("query")
                                        .executes(context -> getPowerLevel(context.getSource().getEntityOrException(), context, true))
                                        .then(Commands.argument("target", EntityArgument.entity())
                                                .executes(context -> {
                                                    Entity target = EntityArgument.getEntity(context, "target");
                                                    return getPowerLevel(target, context, target == context.getSource().getEntity());
                                                })))
                                .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                        .executes(context -> setPowerLevel(context.getSource().getEntityOrException(), context, true))
                                        .then(Commands.argument("target", EntityArgument.entity())
                                                .executes(context -> {
                                                    Entity target = EntityArgument.getEntity(context, "target");
                                                    return setPowerLevel(target, context, target == context.getSource().getEntity());
                                                }))))));
    }

    private static int getKwamiCharged(Entity entity, CommandContext<CommandSourceStack> context, boolean self) throws CommandSyntaxException {
        Holder.Reference<Miraculous> miraculous = resolveMiraculous(context, "miraculous");
        MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
        MiraculousData data = miraculousesData.get(miraculous);
        if (data.transformed()) {
            context.getSource().sendFailure(Component.translatable(CHARGED_FAILURE_TRANSFORMED, entity.getDisplayName()));
            return 0;
        } else {
            KwamiData kwamiData = data.kwamiData().orElse(null);
            if (kwamiData != null) {
                Kwami kwami = ((ServerLevel) entity.level()).getEntity(kwamiData.uuid()) instanceof Kwami k ? k : null;
                if (kwami != null) {
                    context.getSource().sendSuccess(() -> self ? Component.translatable(CHARGED_QUERY_SUCCESS_SELF, Component.translatable(Miraculous.toLanguageKey(miraculous.key())), kwami.isCharged() ? CHARGED_TRUE : CHARGED_FALSE) : Component.translatable(CHARGED_QUERY_SUCCESS_OTHER, entity.getDisplayName(), Component.translatable(Miraculous.toLanguageKey(miraculous.key())), kwami.isCharged() ? CHARGED_TRUE : CHARGED_FALSE), true);
                    return 1;
                } else {
                    context.getSource().sendFailure(self ? Component.translatable(CHARGED_FAILURE_KWAMI_NOT_FOUND_SELF, Component.translatable(Miraculous.toLanguageKey(miraculous.key()))) : Component.translatable(CHARGED_FAILURE_KWAMI_NOT_FOUND_OTHER, entity.getDisplayName(), Component.translatable(Miraculous.toLanguageKey(miraculous.key()))));
                    return 0;
                }
            } else {
                context.getSource().sendFailure(Component.translatable(CHARGED_FAILURE_KWAMI_NOT_FOUND_SELF, Component.translatable(Miraculous.toLanguageKey(miraculous.key()))));
                return 0;
            }
        }
    }

    private static int setKwamiCharged(Entity entity, CommandContext<CommandSourceStack> context, boolean self) throws CommandSyntaxException {
        Holder.Reference<Miraculous> miraculous = resolveMiraculous(context, "miraculous");
        MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
        MiraculousData data = miraculousesData.get(miraculous);
        if (data.transformed()) {
            context.getSource().sendFailure(Component.translatable(CHARGED_FAILURE_TRANSFORMED, entity.getDisplayName()));
            return 0;
        } else {
            KwamiData kwamiData = data.kwamiData().orElse(null);
            if (kwamiData != null) {
                boolean charged = BoolArgumentType.getBool(context, "charged");
                Kwami kwami = ((ServerLevel) entity.level()).getEntity(kwamiData.uuid()) instanceof Kwami k ? k : null;
                if (kwami != null) {
                    kwami.setCharged(charged);
                    context.getSource().sendSuccess(() -> self ? Component.translatable(CHARGED_SET_SUCCESS_SELF, Component.translatable(Miraculous.toLanguageKey(miraculous.key())), charged ? CHARGED_TRUE : CHARGED_FALSE) : Component.translatable(CHARGED_SET_SUCCESS_OTHER, entity.getDisplayName(), Component.translatable(Miraculous.toLanguageKey(miraculous.key())), charged ? CHARGED_TRUE : CHARGED_FALSE), true);
                    return 1;
                } else {
                    context.getSource().sendFailure(Component.translatable(CHARGED_FAILURE_KWAMI_NOT_FOUND_SELF, Component.translatable(Miraculous.toLanguageKey(miraculous.key()))));
                    return 0;
                }
            } else {
                context.getSource().sendFailure(Component.translatable(CHARGED_FAILURE_KWAMI_NOT_FOUND_SELF, Component.translatable(Miraculous.toLanguageKey(miraculous.key()))));
                return 0;
            }
        }
    }

    private static int getPowerLevel(Entity entity, CommandContext<CommandSourceStack> context, boolean self) throws CommandSyntaxException {
        Holder.Reference<Miraculous> miraculousType = resolveMiraculous(context, "miraculous");
        MiraculousesData miraculousesData = entity.getData(MineraculousAttachmentTypes.MIRACULOUSES);
        MiraculousData data = miraculousesData.get(miraculousType);
        context.getSource().sendSuccess(() -> self ? Component.translatable(POWER_LEVEL_QUERY_SUCCESS_SELF, Component.translatable(Miraculous.toLanguageKey(miraculousType.key())), data.powerLevel()) : Component.translatable(POWER_LEVEL_QUERY_SUCCESS_OTHER, entity.getDisplayName(), Component.translatable(Miraculous.toLanguageKey(miraculousType.key())), data.powerLevel()), true);
        return 1;
    }

    private static int setPowerLevel(Entity entity, CommandContext<CommandSourceStack> context, boolean self) throws CommandSyntaxException {
        int newLevel = Math.clamp(IntegerArgumentType.getInteger(context, "level"), 0, 100);
        Holder.Reference<Miraculous> miraculous = resolveMiraculous(context, "miraculous");
        entity.getData(MineraculousAttachmentTypes.MIRACULOUSES).get(miraculous).withPowerLevel(newLevel).save(miraculous, entity, true);
        context.getSource().sendSuccess(() -> self ? Component.translatable(POWER_LEVEL_SET_SUCCESS_SELF, Component.translatable(Miraculous.toLanguageKey(miraculous.key())), newLevel) : Component.translatable(POWER_LEVEL_SET_SUCCESS_OTHER, entity.getDisplayName(), Component.translatable(Miraculous.toLanguageKey(miraculous.key())), newLevel), true);
        return 1;
    }

    public static Holder.Reference<Miraculous> resolveMiraculous(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return ResourceKeyArgument.resolveKey(context, name, MineraculousRegistries.MIRACULOUS, ERROR_INVALID_MIRACULOUS);
    }
}
