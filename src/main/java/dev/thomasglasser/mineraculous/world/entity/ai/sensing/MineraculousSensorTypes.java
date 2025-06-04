package dev.thomasglasser.mineraculous.world.entity.ai.sensing;

import dev.thomasglasser.mineraculous.Mineraculous;
import dev.thomasglasser.tommylib.api.registration.DeferredHolder;
import dev.thomasglasser.tommylib.api.registration.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.sensing.SensorType;

public class MineraculousSensorTypes {
    public static final DeferredRegister<SensorType<?>> SENSOR_TYPES = DeferredRegister.create(BuiltInRegistries.SENSOR_TYPE, Mineraculous.MOD_ID);

    public static final DeferredHolder<SensorType<?>, SensorType<PlayerItemTemptingSensor<?>>> PLAYER_ITEM_TEMPTING = SENSOR_TYPES.register("player_item_tempting", () -> new SensorType<>(PlayerItemTemptingSensor::new));

    public static void init() {}
}
