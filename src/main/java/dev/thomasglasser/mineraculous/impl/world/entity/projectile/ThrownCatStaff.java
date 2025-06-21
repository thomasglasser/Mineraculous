package dev.thomasglasser.mineraculous.impl.world.entity.projectile;

import dev.thomasglasser.mineraculous.api.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.api.world.entity.projectile.ItemBreakingQuicklyReturningThrownSword;
import dev.thomasglasser.mineraculous.api.world.item.MineraculousItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ThrownCatStaff extends ItemBreakingQuicklyReturningThrownSword implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ThrownCatStaff(EntityType<? extends ThrownCatStaff> entity, Level level) {
        super(entity, level);
    }

    public ThrownCatStaff(Level level, double x, double y, double z, ItemStack pickupItemStack) {
        super(MineraculousEntityTypes.THROWN_CAT_STAFF.get(), x, y, z, level, pickupItemStack, 15, null, null);
    }

    public ThrownCatStaff(Level level, LivingEntity shooter, ItemStack pickupItemStack) {
        this(level, shooter.getX(), shooter.getEyeY() - 0.2, shooter.getZ(), pickupItemStack);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return MineraculousItems.CAT_STAFF.toStack();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "spin_controller", 0, state -> {
            if (inGroundTime <= 0) {
                return state.setAndContinue(DefaultAnimations.ATTACK_THROW);
            }
            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
