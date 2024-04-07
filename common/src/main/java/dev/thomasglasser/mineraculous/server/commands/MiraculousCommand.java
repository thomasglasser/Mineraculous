package dev.thomasglasser.mineraculous.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.thomasglasser.mineraculous.commands.arguments.MiraculousTypeArgument;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import dev.thomasglasser.mineraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousDataSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class MiraculousCommand
{
	public static final int COMMANDS_ENABLED_PERMISSION_LEVEL = 2;
	public static final String NOT_SET = "commands.miraculous.name.not_set";
	public static final String NAME_QUERY_SUCCESS_SELF = "commands.miraculous.name.query.success.self";
	public static final String NAME_QUERY_SUCCESS_OTHER = "commands.miraculous.name.query.success.other";
	public static final String NAME_SET_SUCCESS_SELF = "commands.miraculous.name.set.success.self";
	public static final String NAME_SET_SUCCESS_OTHER = "commands.miraculous.name.set.success.other";
	public static final String NAME_CLEAR_SUCCESS_SELF = "commands.miraculous.name.clear.success.self";
	public static final String NAME_CLEAR_SUCCESS_OTHER = "commands.miraculous.name.clear.success.other";
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

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		dispatcher.register(Commands.literal("miraculous")
				.then(Commands.argument("miraculous_type", MiraculousTypeArgument.miraculousType())
						.then(Commands.literal("name")
								.then(Commands.literal("query")
										.executes(context -> getName(context.getSource().getPlayer(), context, true))
										.then(Commands.argument("target", EntityArgument.entity())
												.requires(source -> source.hasPermission(COMMANDS_ENABLED_PERMISSION_LEVEL))
												.executes(context ->
												{
													LivingEntity target = EntityArgument.getEntity(context, "target") instanceof LivingEntity livingEntity ? livingEntity : null;
													return getName(target, context, target == context.getSource().getPlayer());
												})))
							.then(Commands.argument("name", StringArgumentType.string())
										.executes(context -> setName(context.getSource().getPlayer(), context, true))
										.then(Commands.argument("target", EntityArgument.entity())
												.requires(source -> source.hasPermission(COMMANDS_ENABLED_PERMISSION_LEVEL))
												.executes(context ->
												{
													LivingEntity target = EntityArgument.getEntity(context, "target") instanceof LivingEntity livingEntity ? livingEntity : null;
													return setName(target, context, target == context.getSource().getPlayer());
												})))
								.then(Commands.literal("clear")
										.executes(context -> clearName(context.getSource().getPlayer(), context, true))
										.then(Commands.argument("target", EntityArgument.entity())
												.requires(source -> source.hasPermission(COMMANDS_ENABLED_PERMISSION_LEVEL))
												.executes(context ->
												{
													LivingEntity target = EntityArgument.getEntity(context, "target") instanceof LivingEntity livingEntity ? livingEntity : null;
													return clearName(target, context, target == context.getSource().getPlayer());
												}))))
						.then(Commands.literal("charged")
								.requires(source -> source.hasPermission(COMMANDS_ENABLED_PERMISSION_LEVEL))
								.then(Commands.literal("query")
										.executes(context -> getKwamiCharged(context.getSource().getPlayer(), context, true))
										.then(Commands.argument("target", EntityArgument.entity())
												.requires(source -> source.hasPermission(COMMANDS_ENABLED_PERMISSION_LEVEL))
												.executes(context ->
												{
													LivingEntity target = EntityArgument.getEntity(context, "target") instanceof LivingEntity livingEntity ? livingEntity : null;
													return getKwamiCharged(target, context, target == context.getSource().getPlayer());
												})))
								.then(Commands.argument("charged", BoolArgumentType.bool())
										.executes(context -> setKwamiCharged(context.getSource().getPlayer(), context, true))
										.then(Commands.argument("target", EntityArgument.entity())
												.executes(context ->
												{
													LivingEntity target = EntityArgument.getEntity(context, "target") instanceof LivingEntity livingEntity ? livingEntity : null;
													return setKwamiCharged(target, context, target == context.getSource().getPlayer());
												}))))
						.then(Commands.literal("power_level")
								.requires(source -> source.hasPermission(COMMANDS_ENABLED_PERMISSION_LEVEL))
								.then(Commands.literal("query")
										.executes(context -> getPowerLevel(context.getSource().getPlayer(), context, true))
										.then(Commands.argument("target", EntityArgument.entity())
												.requires(source -> source.hasPermission(COMMANDS_ENABLED_PERMISSION_LEVEL))
												.executes(context ->
												{
													LivingEntity target = EntityArgument.getEntity(context, "target") instanceof LivingEntity livingEntity ? livingEntity : null;
													return getPowerLevel(target, context, target == context.getSource().getPlayer());
												})))
								.then(Commands.argument("level", IntegerArgumentType.integer(0))
										.executes(context -> setPowerLevel(context.getSource().getPlayer(), context, true))
										.then(Commands.argument("target", EntityArgument.entity())
												.executes(context ->
												{
													LivingEntity target = EntityArgument.getEntity(context, "target") instanceof LivingEntity livingEntity ? livingEntity : null;
													return setPowerLevel(target, context, target == context.getSource().getPlayer());
												}))))));
	}

	private static int getName(LivingEntity entity, CommandContext<CommandSourceStack> context, boolean self)
	{
		if (entity != null)
		{
			MiraculousType miraculousType = MiraculousTypeArgument.getMiraculousType(context, "miraculous_type");
			MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(entity);
			MiraculousData data = miraculousDataSet.get(miraculousType);
			context.getSource().sendSuccess(() -> self ? Component.translatable(NAME_QUERY_SUCCESS_SELF, Component.translatable(miraculousType.getTranslationKey()), data.name().isEmpty() ? Component.translatable(NOT_SET) : data.name()) : Component.translatable(NAME_QUERY_SUCCESS_OTHER, entity.getDisplayName(), Component.translatable(miraculousType.getTranslationKey()), data.name().isEmpty() ? Component.translatable(NOT_SET) : data.name()), true);
			return 1;
		}
		else
		{
			context.getSource().sendFailure(Component.translatable(NOT_LIVING_ENTITY));
		}
		return 0;
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
			miraculousDataSet.put(livingEntity, miraculousType, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), data.powerLevel(), data.mainPowerActivated(), data.mainPowerActive(), newName), true);
			context.getSource().sendSuccess(() -> self ? Component.translatable(NAME_SET_SUCCESS_SELF, Component.translatable(miraculousType.getTranslationKey()), newName) : Component.translatable(NAME_SET_SUCCESS_OTHER, oldName, Component.translatable(miraculousType.getTranslationKey()), newName), true);
			return 1;
		}
		else
		{
			context.getSource().sendFailure(Component.translatable(NOT_LIVING_ENTITY));
		}
		return 0;
	}

	private static int clearName(LivingEntity livingEntity, CommandContext<CommandSourceStack> context, boolean self)
	{
		if (livingEntity != null)
		{
			MiraculousType miraculousType = MiraculousTypeArgument.getMiraculousType(context, "miraculous_type");
			MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(livingEntity);
			MiraculousData data = miraculousDataSet.get(miraculousType);
			miraculousDataSet.put(livingEntity, miraculousType, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), data.powerLevel(), data.mainPowerActivated(), data.mainPowerActive(), ""), true);
			context.getSource().sendSuccess(() -> self ? Component.translatable(NAME_CLEAR_SUCCESS_SELF, Component.translatable(miraculousType.getTranslationKey())) : Component.translatable(NAME_CLEAR_SUCCESS_OTHER, livingEntity.getDisplayName(), Component.translatable(miraculousType.getTranslationKey())), true);
			return 1;
		}
		else
		{
			context.getSource().sendFailure(Component.translatable(NOT_LIVING_ENTITY));
		}
		return 0;
	}

	private static int getKwamiCharged(LivingEntity livingEntity, CommandContext<CommandSourceStack> context, boolean self)
	{
		if (livingEntity != null)
		{
			MiraculousType miraculousType = MiraculousTypeArgument.getMiraculousType(context, "miraculous_type");
			MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(livingEntity);
			MiraculousData data = miraculousDataSet.get(miraculousType);
			if (data.transformed())
			{
				context.getSource().sendFailure(Component.translatable(TRANSFORMED, livingEntity.getDisplayName()));
				return 0;
			}
			else
			{
				CompoundTag kwamiData = data.miraculousItem().getOrCreateTag().getCompound(MiraculousItem.TAG_KWAMIDATA);
				if (kwamiData.hasUUID("UUID"))
				{
					Entity entity = ((ServerLevel)livingEntity.level()).getEntity(kwamiData.getUUID("UUID"));
					if (entity instanceof Kwami kwami)
					{
						context.getSource().sendSuccess(() -> self ? Component.translatable(CHARGED_QUERY_SUCCESS_SELF, Component.translatable(miraculousType.getTranslationKey()), Component.translatable(kwami.isCharged() ? CHARGED_TRUE : CHARGED_FALSE)) : Component.translatable(CHARGED_QUERY_SUCCESS_OTHER, livingEntity.getDisplayName(), Component.translatable(miraculousType.getTranslationKey()), Component.translatable(kwami.isCharged() ? CHARGED_TRUE : CHARGED_FALSE)), true);
						return 1;
					}
					else
					{
						context.getSource().sendFailure(Component.translatable(KWAMI_NOT_FOUND, livingEntity.getDisplayName(), Component.translatable(miraculousType.getTranslationKey())));
						return 0;
					}
				}
				else
				{
					context.getSource().sendFailure(Component.translatable(KWAMI_NOT_FOUND, livingEntity.getDisplayName(), Component.translatable(miraculousType.getTranslationKey())));
					return 0;
				}
			}
		}
		else
		{
			context.getSource().sendFailure(Component.translatable(NOT_LIVING_ENTITY));
		}
		return 0;
	}

	private static int setKwamiCharged(LivingEntity livingEntity, CommandContext<CommandSourceStack> context, boolean self)
	{
		if (livingEntity != null)
		{
			MiraculousType miraculousType = MiraculousTypeArgument.getMiraculousType(context, "miraculous_type");
			MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(livingEntity);
			MiraculousData data = miraculousDataSet.get(miraculousType);
			if (data.transformed())
			{
				context.getSource().sendFailure(Component.translatable(TRANSFORMED, livingEntity.getDisplayName()));
				return 0;
			}
			else
			{
				CompoundTag kwamiData = data.miraculousItem().getOrCreateTag().getCompound(MiraculousItem.TAG_KWAMIDATA);
				if (kwamiData.hasUUID("UUID"))
				{
					Entity entity = ((ServerLevel)livingEntity.level()).getEntity(kwamiData.getUUID("UUID"));
					boolean charged = BoolArgumentType.getBool(context, "charged");
					if (entity instanceof Kwami kwami)
					{
						kwami.setCharged(charged);
						context.getSource().sendSuccess(() -> self ? Component.translatable(CHARGED_SET_SUCCESS_SELF, Component.translatable(miraculousType.getTranslationKey()), charged) : Component.translatable(CHARGED_SET_SUCCESS_OTHER, livingEntity.getDisplayName(), Component.translatable(miraculousType.getTranslationKey()), charged), true);
						return 1;
					}
					else
					{
						context.getSource().sendFailure(Component.translatable(KWAMI_NOT_FOUND, livingEntity.getDisplayName(), Component.translatable(miraculousType.getTranslationKey())));
						return 0;
					}
				}
				else
				{
					context.getSource().sendFailure(Component.translatable(KWAMI_NOT_FOUND, livingEntity.getDisplayName(), Component.translatable(miraculousType.getTranslationKey())));
					return 0;
				}
			}
		}
		else
		{
			context.getSource().sendFailure(Component.translatable(NOT_LIVING_ENTITY));
		}
		return 0;
	}

	private static int getPowerLevel(LivingEntity livingEntity, CommandContext<CommandSourceStack> context, boolean self)
	{
		if (livingEntity != null)
		{
			MiraculousType miraculousType = MiraculousTypeArgument.getMiraculousType(context, "miraculous_type");
			MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(livingEntity);
			MiraculousData data = miraculousDataSet.get(miraculousType);
			context.getSource().sendSuccess(() -> self ? Component.translatable(POWER_LEVEL_QUERY_SUCCESS_SELF, Component.translatable(miraculousType.getTranslationKey()), data.powerLevel()) : Component.translatable(POWER_LEVEL_QUERY_SUCCESS_OTHER, livingEntity.getDisplayName(), Component.translatable(miraculousType.getTranslationKey()), data.powerLevel()), true);
			return 1;
		}
		else
		{
			context.getSource().sendFailure(Component.translatable(NOT_LIVING_ENTITY));
		}
		return 0;
	}

	private static int setPowerLevel(LivingEntity livingEntity, CommandContext<CommandSourceStack> context, boolean self)
	{
		if (livingEntity != null)
		{
			int newLevel = IntegerArgumentType.getInteger(context, "level");
			MiraculousType miraculousType = MiraculousTypeArgument.getMiraculousType(context, "miraculous_type");
			MiraculousDataSet miraculousDataSet = Services.DATA.getMiraculousDataSet(livingEntity);
			MiraculousData data = miraculousDataSet.get(miraculousType);
			miraculousDataSet.put(livingEntity, miraculousType, new MiraculousData(data.transformed(), data.miraculousItem(), data.curiosData(), data.tool(), newLevel, data.mainPowerActivated(), data.mainPowerActive(), data.name()), true);
			context.getSource().sendSuccess(() -> self ? Component.translatable(POWER_LEVEL_SET_SUCCESS_SELF, Component.translatable(miraculousType.getTranslationKey()), newLevel) : Component.translatable(POWER_LEVEL_SET_SUCCESS_OTHER, livingEntity.getDisplayName(), Component.translatable(miraculousType.getTranslationKey()), newLevel), true);
			return 1;
		}
		else
		{
			context.getSource().sendFailure(Component.translatable(NOT_LIVING_ENTITY));
		}
		return 0;
	}
}
