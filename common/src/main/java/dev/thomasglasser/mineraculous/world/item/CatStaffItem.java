package dev.thomasglasser.mineraculous.world.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.renderer.item.CatStaffRenderer;
import dev.thomasglasser.mineraculous.network.ServerboundActivateToolAbilityPacket;
import dev.thomasglasser.mineraculous.network.ServerboundActivateToolPacket;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.item.BaseModeledSwordItem;
import dev.thomasglasser.tommylib.api.world.item.FabricGeoItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CatStaffItem extends BaseModeledSwordItem implements FabricGeoItem
{
	public static final RawAnimation EXTEND = RawAnimation.begin().thenPlay("attack.extend");
	public static final RawAnimation RETRACT = RawAnimation.begin().thenPlay("attack.retract");
	public static final RawAnimation IDLE_RETRACTED = RawAnimation.begin().thenPlay("misc.idle.retracted");

	public static final String TAG_ACTIVATED = "Activated";
	public static final String TAG_UUID = "UUID";
	public static final String TAG_TRAVELLING = "Ability";

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	private BlockEntityWithoutLevelRenderer bewlr;

	protected CatStaffItem(int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties)
	{
		super(MineraculousTiers.MIRACULOUS, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{
		controllers.add(new AnimationController<GeoAnimatable>(this, "use_controller", state -> PlayState.CONTINUE)
				.triggerableAnim("extend", EXTEND)
				.triggerableAnim("shield", DefaultAnimations.ATTACK_BLOCK)
				.triggerableAnim("throw", DefaultAnimations.ATTACK_THROW)
				.triggerableAnim("idle", DefaultAnimations.IDLE)
				.triggerableAnim("retract", RETRACT)
				.triggerableAnim("retracted", IDLE_RETRACTED));
	}

	@Override
	public BlockEntityWithoutLevelRenderer getBEWLR()
	{
		if (bewlr == null) bewlr = new CatStaffRenderer();
		return bewlr;
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}

	@Override
	public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
		if (pEntity instanceof Player player && !player.isUsingItem())
		{
			if (pLevel instanceof ServerLevel)
			{
				long animId = GeoItem.getOrAssignId(pStack, (ServerLevel) pLevel);
				if (!pStack.getOrCreateTag().getBoolean(TAG_ACTIVATED) && cache.getManagerForId(animId).getAnimationControllers().get("use_controller").getCurrentRawAnimation() != RETRACT)
				{
					triggerAnim(pEntity, animId, "use_controller", "retracted");
				}
				if (pStack.getOrCreateTag().getBoolean(TAG_TRAVELLING))
				{
					player.setDeltaMovement(player.getLookAngle().scale(3));
					player.hurtMarked = true;
				}
			}
			else if (player.getMainHandItem() == pStack || player.getOffhandItem() == pStack)
			{
				InteractionHand hand = player.getMainHandItem() == pStack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

				CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(pEntity);
				int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAITTICKS);
				if (waitTicks <= 0 && !MineraculousClientUtils.hasScreenOpen())
				{
					if (MineraculousKeyMappings.ACTIVATE_TOOL.isDown())
					{
						boolean activate = !pStack.getOrCreateTag().getBoolean(TAG_ACTIVATED);
						pStack.getOrCreateTag().putBoolean(TAG_ACTIVATED, activate);
						TommyLibServices.NETWORK.sendToServer(ServerboundActivateToolPacket.ID, ServerboundActivateToolPacket::new, ServerboundActivateToolPacket.write(activate, pStack, hand));
						playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
					}
					else if (pStack.getOrCreateTag().getBoolean(TAG_ACTIVATED) && MineraculousKeyMappings.ACTIVATE_TRAVELLING.isDown())
					{
						if (!pStack.getOrCreateTag().getBoolean(TAG_TRAVELLING))
						{
							pStack.getOrCreateTag().putBoolean(TAG_TRAVELLING, true);
							TommyLibServices.NETWORK.sendToServer(ServerboundActivateToolAbilityPacket.ID, ServerboundActivateToolAbilityPacket::new, ServerboundActivateToolAbilityPacket.write(true, hand));
							playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
						}
					}
					else if (pStack.getOrCreateTag().getBoolean(TAG_TRAVELLING))
					{
						pStack.getOrCreateTag().putBoolean(TAG_TRAVELLING, false);
						TommyLibServices.NETWORK.sendToServer(ServerboundActivateToolAbilityPacket.ID, ServerboundActivateToolAbilityPacket::new, ServerboundActivateToolAbilityPacket.write(false, hand));
						playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
					}
				}
				TommyLibServices.ENTITY.setPersistentData(pEntity, playerData, false);
			}
		}

		super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if (level instanceof ServerLevel && player.getItemInHand(hand).getOrCreateTag().getBoolean(TAG_ACTIVATED))
		{
			player.startUsingItem(hand);
			// TODO: Move shield to power wheel, switch using to throwing
			triggerAnim(player, GeoItem.getOrAssignId(player.getItemInHand(hand), (ServerLevel) level), "use_controller", "shield");
			return InteractionResultHolder.consume(player.getItemInHand(hand));
		}
		return InteractionResultHolder.pass(player.getItemInHand(hand));
	}

	@Override
	public UseAnim getUseAnimation(ItemStack pStack)
	{
		if (TommyLibServices.PLATFORM.isClientSide() && MineraculousClientUtils.isFirstPerson())
			return UseAnim.NONE;
		return UseAnim.TOOT_HORN;
	}

	@Override
	public int getUseDuration(ItemStack stack)
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
		if (pLevel instanceof ServerLevel serverLevel)
		{
			triggerAnim(pLivingEntity, GeoItem.getOrAssignId(pStack, serverLevel), "use_controller", "idle");
		}
	}

	// Loader override
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		return slot == EquipmentSlot.MAINHAND && stack.getOrCreateTag().getBoolean(TAG_ACTIVATED) ? super.getDefaultAttributeModifiers(slot) : ImmutableMultimap.of();
	}
}
