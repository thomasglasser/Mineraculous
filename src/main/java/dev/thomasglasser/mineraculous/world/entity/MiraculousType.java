package dev.thomasglasser.mineraculous.world.entity;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum MiraculousType implements StringRepresentable {
    CAT;

    public static final Codec<MiraculousType> CODEC = StringRepresentable.fromEnum(MiraculousType::values);

    public String getTranslationKey() {
        return "miraculous." + getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
