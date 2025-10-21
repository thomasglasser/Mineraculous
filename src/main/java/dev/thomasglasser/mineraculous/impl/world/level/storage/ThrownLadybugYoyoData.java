package dev.thomasglasser.mineraculous.impl.world.level.storage;

import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.impl.world.entity.projectile.ThrownLadybugYoyo;
import dev.thomasglasser.tommylib.api.network.ClientboundSyncDataAttachmentPayload;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record ThrownLadybugYoyoData(Optional<Integer> id, int safeFallTicks, boolean summonedLuckyCharm) {

    public static final StreamCodec<ByteBuf, ThrownLadybugYoyoData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.INT), ThrownLadybugYoyoData::id,
            ByteBufCodecs.INT, ThrownLadybugYoyoData::safeFallTicks,
            ByteBufCodecs.BOOL, ThrownLadybugYoyoData::summonedLuckyCharm,
            ThrownLadybugYoyoData::new);
    public static final int DEFAULT_SAFE_FALL_TICKS = 60;
    public ThrownLadybugYoyoData() {
        this(Optional.empty(), 0, false);
    }

    public ThrownLadybugYoyoData(int id) {
        this(Optional.of(id), DEFAULT_SAFE_FALL_TICKS, false);
    }

    public @Nullable ThrownLadybugYoyo getThrownYoyo(Level level) {
        return id.isPresent() && level.getEntity(id.get()) instanceof ThrownLadybugYoyo thrownYoyo ? thrownYoyo : null;
    }

    public ThrownLadybugYoyoData clearId() {
        return new ThrownLadybugYoyoData(Optional.empty(), safeFallTicks, summonedLuckyCharm);
    }

    public ThrownLadybugYoyoData startSafeFall() {
        return withSafeFallTicks(DEFAULT_SAFE_FALL_TICKS);
    }

    public ThrownLadybugYoyoData decrementSafeFallTicks() {
        return new ThrownLadybugYoyoData(id, safeFallTicks - 1, summonedLuckyCharm);
    }

    public ThrownLadybugYoyoData withSafeFallTicks(int safeFallTicks) {
        return new ThrownLadybugYoyoData(id, safeFallTicks, summonedLuckyCharm);
    }

    public ThrownLadybugYoyoData setSummonedLuckyCharm(boolean summonedLuckyCharm) {
        return new ThrownLadybugYoyoData(id, safeFallTicks, summonedLuckyCharm);
    }

    public void save(Entity entity, boolean syncToClient) {
        entity.setData(MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO, this);
        if (syncToClient)
            TommyLibServices.NETWORK.sendToAllClients(new ClientboundSyncDataAttachmentPayload<>(entity.getId(), MineraculousAttachmentTypes.THROWN_LADYBUG_YOYO, this), entity.getServer());
    }
}
