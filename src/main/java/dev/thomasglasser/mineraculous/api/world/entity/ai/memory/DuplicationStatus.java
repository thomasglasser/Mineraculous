package dev.thomasglasser.mineraculous.api.world.entity.ai.memory;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum DuplicationStatus implements StringRepresentable {
    SHOULD_DUPLICATE,
    IS_DUPLICATING,
    HAS_DUPLICATED;

    public static final Codec<DuplicationStatus> CODEC = StringRepresentable.fromEnum(DuplicationStatus::values);

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
