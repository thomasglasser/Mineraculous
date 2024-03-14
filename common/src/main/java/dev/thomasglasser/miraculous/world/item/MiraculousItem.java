package dev.thomasglasser.miraculous.world.item;

import dev.thomasglasser.miraculous.client.renderer.MiraculousBlockEntityWithoutLevelRenderer;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MiraculousItem extends Item implements ModeledItem
{
	public static final String TAG_POWERED = "Powered";
	public static final String TAG_HOLDER = "Holder";

	private BlockEntityWithoutLevelRenderer bewlr;

	public MiraculousItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public ItemStack getDefaultInstance()
	{
		ItemStack stack = super.getDefaultInstance();
		stack.getOrCreateTag().putBoolean(TAG_POWERED, true);
		return stack;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected)
	{
		super.inventoryTick(stack, level, entity, slotId, isSelected);
		if (entity instanceof Player && !stack.getOrCreateTag().getString(TAG_HOLDER).equals(entity.getName().getString()))
		{
			stack.getOrCreateTag().putString(TAG_HOLDER, entity.getName().getString());
		}
	}

	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
		ItemStack itemStack = player.getItemInHand(usedHand);
		boolean powered = itemStack.getOrCreateTag().getBoolean(TAG_POWERED);
		// TODO: Implement right click behavior, procedure makes no sense
		itemStack.getOrCreateTag().putBoolean(TAG_POWERED, !powered);
		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
	}

	public boolean isPowered(ItemStack stack)
	{
		return stack.getOrCreateTag().getBoolean(TAG_POWERED);
	}

	public String getHolder(ItemStack stack)
	{
		return stack.getOrCreateTag().getString(TAG_HOLDER);
	}

	@Override
	public BlockEntityWithoutLevelRenderer getBEWLR()
	{
		if (bewlr == null)
		{
			bewlr = new MiraculousBlockEntityWithoutLevelRenderer();
		}
		return bewlr;
	}
}
