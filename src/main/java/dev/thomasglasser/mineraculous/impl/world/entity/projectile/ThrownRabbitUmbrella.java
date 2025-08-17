package dev.thomasglasser.mineraculous.impl.world.entity.projectile;

import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.entity.projectile.ItemBreakingQuicklyReturningThrownSword;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import dev.thomasglasser.mineraculous.impl.world.item.RabbitUmbrellaItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ThrownRabbitUmbrella extends ItemBreakingQuicklyReturningThrownSword implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> DATA_SHOW_BLADE = SynchedEntityData.defineId(ThrownRabbitUmbrella.class, EntityDataSerializers.BOOLEAN);

    public ThrownRabbitUmbrella(EntityType<? extends ThrownRabbitUmbrella> entity, Level level) {
        super(entity, level);
    }

    public ThrownRabbitUmbrella(double x, double y, double z, Level level, ItemStack pickupItemStack) {
        super(MineraculousEntityTypes.THROWN_RABBIT_UMBRELLA.get(), x, y, z, level, pickupItemStack, 8, null, null);
        if (pickupItemStack.get(MineraculousDataComponents.RABBIT_UMBRELLA_ABILITY) == RabbitUmbrellaItem.Ability.BLADE) {
            this.entityData.set(DATA_SHOW_BLADE, true);
            setBaseDamage(15);
        }
    }

    public ThrownRabbitUmbrella(Level level, LivingEntity shooter, ItemStack pickupItemStack) {
        this(shooter.getX(), shooter.getEyeY() - 0.2, shooter.getZ(), level, pickupItemStack);
        setOwner(shooter);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SHOW_BLADE, false);
    }

    public void setShowBlade(boolean showBlade) {
        this.entityData.set(DATA_SHOW_BLADE, showBlade);
    }

    public boolean shouldShowBlade() {
        return this.entityData.get(DATA_SHOW_BLADE);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return MineraculousItems.RABBIT_UMBRELLA.toStack();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setShowBlade(compound.getBoolean("ShowBlade"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("ShowBlade", shouldShowBlade());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", state -> {
            if (shouldShowBlade())
                return state.setAndContinue(RabbitUmbrellaItem.BLADE_IDLE);
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
