package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.renderer.item.CatStaffRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundActivateToolAbilityPayload;
import dev.thomasglasser.mineraculous.network.ServerboundActivateToolPayload;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.item.BaseModeledSwordItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CatStaffItem extends BaseModeledSwordItem implements GeoItem
{
	public static final RawAnimation EXTEND = RawAnimation.begin().thenPlay("attack.extend");
	public static final RawAnimation RETRACT = RawAnimation.begin().thenPlay("attack.retract");
	public static final RawAnimation IDLE_RETRACTED = RawAnimation.begin().thenPlay("misc.idle.retracted");

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	private BlockEntityWithoutLevelRenderer bewlr;

	protected CatStaffItem(Properties pProperties)
	{
		super(MineraculousTiers.MIRACULOUS, pProperties.attributes(SwordItem.createAttributes(MineraculousTiers.MIRACULOUS, 3, -2.4F)).component(MineraculousDataComponents.TRAVELING.get(), false));
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
				if (!pStack.getOrDefault(MineraculousDataComponents.POWERED.get(), false) && cache.getManagerForId(animId).getAnimationControllers().get("use_controller").getCurrentRawAnimation() != RETRACT)
				{
					triggerAnim(pEntity, animId, "use_controller", "retracted");
				}
				if (pStack.getOrDefault(MineraculousDataComponents.TRAVELING.get(), false))
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
				if (waitTicks <= 0 && !MineraculousClientUtils.hasScreenOpen() && pStack.has(MineraculousDataComponents.POWERED.get()))
				{
					if (MineraculousKeyMappings.ACTIVATE_TOOL.isDown())
					{
						boolean activate = !pStack.getOrDefault(MineraculousDataComponents.POWERED.get(), false);
						pStack.set(MineraculousDataComponents.POWERED.get(), activate);
						TommyLibServices.NETWORK.sendToServer(new ServerboundActivateToolPayload(activate, pStack, hand));
						playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
					}
					else if (pStack.getOrDefault(MineraculousDataComponents.POWERED.get(), false) && MineraculousKeyMappings.ACTIVATE_TRAVELLING.isDown())
					{
						if (!pStack.getOrDefault(MineraculousDataComponents.TRAVELING.get(), false))
						{
							pStack.set(MineraculousDataComponents.TRAVELING.get(), true);
							TommyLibServices.NETWORK.sendToServer(new ServerboundActivateToolAbilityPayload(true, hand));
							playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
						}
					}
					else if (pStack.getOrDefault(MineraculousDataComponents.TRAVELING.get(), false))
					{
						pStack.set(MineraculousDataComponents.TRAVELING.get(), false);
						TommyLibServices.NETWORK.sendToServer(new ServerboundActivateToolAbilityPayload(false, hand));
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
		if (level instanceof ServerLevel && player.getItemInHand(hand).getOrDefault(MineraculousDataComponents.POWERED.get(), false))
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
}
