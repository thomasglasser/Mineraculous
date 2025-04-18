package dev.thomasglasser.mineraculous.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.core.registries.MineraculousRegistries;
import dev.thomasglasser.mineraculous.network.ClientboundOpenLookCustomizationScreenPayload;
import dev.thomasglasser.mineraculous.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.world.entity.Kwami;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.item.component.KwamiData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedLookDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Map;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class MiraculousCommand {
    public static final int COMMANDS_ENABLED_PERMISSION_LEVEL = 2;
    public static final String NOT_SET = "commands.miraculous.name.not_set";
    public static final String NAME_QUERY_SUCCESS_SELF = "commands.miraculous.name.query.success.self";
    public static final String NAME_QUERY_SUCCESS_OTHER = "commands.miraculous.name.query.success.other";
    public static final String NAME_SET_SUCCESS_SELF = "commands.miraculous.name.set.success.self";
    public static final String NAME_SET_SUCCESS_OTHER = "commands.miraculous.name.set.success.other";
    public static final String NAME_CLEAR_SUCCESS_SELF = "commands.miraculous.name.clear.success.self";
    public static final String NAME_CLEAR_SUCCESS_OTHER = "commands.miraculous.name.clear.success.other";
    public static final String CUSTOMIZE_OPEN_SUCCESS_SELF = "commands.miraculous.customize.open.success.self";
    public static final String CUSTOMIZE_OPEN_SUCCESS_OTHER = "commands.miraculous.customize.open.success.other";
    public static final String CHARGED_TRUE = "commands.miraculous.charged.true";
    public static final String CHARGED_FALSE = "commands.miraculous.charged.false";
    public static final String CHARGED_QUERY_SUCCESS_SELF = "commands.miraculous.charged.query.success.self";
    public static final String CHARGED_QUERY_SUCCESS_OTHER = "commands.miraculous.charged.query.success.other";
    public static final String CHARGED_SET_SUCCESS_SELF = "commands.miraculous.charged.set.success.self";
    public static final String CHARGED_SET_SUCCESS_OTHER = "commands.miraculous.charged.set.success.other";
    public static final String POWER_LEVEL_QUERY_SUCCESS_SELF = "commands.miraculous.power_level.query.success.self";
    public static final String POWER_LEVEL_QUERY_SUCCESS_OTHER = "commands.miraculous.power_level.query.success.other";
    public static final String POWER_LEVEL_SET_SUCCESS_SELF = "commands.miraculous.power_level.set.success.self";
    public static final String POWER_LEVEL_SET_SUCCESS_OTHER = "commands.miraculous.power_level.set.success.other";
    public static final String NOT_LIVING_ENTITY = "commands.miraculous.failure.not_living_entity";
    public static final String TRANSFORMED = "commands.miraculous.failure.transformed";
    public static final String KWAMI_NOT_FOUND = "commands.miraculous.failure.kwami_not_found";
    public static final String CUSTOM_LOOKS_NOT_ENABLED = "commands.miraculous.failure.custom_looks_not_enabled";
    public static final String CUSTOM_LOOKS_NO_NUMBERS = "commands.miraculous.failure.custom_looks_no_numbers";
    public static final String CUSTOM_LOOKS_NO_GLOWMASK = "commands.miraculous.failure.custom_looks_no_glowmask";
    public static final String MIRACULOUS_INVALID = "commands.miraculous.miraculous.invalid";
    public static final DynamicCommandExceptionType ERROR_INVALID_MIRACULOUS = new DynamicCommandExceptionType(
            p_304101_ -> Component.translatableEscape(MIRACULOUS_INVALID, p_304101_));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("miraculous")
                .then(Commands.argument("miraculous", ResourceKeyArgument.key(MineraculousRegistries.MIRACULOUS))
                        .then(Commands.literal("customize")
                                .executes(context -> tryOpenCustomizationScreen(context.getSource().getPlayerOrException(), context, true))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                            return tryOpenCustomizationScreen(target, context, target == context.getSource().getPlayer());
                                        })))
                        .then(Commands.literal("charged")
                                .requires(source -> source.hasPermission(COMMANDS_ENABLED_PERMISSION_LEVEL))
                                .then(Commands.literal("query")
                                        .executes(context -> getKwamiCharged(context.getSource().getPlayerOrException(), context, true))
                                        .then(Commands.argument("target", EntityArgument.entity())
                                                .requires(source -> source.hasPermission(COMMANDS_ENABLED_PERMISSION_LEVEL))
                                                .executes(context -> {
                                                    LivingEntity target = EntityArgument.getEntity(context, "target") instanceof LivingEntity livingEntity ? livingEntity : null;
                                                    return getKwamiCharged(target, context, target == context.getSource().getPlayer());
                                                })))
                                .then(Commands.argument("charged", BoolArgumentType.bool())
                                        .executes(context -> setKwamiCharged(context.getSource().getPlayerOrException(), context, true))
                                        .then(Commands.argument("target", EntityArgument.entity())
                                                .executes(context -> {
                                                    LivingEntity target = EntityArgument.getEntity(context, "target") instanceof LivingEntity livingEntity ? livingEntity : null;
                                                    return setKwamiCharged(target, context, target == context.getSource().getPlayer());
                                                }))))
                        .then(Commands.literal("power_level")
                                .requires(source -> source.hasPermission(COMMANDS_ENABLED_PERMISSION_LEVEL))
                                .then(Commands.literal("query")
                                        .executes(context -> getPowerLevel(context.getSource().getPlayerOrException(), context, true))
                                        .then(Commands.argument("target", EntityArgument.entity())
                                                .requires(source -> source.hasPermission(COMMANDS_ENABLED_PERMISSION_LEVEL))
                                                .executes(context -> {
                                                    LivingEntity target = EntityArgument.getEntity(context, "target") instanceof LivingEntity livingEntity ? livingEntity : null;
                                                    return getPowerLevel(target, context, target == context.getSource().getPlayer());
                                                })))
                                .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                        .executes(context -> setPowerLevel(context.getSource().getPlayerOrException(), context, true))
                                        .then(Commands.argument("target", EntityArgument.entity())
                                                .executes(context -> {
                                                    LivingEntity target = EntityArgument.getEntity(context, "target") instanceof LivingEntity livingEntity ? livingEntity : null;
                                                    return setPowerLevel(target, context, target == context.getSource().getPlayer());
                                                }))))));
    }

    private static int tryOpenCustomizationScreen(ServerPlayer player, CommandContext<CommandSourceStack> context, boolean self) throws CommandSyntaxException {
        Holder.Reference<Miraculous> miraculousType = resolveMiraculous(context, "miraculous");
        Map<String, FlattenedSuitLookData> serverSuits = ((FlattenedLookDataHolder) context.getSource().getServer().overworld()).mineraculous$getCommonSuitLookData().get(resolveMiraculous(context, "miraculous").key());
        Map<String, FlattenedMiraculousLookData> serverMiraculous = ((FlattenedLookDataHolder) context.getSource().getServer().overworld()).mineraculous$getCommonMiraculousLookData().get(resolveMiraculous(context, "miraculous").key());
        context.getSource().sendSuccess(() -> self ? Component.translatable(CUSTOMIZE_OPEN_SUCCESS_SELF, Component.translatable(Miraculous.toLanguageKey(miraculousType.key()))) : Component.translatable(CUSTOMIZE_OPEN_SUCCESS_OTHER, Component.translatable(Miraculous.toLanguageKey(miraculousType.key())), player.getDisplayName()), true);
        TommyLibServices.NETWORK.sendToClient(new ClientboundOpenLookCustomizationScreenPayload(context.getSource().getPlayer() == null ? Optional.empty() : Optional.of(context.getSource().getPlayer().getUUID()), true, miraculousType.key(), serverSuits, serverMiraculous), player);
        return 1;
    }

    private static int getKwamiCharged(LivingEntity livingEntity, CommandContext<CommandSourceStack> context, boolean self) throws CommandSyntaxException {
        if (livingEntity != null) {
            Holder.Reference<Miraculous> miraculousType = resolveMiraculous(context, "miraculous");
            MiraculousDataSet miraculousDataSet = livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS.get());
            MiraculousData data = miraculousDataSet.get(miraculousType.key());
            if (data.transformed()) {
                context.getSource().sendFailure(Component.translatable(TRANSFORMED, livingEntity.getDisplayName()));
                return 0;
            } else {
                KwamiData kwamiData = data.miraculousItem().get(MineraculousDataComponents.KWAMI_DATA.get());
                if (kwamiData != null) {
                    Entity entity = ((ServerLevel) livingEntity.level()).getEntity(kwamiData.uuid());
                    if (entity instanceof Kwami kwami) {
                        context.getSource().sendSuccess(() -> self ? Component.translatable(CHARGED_QUERY_SUCCESS_SELF, Component.translatable(Miraculous.toLanguageKey(miraculousType.key())), Component.translatable(kwami.isCharged() ? CHARGED_TRUE : CHARGED_FALSE)) : Component.translatable(CHARGED_QUERY_SUCCESS_OTHER, livingEntity.getDisplayName(), Component.translatable(Miraculous.toLanguageKey(miraculousType.key())), Component.translatable(kwami.isCharged() ? CHARGED_TRUE : CHARGED_FALSE)), true);
                        return 1;
                    } else {
                        context.getSource().sendFailure(Component.translatable(KWAMI_NOT_FOUND, livingEntity.getDisplayName(), Component.translatable(Miraculous.toLanguageKey(miraculousType.key()))));
                        return 0;
                    }
                } else {
                    context.getSource().sendFailure(Component.translatable(KWAMI_NOT_FOUND, livingEntity.getDisplayName(), Component.translatable(Miraculous.toLanguageKey(miraculousType.key()))));
                    return 0;
                }
            }
        } else {
            context.getSource().sendFailure(Component.translatable(NOT_LIVING_ENTITY));
        }
        return 0;
    }

    private static int setKwamiCharged(LivingEntity livingEntity, CommandContext<CommandSourceStack> context, boolean self) throws CommandSyntaxException {
        if (livingEntity != null) {
            Holder.Reference<Miraculous> miraculousType = resolveMiraculous(context, "miraculous");
            MiraculousDataSet miraculousDataSet = livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS.get());
            MiraculousData data = miraculousDataSet.get(miraculousType.key());
            if (data.transformed()) {
                context.getSource().sendFailure(Component.translatable(TRANSFORMED, livingEntity.getDisplayName()));
                return 0;
            } else {
                ItemStack miraculousItem = data.miraculousItem();
                if (miraculousItem.getCount() == 0)
                    miraculousItem.setCount(1);
                KwamiData kwamiData = miraculousItem.get(MineraculousDataComponents.KWAMI_DATA.get());
                if (kwamiData != null) {
                    Entity entity = ((ServerLevel) livingEntity.level()).getEntity(kwamiData.uuid());
                    boolean charged = BoolArgumentType.getBool(context, "charged");
                    if (entity instanceof Kwami kwami) {
                        kwami.setCharged(charged);
                        context.getSource().sendSuccess(() -> self ? Component.translatable(CHARGED_SET_SUCCESS_SELF, Component.translatable(Miraculous.toLanguageKey(miraculousType.key())), charged ? Component.translatable(CHARGED_TRUE) : Component.translatable(CHARGED_FALSE)) : Component.translatable(CHARGED_SET_SUCCESS_OTHER, livingEntity.getDisplayName(), Component.translatable(Miraculous.toLanguageKey(miraculousType.key())), charged ? Component.translatable(CHARGED_TRUE) : Component.translatable(CHARGED_FALSE)), true);
                        return 1;
                    } else {
                        context.getSource().sendFailure(Component.translatable(KWAMI_NOT_FOUND, livingEntity.getDisplayName(), Component.translatable(Miraculous.toLanguageKey(miraculousType.key()))));
                        return 0;
                    }
                } else {
                    context.getSource().sendFailure(Component.translatable(KWAMI_NOT_FOUND, livingEntity.getDisplayName(), Component.translatable(Miraculous.toLanguageKey(miraculousType.key()))));
                    return 0;
                }
            }
        } else {
            context.getSource().sendFailure(Component.translatable(NOT_LIVING_ENTITY));
        }
        return 0;
    }

    private static int getPowerLevel(LivingEntity livingEntity, CommandContext<CommandSourceStack> context, boolean self) throws CommandSyntaxException {
        if (livingEntity != null) {
            Holder.Reference<Miraculous> miraculousType = resolveMiraculous(context, "miraculous");
            MiraculousDataSet miraculousDataSet = livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS.get());
            MiraculousData data = miraculousDataSet.get(miraculousType.key());
            context.getSource().sendSuccess(() -> self ? Component.translatable(POWER_LEVEL_QUERY_SUCCESS_SELF, Component.translatable(Miraculous.toLanguageKey(miraculousType.key())), data.powerLevel()) : Component.translatable(POWER_LEVEL_QUERY_SUCCESS_OTHER, livingEntity.getDisplayName(), Component.translatable(Miraculous.toLanguageKey(miraculousType.key())), data.powerLevel()), true);
            return 1;
        } else {
            context.getSource().sendFailure(Component.translatable(NOT_LIVING_ENTITY));
        }
        return 0;
    }

    private static int setPowerLevel(LivingEntity livingEntity, CommandContext<CommandSourceStack> context, boolean self) throws CommandSyntaxException {
        if (livingEntity != null) {
            int newLevel = IntegerArgumentType.getInteger(context, "level");
            Holder.Reference<Miraculous> miraculousType = resolveMiraculous(context, "miraculous");
            MiraculousDataSet miraculousDataSet = livingEntity.getData(MineraculousAttachmentTypes.MIRACULOUS.get());
            MiraculousData data = miraculousDataSet.get(miraculousType.key());
            miraculousDataSet.put(livingEntity, miraculousType.key(), data.withLevel(newLevel), true);
            context.getSource().sendSuccess(() -> self ? Component.translatable(POWER_LEVEL_SET_SUCCESS_SELF, Component.translatable(Miraculous.toLanguageKey(miraculousType.key())), Math.clamp(newLevel, 0, 100)) : Component.translatable(POWER_LEVEL_SET_SUCCESS_OTHER, livingEntity.getDisplayName(), Component.translatable(Miraculous.toLanguageKey(miraculousType.key())), newLevel), true);
            return 1;
        } else {
            context.getSource().sendFailure(Component.translatable(NOT_LIVING_ENTITY));
        }
        return 0;
    }

    private static Holder.Reference<Miraculous> resolveMiraculous(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return ResourceKeyArgument.resolveKey(context, name, MineraculousRegistries.MIRACULOUS, ERROR_INVALID_MIRACULOUS);
    }
}
