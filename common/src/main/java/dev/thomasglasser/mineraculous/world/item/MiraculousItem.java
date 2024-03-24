package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.mineraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.tommylib.api.world.item.BaseModeledItem;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

public class MiraculousItem extends BaseModeledItem
{
	public static final String TAG_POWERED = "Powered";
	public static final String TAG_HOLDER = "Holder";
	public static final String TAG_KWAMIDATA = "KwamiData";
	public static final String TAG_RECALLED = "Recalled";
	public static final String TAG_TRANSFORMTICKS = "TransformTicks";
	public static final String TAG_DETRANSFORMTICKS = "DetransformTicks";

	private final ArmorSet armor;
	private final Supplier<Item> tool;
	private final SoundEvent transformSound;
	private final Supplier<EntityType<? extends Kwami>> kwamiType;
	private final List<String> acceptableSlots;

	public MiraculousItem(Properties properties, ArmorSet armor, Supplier<Item> tool, SoundEvent transformSound, Supplier<EntityType<? extends Kwami>> kwamiType, List<String> acceptableSlots)
	{
		super(properties.stacksTo(1).fireResistant().rarity(Rarity.EPIC));
		this.armor = armor;
		this.tool = tool;
		this.transformSound = transformSound;
		this.kwamiType = kwamiType;
		this.acceptableSlots = acceptableSlots;
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
		if (!level.isClientSide)
		{
			if (entity instanceof Player && !stack.getOrCreateTag().getString(TAG_HOLDER).equals(entity.getName().getString()))
			{
				stack.getOrCreateTag().putString(TAG_HOLDER, entity.getName().getString());
			}
			if (!stack.getOrCreateTag().getBoolean(TAG_POWERED) && !stack.getOrCreateTag().contains(TAG_KWAMIDATA))
			{
				stack.getOrCreateTag().putBoolean(TAG_POWERED, true);
			}
		}
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand)
	{
		if (!player.level().isClientSide && usedHand == InteractionHand.MAIN_HAND && interactionTarget instanceof Kwami kwami)
		{
			if (MineraculousEntityEvents.renounceMiraculous(stack, kwami))
				return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
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
		return Mineraculous.getBewlr();
	}

	public Item getTool()
	{
		return tool == null ? null : tool.get();
	}

	public ArmorSet getArmorSet()
	{
		return armor;
	}

	public EntityType<? extends Kwami> getKwamiType()
	{
		return kwamiType.get();
	}

	public List<String> getAcceptableSlots()
	{
		return acceptableSlots;
	}

	@Override
	public boolean isEnchantable(ItemStack stack)
	{
		return false;
	}

	@Override
	public boolean isFoil(ItemStack stack)
	{
		return false;
	}
}
