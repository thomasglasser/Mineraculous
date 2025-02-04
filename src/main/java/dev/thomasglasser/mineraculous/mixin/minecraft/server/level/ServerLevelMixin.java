package dev.thomasglasser.mineraculous.mixin.minecraft.server.level;

import com.mojang.datafixers.util.Either;
import dev.thomasglasser.mineraculous.world.entity.miraculous.Miraculous;
import dev.thomasglasser.mineraculous.world.level.storage.ChargeOverrideData;
import dev.thomasglasser.mineraculous.world.level.storage.ChargeOverrideDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedKamikotizationLookData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedLookDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedMiraculousLookData;
import dev.thomasglasser.mineraculous.world.level.storage.FlattenedSuitLookData;
import dev.thomasglasser.mineraculous.world.level.storage.LuckyCharmIdData;
import dev.thomasglasser.mineraculous.world.level.storage.LuckyCharmIdDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousRecoveryBlockData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousRecoveryDataHolder;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousRecoveryEntityData;
import dev.thomasglasser.mineraculous.world.level.storage.MiraculousRecoveryItemData;
import dev.thomasglasser.mineraculous.world.level.storage.ToolIdData;
import dev.thomasglasser.mineraculous.world.level.storage.ToolIdDataHolder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin implements ToolIdDataHolder, LuckyCharmIdDataHolder, FlattenedLookDataHolder, ChargeOverrideDataHolder, MiraculousRecoveryDataHolder {
    @Unique
    protected ToolIdData mineraculous$toolIdData;
    @Unique
    protected LuckyCharmIdData mineraculous$luckyCharmIdData;
    @Unique
    protected ChargeOverrideData mineraculous$chargeOverrideData;
    @Unique
    protected MiraculousRecoveryEntityData mineraculous$miraculousRecoveryEntityData;
    @Unique
    protected MiraculousRecoveryItemData mineraculous$miraculousRecoveryItemData;
    @Unique
    protected MiraculousRecoveryBlockData mineraculous$miraculousRecoveryBlockData;
    @Unique
    protected Map<ResourceKey<Miraculous>, Map<String, FlattenedSuitLookData>> mineraculous$commonSuitLookData;
    @Unique
    protected Map<ResourceKey<Miraculous>, Map<String, FlattenedMiraculousLookData>> mineraculous$commonMiraculousLookData;
    @Unique
    protected Map<UUID, Set<FlattenedSuitLookData>> mineraculous$suitLookData;
    @Unique
    protected Map<UUID, Set<FlattenedMiraculousLookData>> mineraculous$miraculousLookData;
    @Unique
    protected Map<UUID, FlattenedKamikotizationLookData> mineraculous$kamikotizationLookData;
    @Unique
    protected List<Either<UUID, String>> mineraculous$whitelist;
    @Unique
    protected List<Either<UUID, String>> mineraculous$blacklist;

    @Shadow
    public abstract DimensionDataStorage getDataStorage();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void minejago_init(MinecraftServer minecraftServer, Executor executor, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey<Level> resourceKey, LevelStem levelStem, ChunkProgressListener chunkProgressListener, boolean bl, long l, List list, boolean bl2, RandomSequences randomSequences, CallbackInfo ci) {
        if (resourceKey == Level.OVERWORLD) {
            mineraculous$toolIdData = this.getDataStorage().computeIfAbsent(ToolIdData.factory(), ToolIdData.FILE_ID);
            mineraculous$luckyCharmIdData = this.getDataStorage().computeIfAbsent(LuckyCharmIdData.factory(), LuckyCharmIdData.FILE_ID);
            mineraculous$chargeOverrideData = this.getDataStorage().computeIfAbsent(ChargeOverrideData.factory(), ChargeOverrideData.FILE_ID);
            mineraculous$miraculousRecoveryEntityData = this.getDataStorage().computeIfAbsent(MiraculousRecoveryEntityData.factory(), MiraculousRecoveryEntityData.FILE_ID);
            mineraculous$miraculousRecoveryItemData = this.getDataStorage().computeIfAbsent(MiraculousRecoveryItemData.factory(), MiraculousRecoveryItemData.FILE_ID);
            mineraculous$miraculousRecoveryBlockData = this.getDataStorage().computeIfAbsent(MiraculousRecoveryBlockData.factory(), MiraculousRecoveryBlockData.FILE_ID);
            mineraculous$commonSuitLookData = new HashMap<>();
            mineraculous$commonMiraculousLookData = new HashMap<>();
            mineraculous$suitLookData = new HashMap<>();
            mineraculous$miraculousLookData = new HashMap<>();
            mineraculous$kamikotizationLookData = new HashMap<>();
        }
    }

    @Override
    public ToolIdData mineraculous$getToolIdData() {
        return mineraculous$toolIdData;
    }

    @Override
    public LuckyCharmIdData mineraculous$getLuckyCharmIdData() {
        return mineraculous$luckyCharmIdData;
    }

    @Override
    public MiraculousRecoveryEntityData mineraculous$getMiraculousRecoveryEntityData() {
        return mineraculous$miraculousRecoveryEntityData;
    }

    @Override
    public MiraculousRecoveryItemData mineraculous$getMiraculousRecoveryItemData() {
        return mineraculous$miraculousRecoveryItemData;
    }

    @Override
    public MiraculousRecoveryBlockData mineraculous$getMiraculousRecoveryBlockData() {
        return mineraculous$miraculousRecoveryBlockData;
    }

    @Override
    public Map<ResourceKey<Miraculous>, Map<String, FlattenedSuitLookData>> mineraculous$getCommonSuitLookData() {
        return mineraculous$commonSuitLookData;
    }

    @Override
    public void mineraculous$setCommonSuitLookData(Map<ResourceKey<Miraculous>, Map<String, FlattenedSuitLookData>> data) {
        mineraculous$commonSuitLookData = data;
    }

    @Override
    public Map<ResourceKey<Miraculous>, Map<String, FlattenedMiraculousLookData>> mineraculous$getCommonMiraculousLookData() {
        return mineraculous$commonMiraculousLookData;
    }

    @Override
    public void mineraculous$setCommonMiraculousLookData(Map<ResourceKey<Miraculous>, Map<String, FlattenedMiraculousLookData>> data) {
        mineraculous$commonMiraculousLookData = data;
    }

    @Override
    public Map<UUID, Set<FlattenedSuitLookData>> mineraculous$getSuitLookData() {
        return mineraculous$suitLookData;
    }

    @Override
    public void mineraculous$addSuitLookData(UUID player, FlattenedSuitLookData data) {
        mineraculous$suitLookData.computeIfAbsent(player, p -> new HashSet<>()).add(data);
    }

    @Override
    public Map<UUID, Set<FlattenedMiraculousLookData>> mineraculous$getMiraculousLookData() {
        return mineraculous$miraculousLookData;
    }

    @Override
    public void mineraculous$addMiraculousLookData(UUID player, FlattenedMiraculousLookData data) {
        mineraculous$miraculousLookData.computeIfAbsent(player, p -> new HashSet<>()).add(data);
    }

    @Override
    public Map<UUID, FlattenedKamikotizationLookData> mineraculous$getKamikotizationLookData() {
        return mineraculous$kamikotizationLookData;
    }

    @Override
    public void mineraculous$addKamikotizationLookData(UUID player, FlattenedKamikotizationLookData data) {
        mineraculous$kamikotizationLookData.put(player, data);
    }

    @Override
    public boolean mineraculous$isPlayerWhitelisted(Player player) {
        return mineraculous$whitelist.stream().anyMatch(either -> either.left().isPresent() && either.left().get().equals(player.getUUID()) || either.right().orElseThrow().equals(player.getGameProfile().getName()));
    }

    @Override
    public void mineraculous$setWhitelist(List<Either<UUID, String>> whitelist) {
        mineraculous$whitelist = whitelist;
    }

    @Override
    public boolean mineraculous$isPlayerBlacklisted(Player player) {
        return mineraculous$blacklist.stream().anyMatch(either -> either.left().isPresent() && either.left().get().equals(player.getUUID()) || either.right().orElseThrow().equals(player.getGameProfile().getName()));
    }

    @Override
    public void mineraculous$setBlacklist(List<Either<UUID, String>> blacklist) {
        mineraculous$blacklist = blacklist;
    }

    @Override
    public ChargeOverrideData mineraculous$getChargeOverrideData() {
        return mineraculous$chargeOverrideData;
    }
}
