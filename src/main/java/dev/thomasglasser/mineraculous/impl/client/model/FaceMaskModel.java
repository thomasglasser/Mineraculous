package dev.thomasglasser.mineraculous.impl.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.impl.Mineraculous;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

public class FaceMaskModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Mineraculous.modLoc("face_mask"), "main");

    private final ModelPart mask;

    public FaceMaskModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.mask = root.getChild("mask");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition definition = new MeshDefinition();
        PartDefinition root = definition.getRoot();

        PartDefinition mask = root.addOrReplaceChild("mask", CubeListBuilder.create().texOffs(0, 0).addBox(-7.5F, -37.0F, -8.0F, 15.0F, 15.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(definition, 32, 16);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        mask.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
