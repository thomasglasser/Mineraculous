package dev.thomasglasser.mineraculous.world.entity.kwami;

import dev.thomasglasser.mineraculous.tags.MineraculousItemTags;
import dev.thomasglasser.mineraculous.world.item.MiraculousItem;
import dev.thomasglasser.tommylib.api.world.item.ItemUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.AvoidEntity;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FleeTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FloatToSurfaceOfFluid;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.FollowOwner;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomFlyingTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public abstract class Kwami extends TamableAnimal implements SmartBrainOwner<Kwami>, GeoEntity
{
	public static final String TAG_CHARGED = "Charged";
	public static final EntityDataSerializer<Boolean> CHARGED = EntityDataSerializers.BOOLEAN;
	private static final EntityDataAccessor<Boolean> DATA_CHARGED = SynchedEntityData.defineId(Kwami.class, CHARGED);

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);


	protected Kwami(EntityType<? extends Kwami> entityType, Level level)
	{
		super(entityType, level);
		setPersistenceRequired();
		moveControl = new FlyingMoveControl(this, 10, true);
		setInvulnerable(true);
		setNoGravity(true);
		noPhysics = true;
	}

	public static AttributeSupplier.Builder createKwamiAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.3)
				.add(Attributes.MAX_HEALTH, 1024)
				.add(Attributes.FLYING_SPEED, 0.3);
	}

	@Override
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		entityData.define(DATA_CHARGED, true);
	}

	@Override
	protected @NotNull PathNavigation createNavigation(@NotNull Level world) {
		return new FlyingPathNavigation(this, world);
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent)
	{
		return null;
	}

	@Override
	protected Brain.@NotNull Provider<?> brainProvider() {
		return new SmartBrainProvider<>(this);
	}

	@Override
	protected void customServerAiStep() {
		tickBrain(this);
	}

	@Override
	public List<? extends ExtendedSensor<? extends Kwami>> getSensors()
	{
		return ObjectArrayList.of(
			new NearbyPlayersSensor<>()
		);
	}

	@Override
	public BrainActivityGroup<? extends Kwami> getCoreTasks()
	{
		return BrainActivityGroup.coreTasks(
				new AvoidEntity<>().noCloserThan(5).stopCaringAfter(10).speedModifier(2f).avoiding(livingEntity -> livingEntity instanceof Player && livingEntity != getOwner()),
				new FollowOwner<>().speedMod(10f).stopFollowingWithin(4).teleportToTargetAfter(10),
				new SetWalkTargetToAttackTarget<>(),
				new MoveToWalkTarget<>(),
				new FleeTarget<>().speedModifier(1.5f),
				new LookAtTarget<>(),
				new FloatToSurfaceOfFluid<>(),
				new AvoidEntity<>().avoiding(livingEntity -> livingEntity instanceof Player)
		);
	}

	@Override
	public BrainActivityGroup<? extends Kwami> getIdleTasks()
	{
		return BrainActivityGroup.idleTasks(
				new FirstApplicableBehaviour<>(
						new SetPlayerLookTarget<>(),
						new SetRandomLookTarget<>()
				),
				new OneRandomBehaviour<>(
						new SetRandomFlyingTarget<>(),
						new Idle<>().runFor(entity -> entity.getRandom().nextInt(30, 60))
				)
		);
	}

	public void setCharged(boolean charged)
	{
		entityData.set(DATA_CHARGED, charged);
	}

	public boolean isCharged()
	{
		return entityData.get(DATA_CHARGED);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand)
	{
		if (!player.level().isClientSide && player == getOwner())
		{
			ItemStack stack = player.getItemInHand(hand);
			if (!isCharged())
			{
				if (isTreat(stack) || (isFood(stack) && random.nextInt(3) == 0))
				{
					setCharged(true);
				}
				if (isTreat(stack) || isFood(stack))
				{
					ItemStack remainder = ItemUtils.safeShrink(1, stack, player);
					if (!remainder.isEmpty()) player.addItem(remainder);
					return InteractionResult.SUCCESS;
				}
			}
		}
		else
		{
			BrainUtils.setMemory(this, MemoryModuleType.ATTACK_TARGET, player);
		}
		return InteractionResult.PASS;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
	{

	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache()
	{
		return cache;
	}

	public boolean isFood(ItemStack stack)
	{
		return stack.is(MineraculousItemTags.KWAMI_FOODS);
	}

	public boolean isTreat(ItemStack stack)
	{
		return stack.is(MineraculousItemTags.KWAMI_TREATS);
	}

	public abstract SoundEvent getHungrySound();

	@Override
	public void die(DamageSource damageSource)
	{
		if (getOwner() instanceof ServerPlayer player)
		{
			List<ItemStack> miraculous = player.getInventory().items.stream().filter(stack -> stack.getOrCreateTag().getCompound(MiraculousItem.TAG_KWAMIDATA).hasUUID("UUID") && stack.getOrCreateTag().getCompound(MiraculousItem.TAG_KWAMIDATA).getUUID("UUID").equals(getUUID())).toList();
			for (ItemStack stack : miraculous)
			{
				stack.getOrCreateTag().putBoolean(MiraculousItem.TAG_POWERED, true);
				stack.getOrCreateTag().remove(MiraculousItem.TAG_KWAMIDATA);
			}
		}
		super.die(damageSource);
	}
}
