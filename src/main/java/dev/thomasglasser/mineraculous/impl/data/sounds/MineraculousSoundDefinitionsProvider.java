package dev.thomasglasser.mineraculous.impl.data.sounds;

import dev.thomasglasser.mineraculous.impl.Mineraculous;
import dev.thomasglasser.mineraculous.api.sounds.MineraculousSoundEvents;
import dev.thomasglasser.tommylib.api.data.sounds.ExtendedSoundDefinitionsProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class MineraculousSoundDefinitionsProvider extends ExtendedSoundDefinitionsProvider {
    public MineraculousSoundDefinitionsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, Mineraculous.MOD_ID, helper);
    }

    @Override
    public void registerSounds() {
        // Abilities
        add(MineraculousSoundEvents.CATACLYSM_ACTIVATE, 2);
        add(MineraculousSoundEvents.CATACLYSM_USE);
        add(MineraculousSoundEvents.KAMIKOTIZATION_ACTIVATE);
        add(MineraculousSoundEvents.KAMIKOTIZED_COMMUNICATION_ACTIVATE);
        add(MineraculousSoundEvents.LUCKY_CHARM_ACTIVATE);
        add(MineraculousSoundEvents.MIRACULOUS_LADYBUG_ACTIVATE);

        // Miraculous Tools
        add(MineraculousSoundEvents.GENERIC_SHIELD);
        add(MineraculousSoundEvents.LADYBUG_YOYO_SHIELD);
        add(MineraculousSoundEvents.CAT_STAFF_EXTEND);
        add(MineraculousSoundEvents.CAT_STAFF_RETRACT);

        // Miraculous
        add(MineraculousSoundEvents.GENERIC_TRANSFORM);
        add(MineraculousSoundEvents.GENERIC_DETRANSFORM);
        add(MineraculousSoundEvents.GENERIC_TIMER_WARNING);
        add(MineraculousSoundEvents.GENERIC_TIMER_END);
        add(MineraculousSoundEvents.LADYBUG_TRANSFORM);
        add(MineraculousSoundEvents.CAT_TRANSFORM);
        add(MineraculousSoundEvents.BUTTERFLY_TRANSFORM);

        // Kamikotization
        add(MineraculousSoundEvents.KAMIKOTIZATION_TRANSFORM);
        add(MineraculousSoundEvents.KAMIKOTIZATION_DETRANSFORM);

        // Kwamis
        add(MineraculousSoundEvents.KWAMI_HURT);
        add(MineraculousSoundEvents.KWAMI_SUMMON);
    }
}
