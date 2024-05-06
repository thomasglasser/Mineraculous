package dev.thomasglasser.mineraculous.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.world.entity.Kamiko;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

/**
 * Derived from BatModel
 */
public class KamikoModel extends EntityModel<Kamiko> {
    private final ModelPart frame;

    public KamikoModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.frame = root.getChild("frame");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition frame = root.addOrReplaceChild("frame",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 0.0F),
                PartPose.offset( 0.5F, 0.5F, 0.0F)
        );
        return LayerDefinition.create(mesh,2,2);
    }


    @Override
    public void setupAnim(Kamiko entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        frame.xRot = camera.getXRot()* Mth.DEG_TO_RAD;
        frame.yRot = camera.getYRot()*Mth.DEG_TO_RAD;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        frame.render(poseStack,buffer,packedLight,packedOverlay,red,green,blue,alpha);
    }
}

