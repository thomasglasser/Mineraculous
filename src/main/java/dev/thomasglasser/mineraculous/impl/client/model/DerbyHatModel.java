package dev.thomasglasser.mineraculous.impl.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
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
import net.minecraft.resources.ResourceLocation;

public class DerbyHatModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(MineraculousConstants.modLoc("derby_hat"), "main");
    public static final ResourceLocation TEXTURE = MineraculousConstants.modLoc("textures/entity/player/derby_hat.png");

    private final ModelPart hat;

    public DerbyHatModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.hat = root.getChild("hat");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition hat = partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 13).addBox(-5.0F, -38.0F, -5.0F, 10.0F, 6.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, -32.0F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(40, 0).addBox(-6.0F, -33.0F, -6.0F, 12.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(40, 1).addBox(-6.0F, -33.0F, 6.0F, 12.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 17).addBox(6.0F, -33.0F, -6.0F, 0.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(24, 17).addBox(-6.0F, -33.0F, -6.0F, 0.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 26.0F, 0.0F));

        PartDefinition cube_r1 = hat.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(40, 15).addBox(0.0F, -9.0F, -2.0F, 0.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -33.2F, 0.0F, -0.7854F, 0.1745F, 0.1745F));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int i1, int i2) {
        hat.render(poseStack, vertexConsumer, i, i1, i2);
    }
}
