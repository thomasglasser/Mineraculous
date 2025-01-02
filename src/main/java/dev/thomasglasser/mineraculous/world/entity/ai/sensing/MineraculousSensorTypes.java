package dev.thomasglasser.mineraculous.world.entity.ai.sensing;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.sensing.SensorType;

public class MineraculousSensorTypes {
    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(BuiltInRegistries.SENSOR_TYPE, Mineraculous.MOD_ID);

    public static final DeferredHolder<SensorType<?>, SensorType<PlayerTemptingSensor<?>>> PLAYER_TEMPTING = SENSOR_TYPES.register("player_tempting", () -> new SensorType<>(PlayerTemptingSensor::new));

    public static void init() {}
}
