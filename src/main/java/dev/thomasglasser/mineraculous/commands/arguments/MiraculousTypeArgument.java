package dev.thomasglasser.mineraculous.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import dev.thomasglasser.mineraculous.world.entity.MiraculousType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.StringRepresentableArgument;

public class MiraculousTypeArgument extends StringRepresentableArgument<MiraculousType>
{
	private MiraculousTypeArgument()
	{
		super(MiraculousType.CODEC, MiraculousType::values);
	}

	public static MiraculousTypeArgument miraculousType() {
		return new MiraculousTypeArgument();
	}

	public static MiraculousType getMiraculousType(CommandContext<CommandSourceStack> context, String name) {
		return context.getArgument(name, MiraculousType.class);
	}
}
