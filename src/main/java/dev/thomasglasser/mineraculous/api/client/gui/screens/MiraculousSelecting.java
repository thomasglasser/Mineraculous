package dev.thomasglasser.mineraculous.api.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

// TODO remove this class once D & R is done
public abstract class MiraculousSelecting extends Screen {
    protected MiraculousSelecting() {
        super(Component.empty());
    }

    public abstract void render3dElements(
            RenderLevelStageEvent.Stage stage,
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            float partialTick);
}
