package dev.thomasglasser.mineraculous.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.thomasglasser.mineraculous.commands.arguments.MiraculousTypeArgument;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public class MiraculousCommand
{
	public static final String NAME_SUCCESS_SELF = "commands.miraculous.name.success.self";
	public static final String NAME_SUCCESS_OTHER = "commands.miraculous.name.success.other";
	public static final String NAME_CLEAR_SUCCESS_SELF = "commands.miraculous.name.clear.success.self";
	public static final String NAME_CLEAR_SUCCESS_OTHER = "commands.miraculous.name.clear.success.other";
	public static final String NOT_LIVING_ENTITY = "commands.miraculous.failure.not_living_entity";

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("miraculous")
				.then(Commands.argument("miraculous_type", MiraculousTypeArgument.miraculousType())
					.then(Commands.literal("name")
							.then(Commands.argument("name", StringArgumentType.string())
									.executes(context -> setName(context.getSource().getPlayer(), context, true))
									.then(Commands.argument("target", EntityArgument.entity())
											.requires(source -> source.hasPermission(2))
											.executes(context ->
											{
												LivingEntity target = EntityArgument.getEntity(context, "target") instanceof LivingEntity livingEntity ? livingEntity : null;
												return setName(target, context, target == context.getSource().getPlayer());
											})))
							.then(Commands.literal("clear")
									.executes(context -> clearName(context.getSource().getPlayer(), context, true))
									.then(Commands.argument("target", EntityArgument.entity())
											.requires(source -> source.hasPermission(2))
											.executes(context ->
											{
												LivingEntity target = EntityArgument.getEntity(context, "target") instanceof LivingEntity livingEntity ? livingEntity : null;
												return clearName(target, context, target == context.getSource().getPlayer());
											}))))));
	}

	private static int setName(LivingEntity livingEntity, CommandContext<CommandSourceStack> context, boolean self)
	{
		if (livingEntity != null)
		{
			Component oldName = livingEntity.getDisplayName();
			MiraculousType miraculousType = MiraculousTypeArgument.getMiraculousType(context, "miraculous_type");
			String newName = StringArgumentType.getString(context, "name");
			MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(livingEntity);
			MiraculousData data = miraculousDataSet.get(miraculousType);
			miraculousDataSet.put(livingEntity, miraculousType, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), data.powerLevel(), data.powerActivated(), data.powerActive(), newName), true);
			context.getSource().sendSuccess(() -> Component.translatable(self ? NAME_SUCCESS_SELF : NAME_SUCCESS_OTHER, Component.translatable(miraculousType.getTranslationKey()), newName, oldName), true);
			return 1;
		}
		else
			context.getSource().sendFailure(Component.translatable(NOT_LIVING_ENTITY));
		return 0;
	}

	private static int clearName(LivingEntity livingEntity, CommandContext<CommandSourceStack> context, boolean self)
	{
		if (livingEntity != null)
		{
			MiraculousType miraculousType = MiraculousTypeArgument.getMiraculousType(context, "miraculous_type");
			MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(livingEntity);
			MiraculousData data = miraculousDataSet.get(miraculousType);
			miraculousDataSet.put(livingEntity, miraculousType, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), data.powerLevel(), data.powerActivated(), data.powerActive(), ""), true);
			context.getSource().sendSuccess(() -> Component.translatable(self ? NAME_CLEAR_SUCCESS_SELF : NAME_CLEAR_SUCCESS_OTHER, Component.translatable(miraculousType.getTranslationKey()), livingEntity.getName()), true);
			return 1;
		}
		else
			context.getSource().sendFailure(Component.translatable(NOT_LIVING_ENTITY));
		return 0;
	}
}
