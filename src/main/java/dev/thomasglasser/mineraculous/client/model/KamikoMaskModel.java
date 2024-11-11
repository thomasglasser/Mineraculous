package dev.thomasglasser.mineraculous.client.model;

import dev.thomasglasser.mineraculous.Mineraculous;
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

public class KamikoMaskModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Mineraculous.modLoc("kamiko_mask"), "main");
    public static final ResourceLocation TEXTURE = Mineraculous.modLoc("textures/entity/player/kamiko_mask.png");

    public KamikoMaskModel(ModelPart root) {
        super(root, RenderType::entityCutout);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition mask = partdefinition.addOrReplaceChild("mask", CubeListBuilder.create().texOffs(0, 0).addBox(-7.5F, -37.0F, -8.0F, 15.0F, 15.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 30, 15);
    }
}
