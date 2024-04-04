package dev.thomasglasser.mineraculous.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.RegistrationProvider;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;

public class MineraculousCommandArgumentTypes
{
	public static final RegistrationProvider<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = RegistrationProvider.get(Registries.COMMAND_ARGUMENT_TYPE, Mineraculous.MOD_ID);

	public static final RegistryObject<ArgumentTypeInfo<MiraculousTypeArgument, SingletonArgumentInfo<MiraculousTypeArgument>.Template>> MIRACULOUS_TYPE = register("miraculous_type", MiraculousTypeArgument.class, SingletonArgumentInfo.contextFree(MiraculousTypeArgument::miraculousType));

	private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> RegistryObject<ArgumentTypeInfo<A, T>> register(String name, Class<? extends A> argumentClass, ArgumentTypeInfo<A, T> info)
	{
		ArgumentTypeInfos.BY_CLASS.put(argumentClass, info);
		return COMMAND_ARGUMENT_TYPES.register(name, () -> info);
	}

	public static void init() {}
}
