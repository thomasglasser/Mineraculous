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

public class BeardModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(MineraculousConstants.modLoc("beard"), "main");
    public static final ResourceLocation TEXTURE = MineraculousConstants.modLoc("textures/entity/player/beard.png");

    private final ModelPart beard;

    public BeardModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.beard = root.getChild("beard");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition beard = partdefinition.addOrReplaceChild("beard", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -27.575F, -4.0F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.025F)), PartPose.offset(0.0F, 24.0F, -0.5F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int i1, int i2) {
        beard.render(poseStack, vertexConsumer, i, i1, i2);
    }
}
