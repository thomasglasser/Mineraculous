package dev.thomasglasser.mineraculous.network;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.mineraculous.platform.Services;
import dev.thomasglasser.mineraculous.world.entity.kwami.Kwami;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.mineraculous.world.item.curio.CuriosData;
import dev.thomasglasser.mineraculous.world.level.storage.ArmorData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousData;
import dev.thomasglasser.tommylib.api.network.CustomPacket;
import dev.thomasglasser.tommylib.api.network.PacketUtils;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.registration.RegistryObject;
import dev.thomasglasser.tommylib.api.world.item.armor.ArmorSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ServerboundMiraculousTransformPacket implements CustomPacket
{
	public static final ResourceLocation ID = Mineraculous.modLoc("serverbound_miraculous_transform");

	private final ItemStack miraculous;
	private final CuriosData curiosData;
	private final boolean transform;

	public ServerboundMiraculousTransformPacket(ItemStack miraculous, CuriosData curiosData, boolean transform)
	{
		this.miraculous = miraculous;
		this.curiosData = curiosData;
		this.transform = transform;
	}

	public ServerboundMiraculousTransformPacket(FriendlyByteBuf buf)
	{
		miraculous = buf.readItem();
		curiosData = buf.readWithCodecTrusted(NbtOps.INSTANCE, CuriosData.CODEC);
		transform = buf.readBoolean();
	}

	// ON SERVER
	@Override
	public void handle(@Nullable Player player)
	{
		if (player != null)
		{
			ServerLevel serverLevel = (ServerLevel) player.level();
			MiraculousData miraculousData = Services.DATA.getMiraculousData(player);
			if (transform)
			{
				// Transform
				CompoundTag data = miraculous.getOrCreateTag().getCompound(MiraculousItem.TAG_KWAMIDATA);
				Entity entity = serverLevel.getEntity(data.getUUID("UUID"));
				if (entity instanceof Kwami kwami)
				{
					if (kwami.isCharged() && miraculous.getItem() instanceof MiraculousItem miraculousItem)
					{
						// TODO: Sound
//						level.playSound(null, player.getX(), player.getY(), player.getZ(), transformSound, SoundSource.PLAYERS, 1, 1);

						ArmorData armor = new ArmorData(player.getItemBySlot(EquipmentSlot.HEAD), player.getItemBySlot(EquipmentSlot.CHEST), player.getItemBySlot(EquipmentSlot.LEGS), player.getItemBySlot(EquipmentSlot.FEET));
						Services.DATA.setStoredArmor(armor, player);
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
									stack.getOrCreateTag().putBoolean("HideFlags", true);
									player.setItemSlot(slot, stack);
								}
							}
						}

						miraculous.enchant(Enchantments.BINDING_CURSE, 1);
						miraculous.getOrCreateTag().putBoolean("HideFlags", true);

						ItemStack tool = miraculousItem.getTool() == null ? ItemStack.EMPTY : miraculousItem.getTool().getDefaultInstance();
						player.addItem(tool);
						Services.DATA.setMiraculousData(new MiraculousData(true, tool), player);
						TommyLibServices.NETWORK.sendToAllClients(ClientboundMiraculousTransformPacket.class, ClientboundMiraculousTransformPacket.write(miraculous, curiosData, transform, Services.DATA.getMiraculousData(player).tool()), serverLevel.getServer());
						miraculous.getOrCreateTag().putBoolean(MiraculousItem.TAG_POWERED, true);
						Services.CURIOS.setStackInSlot(player, curiosData, miraculous);
						kwami.discard();
						// TODO: Advancement trigger with miraculous context
					}
					else
					{
						// TODO: Hungry sound
//						level.playSound(null, player.getX(), player.getY(), player.getZ(), kwami.getHungrySound(), kwami.getSoundSource(), 1, 1);
					}
				}
				else
				{
					miraculous.getOrCreateTag().remove(MiraculousItem.TAG_KWAMIDATA);
					Services.CURIOS.setStackInSlot(player, curiosData, miraculous);
				}
			}
			else
			{
				// De-transform
				Kwami kwami = ((MiraculousItem)miraculous.getItem()).summonKwami(player.level(), miraculous, curiosData, player);
				if (kwami != null)
				{
					kwami.setCharged(false);
				}
				else
				{
					Mineraculous.LOGGER.error("Kwami could not be created for player " + player.getName().getString());
					return;
				}
				ArmorData armor = Services.DATA.getStoredArmor(player);
				for (EquipmentSlot slot : Arrays.stream(EquipmentSlot.values()).filter(slot -> slot.getType() == EquipmentSlot.Type.ARMOR).toArray(EquipmentSlot[]::new))
				{
					player.setItemSlot(slot, armor.forSlot(slot));
				}
				miraculous.removeTagKey("Enchantments");
				miraculous.getOrCreateTag().putBoolean(MiraculousItem.TAG_POWERED, false);
				Services.CURIOS.setStackInSlot(player, curiosData, miraculous);
				miraculousData.tool().getOrCreateTag().putBoolean(MiraculousItem.TAG_RECALLED, true);
				Services.DATA.setMiraculousData(new MiraculousData(false, ItemStack.EMPTY), player);
				TommyLibServices.NETWORK.sendToAllClients(ClientboundMiraculousTransformPacket.class, ClientboundMiraculousTransformPacket.write(miraculous, curiosData, transform, Services.DATA.getMiraculousData(player).tool()), serverLevel.getServer());
			}
		}
	}

	@Override
	public Direction direction()
	{
		return Direction.CLIENT_TO_SERVER;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeItem(miraculous);
		buffer.writeWithCodec(NbtOps.INSTANCE, CuriosData.CODEC, curiosData);
		buffer.writeBoolean(transform);
	}

	public static FriendlyByteBuf write(ItemStack miraculous, CuriosData curiosData, boolean transform)
	{
		FriendlyByteBuf buf = PacketUtils.create();
		buf.writeItem(miraculous);
		buf.writeWithCodec(NbtOps.INSTANCE, CuriosData.CODEC, curiosData);
		buf.writeBoolean(transform);
		return buf;
	}

	@Override
	public @NotNull ResourceLocation id()
	{
		return ID;
	}
}
