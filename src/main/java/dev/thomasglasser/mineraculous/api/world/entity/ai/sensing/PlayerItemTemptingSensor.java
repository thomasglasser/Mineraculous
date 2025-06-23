package dev.thomasglasser.mineraculous.api.world.entity.ai.sensing;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.ItemTemptingSensor;
import net.tslat.smartbrainlib.object.TriPredicate;

/**
 * An extension of {@link ItemTemptingSensor} that includes the player in the predicate.
 *
 * @param <E> The entity being tempted
 */
public class PlayerItemTemptingSensor<E extends LivingEntity> extends ItemTemptingSensor<E> {
    protected TriPredicate<E, Player, ItemStack> temptPredicate = (entity, player, stack) -> false;

    public PlayerItemTemptingSensor() {
        setPredicate((player, entity) -> {
            if (player.isSpectator() || !player.isAlive())
                return false;

            return this.temptPredicate.test(entity, player, player.getMainHandItem()) || this.temptPredicate.test(entity, player, player.getOffhandItem());
        });
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return MineraculousSensorTypes.PLAYER_ITEM_TEMPTING.get();
    }

    /**
     * Sets the predicate to tempt the entity with.
     *
     * @param predicate The predicate to tempt the entity with
     * @return The predicated sensor
     */
    public PlayerItemTemptingSensor<E> temptedWith(final TriPredicate<E, Player, ItemStack> predicate) {
        this.temptPredicate = predicate;

        return this;
    }
}
