package dev.thomasglasser.mineraculous.api.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.thomasglasser.mineraculous.api.core.component.MineraculousDataComponents;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;

/**
 * Specifies settings to use when toggling an item's {@link MineraculousDataComponents#ACTIVE} state
 *
 * @param controller The animation controller to use for the item
 * @param onAnim     The animation to play when the active state is toggled on
 * @param offAnim    The animation to play when the active state is toggled off
 * @param onSound    The sound to play when the active state is toggled on
 * @param offSound   The sound to play when the active state is toggled off
 */
public record ActiveSettings(Optional<String> controller, Optional<String> onAnim, Optional<String> offAnim, Optional<Holder<SoundEvent>> onSound, Optional<Holder<SoundEvent>> offSound) {
    public static final Codec<ActiveSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("controller").forGetter(ActiveSettings::controller),
            Codec.STRING.optionalFieldOf("on_anim").forGetter(ActiveSettings::onAnim),
            Codec.STRING.optionalFieldOf("off_anim").forGetter(ActiveSettings::offAnim),
            SoundEvent.CODEC.optionalFieldOf("on_sound").forGetter(ActiveSettings::onSound),
            SoundEvent.CODEC.optionalFieldOf("off_sound").forGetter(ActiveSettings::offSound)).apply(instance, ActiveSettings::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActiveSettings> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ActiveSettings::controller,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ActiveSettings::onAnim,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), ActiveSettings::offAnim,
            ByteBufCodecs.optional(SoundEvent.STREAM_CODEC), ActiveSettings::onSound,
            ByteBufCodecs.optional(SoundEvent.STREAM_CODEC), ActiveSettings::offSound,
            ActiveSettings::new);
}
