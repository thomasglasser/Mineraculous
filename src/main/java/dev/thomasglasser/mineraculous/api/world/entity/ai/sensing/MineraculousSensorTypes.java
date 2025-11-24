package dev.thomasglasser.mineraculous.api.world.entity.ai.sensing;

import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.sensing.SensorType;
import org.jetbrains.annotations.ApiStatus;

public class MineraculousSensorTypes {
    private static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(BuiltInRegistries.SENSOR_TYPE, MineraculousConstants.MOD_ID);

    public static final DeferredHolder<SensorType<?>, SensorType<PlayerItemTemptingSensor<?>>> PLAYER_ITEM_TEMPTING = SENSOR_TYPES.register("player_item_tempting", () -> new SensorType<>(PlayerItemTemptingSensor::new));

    @ApiStatus.Internal
    public static void init() {}
}
