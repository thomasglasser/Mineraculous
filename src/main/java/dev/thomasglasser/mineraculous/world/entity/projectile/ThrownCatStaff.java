package dev.thomasglasser.mineraculous.world.entity.projectile;

import dev.thomasglasser.mineraculous.world.entity.MineraculousEntityTypes;
import dev.thomasglasser.mineraculous.world.item.CatStaffItem;
import dev.thomasglasser.mineraculous.world.item.MineraculousItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ThrownCatStaff extends AbstractArrow implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ThrownCatStaff(LivingEntity owner, Level level, ItemStack pickupItemStack, ItemStack firedBy) {
        super(MineraculousEntityTypes.THROWN_CAT_STAFF.get(), owner, level, pickupItemStack, firedBy);
    }

    public ThrownCatStaff(double x, double y, double z, Level level, ItemStack pickupItemStack, ItemStack firedBy) {
        super(MineraculousEntityTypes.THROWN_CAT_STAFF.get(), x, y, z, level, pickupItemStack, firedBy);
    }

    public ThrownCatStaff(EntityType<? extends ThrownCatStaff> entityType, Level level) {
        super(entityType, level);
        this.pickup = AbstractArrow.Pickup.ALLOWED;
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
            return state.setAndContinue(CatStaffItem.EXTEND);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
