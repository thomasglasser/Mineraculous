package dev.thomasglasser.miraculous.world.item;

import dev.thomasglasser.miraculous.client.renderer.MiraculousBlockEntityWithoutLevelRenderer;
import dev.thomasglasser.miraculous.platform.Services;
import dev.thomasglasser.miraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.miraculous.world.level.storage.ArmorData;
import dev.thomasglasser.miraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import dev.thomasglasser.tommylib.api.world.item.ModeledItem;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.function.Supplier;

public class MiraculousItem extends Item implements ModeledItem
{
	public static final String TAG_POWERED = "Powered";
	public static final String TAG_HOLDER = "Holder";
	public static final String TAG_KWAMIDATA = "KwamiData";
	public static final String TAG_RECALLED = "Recalled";

	private BlockEntityWithoutLevelRenderer bewlr;
	private final ArmorSet armor;
	private final Supplier<Item> tool;
	private final SoundEvent transformSound;
	private final Supplier<EntityType<? extends Kwami>> kwamiType;

	public MiraculousItem(Properties properties, ArmorSet armor, Supplier<Item> tool, SoundEvent transformSound, Supplier<EntityType<? extends Kwami>> kwamiType)
	{
		super(properties.stacksTo(1).fireResistant().rarity(Rarity.EPIC));
		this.armor = armor;
		this.tool = tool;
		this.transformSound = transformSound;
		this.kwamiType = kwamiType;
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

	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
		ItemStack itemStack = player.getItemInHand(usedHand);
		if (level instanceof ServerLevel serverLevel)
		{
			boolean powered = itemStack.getOrCreateTag().getBoolean(TAG_POWERED);
			CompoundTag data = itemStack.getOrCreateTag().getCompound(TAG_KWAMIDATA);
			if (powered)
			{
				Kwami kwami = getKwamiType().create(level);
				if (kwami != null)
				{
					if (data.contains("UUID"))
						kwami.setUUID(data.getUUID("UUID"));
					if (data.contains(Kwami.TAG_HASVOICE))
					{
						kwami.setHasVoice(data.getBoolean(Kwami.TAG_HASVOICE));
					}
					else
					{
						kwami.setHasVoice(true);
					}
					if (data.contains(Kwami.TAG_CHARGED))
					{
						kwami.setCharged(data.getBoolean(Kwami.TAG_CHARGED));
					}
					else
					{
						kwami.setCharged(true);
					}
					kwami.setPos(player.getX() + level.random.nextInt(3), player.getY() + 2, player.getZ() +  + level.random.nextInt(3));
					kwami.tame(player);
					level.addFreshEntity(kwami);

					data.putUUID("UUID", kwami.getUUID());
					data.putBoolean(Kwami.TAG_HASVOICE, kwami.hasVoice());
					data.putBoolean(Kwami.TAG_CHARGED, kwami.isCharged());
					itemStack.getOrCreateTag().put(TAG_KWAMIDATA, data);

					MiraculousData miraculousData = Services.DATA.getMiraculousData(player);
					if (miraculousData.transformed())
					{
						kwami.setCharged(false);
						ArmorData armor = Services.DATA.getStoredArmor(player);
						for (EquipmentSlot slot : Arrays.stream(EquipmentSlot.values()).filter(slot -> slot.getType() == EquipmentSlot.Type.ARMOR).toArray(EquipmentSlot[]::new))
						{
							player.setItemSlot(slot, armor.forSlot(slot));
						}
						miraculousData.tool().getOrCreateTag().putBoolean(TAG_RECALLED, true);
						Services.DATA.setMiraculousData(new MiraculousData(false, ItemStack.EMPTY), player);
					}

					itemStack.getOrCreateTag().putBoolean(TAG_POWERED, false);
				}
			}
			else
			{
				Entity entity = serverLevel.getEntity(data.getUUID("UUID"));
				if (entity instanceof Kwami kwami)
				{
					if (kwami.isCharged() && itemStack.getItem() instanceof MiraculousItem miraculousItem)
					{
						// TODO: Sound
//						if (!level.isClientSide)
//							level.playSound(null, player.getX(), player.getY(), player.getZ(), transformSound, SoundSource.PLAYERS, 1, 1);

						ArmorData armor = new ArmorData(player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST), player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET));
						Services.DATA.setStoredArmor(armor, player);
						// Suit texture animation needed, particles, player animation?
						ArmorSet set = miraculousItem.getArmorSet();
						if (set != null)
						{
							for (EquipmentSlot slot : EquipmentSlot.values())
							{
								RegistryObject<ArmorItem> armorPiece = set.getForSlot(slot);
								if (!(armorPiece == null))
								{
									ItemStack stack = armorPiece.get().getDefaultInstance();
									stack.enchant(Enchantments.BINDING_CURSE, 1);
									stack.getOrCreateTag().putInt("HideFlags", 1);
									player.setItemSlot(slot, stack);
								}
							}
						}
						ItemStack tool = miraculousItem.getTool() == null ? ItemStack.EMPTY : miraculousItem.getTool().getDefaultInstance();
						player.addItem(tool);
						Services.DATA.setMiraculousData(new MiraculousData(true, tool), player);
						itemStack.getOrCreateTag().putBoolean(TAG_POWERED, true);
						kwami.discard();
						// TODO: Advancement trigger with miraculous context
					}
					else
					{
						if (!level.isClientSide)
							level.playSound(null, player.getX(), player.getY(), player.getZ(), kwami.getHungrySound(), kwami.getSoundSource(), 1, 1);
					}
				}
				else
					itemStack.getOrCreateTag().remove(TAG_KWAMIDATA);
			}
		}
		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand)
	{
		if (!player.level().isClientSide && usedHand == InteractionHand.MAIN_HAND)
		{
			CompoundTag tag = stack.getOrCreateTag();
			CompoundTag data = tag.getCompound(TAG_KWAMIDATA);
			if (data.hasUUID("UUID") && interactionTarget.getUUID().equals(data.getUUID("UUID")))
			{
				if (interactionTarget instanceof Kwami kwami)
				{
					data.putBoolean(Kwami.TAG_HASVOICE, kwami.hasVoice());
					data.putBoolean(Kwami.TAG_CHARGED, kwami.isCharged());
				}
				tag.put(TAG_KWAMIDATA, data);
				tag.putBoolean(TAG_POWERED, true);
				interactionTarget.discard();
				// TODO: Play kwami hiding sound
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.sidedSuccess(player.level().isClientSide);
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
}
