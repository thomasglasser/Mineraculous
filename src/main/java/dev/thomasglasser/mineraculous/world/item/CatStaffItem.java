package dev.thomasglasser.mineraculous.world.item;

import dev.thomasglasser.mineraculous.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.client.MineraculousKeyMappings;
import dev.thomasglasser.mineraculous.client.renderer.item.CatStaffRenderer;
import dev.thomasglasser.mineraculous.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.network.ServerboundActivateToolPayload;
import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityEvents;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import dev.thomasglasser.tommylib.api.world.item.BaseModeledSwordItem;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.component.Unbreakable;
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
		super(MineraculousTiers.MIRACULOUS, pProperties
				.attributes(SwordItem.createAttributes(MineraculousTiers.MIRACULOUS, 3, -2.4F))
				.component(DataComponents.UNBREAKABLE, new Unbreakable(false)));
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
				if (!pStack.has(MineraculousDataComponents.POWERED.get()) && cache.getManagerForId(animId).getAnimationControllers().get("use_controller").getCurrentRawAnimation() != RETRACT)
				{
					triggerAnim(pEntity, animId, "use_controller", "retracted");
				}
			}
			else if (player.getMainHandItem() == pStack || player.getOffhandItem() == pStack)
			{
				InteractionHand hand = player.getMainHandItem() == pStack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

				CompoundTag playerData = TommyLibServices.ENTITY.getPersistentData(pEntity);
				int waitTicks = playerData.getInt(MineraculousEntityEvents.TAG_WAITTICKS);
				if (waitTicks <= 0 && MineraculousClientUtils.hasNoScreenOpen())
				{
					if (MineraculousKeyMappings.ACTIVATE_TOOL.isDown())
					{
						boolean activate = !pStack.has(MineraculousDataComponents.POWERED.get());
						if (activate)
						{
							pStack.set(MineraculousDataComponents.POWERED.get(), Unit.INSTANCE);
						}
						else
						{
							pStack.remove(MineraculousDataComponents.POWERED.get());
						}
						TommyLibServices.NETWORK.sendToServer(new ServerboundActivateToolPayload(activate, pStack, hand));
						playerData.putInt(MineraculousEntityEvents.TAG_WAITTICKS, 10);
					}
				}
				TommyLibServices.ENTITY.setPersistentData(pEntity, playerData, false);
			}
		}

		super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand)
	{
		ItemStack stack = pPlayer.getItemInHand(pHand);
		if (!stack.has(MineraculousDataComponents.POWERED.get()))
			return InteractionResultHolder.fail(stack);
		return super.use(pLevel, pPlayer, pHand);
	}
}
