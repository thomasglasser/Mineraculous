package dev.thomasglasser.mineraculous.world.entity.ai.sensing;

import java.util.function.BiPredicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.ItemTemptingSensor;
import net.tslat.smartbrainlib.object.TriPredicate;

public class PlayerTemptingSensor<E extends LivingEntity> extends ItemTemptingSensor<E> {
    protected TriPredicate<E, Player, ItemStack> temptPredicate = (entity, player, stack) -> false;

    public PlayerTemptingSensor() {
        setPredicate((player, entity) -> {
            if (player.isSpectator() || !player.isAlive())
                return false;

            return this.temptPredicate.test(entity, player, player.getMainHandItem()) || this.temptPredicate.test(entity, player, player.getOffhandItem());
        });
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return MineraculousSensorTypes.PLAYER_TEMPTING.get();
    }

    /**
     * Set the items to temptable items for the entity.
     *
     * @param predicate An ingredient representing the temptations for the
     *                  entity
     * @return this
     */
    public PlayerTemptingSensor<E> temptedWith(final TriPredicate<E, Player, ItemStack> predicate) {
        this.temptPredicate = predicate;

        return this;
    }

    @Override
    public ItemTemptingSensor<E> temptedWith(final BiPredicate<E, ItemStack> predicate) {
        this.temptPredicate = (entity, player, stack) -> predicate.test(entity, stack);

        return this;
    }
}
