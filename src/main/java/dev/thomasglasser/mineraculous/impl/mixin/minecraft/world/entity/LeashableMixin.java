package dev.thomasglasser.mineraculous.impl.mixin.minecraft.world.entity;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.world.level.storage.LeashingLadybugYoyoData;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Leashable.class)
public interface LeashableMixin {
    @Definition(id = "f", local = @Local(type = float.class))
    @Expression("(double)f > 10.0")
    @ModifyExpressionValue(method = "tickLeash", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static <E extends Entity & Leashable> boolean overrideSnapping(boolean original, E entity) {
        if (entity.getData(MineraculousAttachmentTypes.YOYO_LEASH_OVERRIDE)) {
            return false;
        }
        return original;
    }

    @Definition(id = "f", local = @Local(type = float.class))
    @Expression("(double)f > 6.0")
    @ModifyExpressionValue(method = "tickLeash", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static <E extends Entity & Leashable> boolean overrideElasticPull(boolean original, E entity, @Local float f) {
        if (entity.getData(MineraculousAttachmentTypes.YOYO_LEASH_OVERRIDE)) {
            Entity leashHolder = entity.getLeashHolder();
            if (leashHolder != null) {
                Optional<LeashingLadybugYoyoData> optionalData = leashHolder.getData(MineraculousAttachmentTypes.LEASHING_LADYBUG_YOYO);
                if (optionalData.isPresent()) {
                    LeashingLadybugYoyoData data = optionalData.get();
                    return f > data.maxRopeLength();
                }
            }
        }
        return original;
    }

    @ModifyVariable(method = "dropLeash(Lnet/minecraft/world/entity/Entity;ZZ)V", at = @At("HEAD"), index = 2, argsOnly = true)
    private static <E extends Entity & Leashable> boolean neverDropYoyoLeash(boolean original, @Local(argsOnly = true) E entity) {
        if (entity.getData(MineraculousAttachmentTypes.YOYO_LEASH_OVERRIDE)) {
            return false;
        }
        return original;
    }
}
