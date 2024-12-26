package dev.thomasglasser.mineraculous.mixin.minecraft.server.level;

import dev.thomasglasser.mineraculous.world.ToolIdData;
import dev.thomasglasser.mineraculous.world.ToolIdDataHolder;
import java.util.List;
import java.util.concurrent.Executor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
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
public abstract class ServerLevelMixin implements ToolIdDataHolder {
    @Unique
    private final ServerLevel mineraculous$INSTANCE = ((ServerLevel) (Object) (this));

    @Unique
    protected ToolIdData mineraculous$toolIdData;

    @Shadow
    public abstract DimensionDataStorage getDataStorage();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void minejago_init(MinecraftServer minecraftServer, Executor executor, LevelStorageSource.LevelStorageAccess levelStorageAccess, ServerLevelData serverLevelData, ResourceKey<Level> resourceKey, LevelStem levelStem, ChunkProgressListener chunkProgressListener, boolean bl, long l, List list, boolean bl2, RandomSequences randomSequences, CallbackInfo ci) {
        if (resourceKey == Level.OVERWORLD)
            mineraculous$toolIdData = this.getDataStorage().computeIfAbsent(ToolIdData.factory(mineraculous$INSTANCE), ToolIdData.FILE_ID);
    }

    @Override
    public ToolIdData mineraculous$getToolIdData() {
        return mineraculous$toolIdData;
    }
}
