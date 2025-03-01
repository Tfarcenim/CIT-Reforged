package schm.shsupercm.citresewn.mixin.types.armor;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schm.shsupercm.citresewn.cit.CIT;
import schm.shsupercm.citresewn.cit.CITContext;
import schm.shsupercm.citresewn.defaults.cit.types.TypeArmor;

import java.util.Map;


@Mixin(HumanoidArmorLayer.class)
public class ArmorFeatureRendererMixinNeoforge<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    @Unique
    private Map<String, ResourceLocation> citresewn$cachedTextures = null;

    @Inject(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V", at = @At("HEAD"))
    public void citresewn$renderArmor(PoseStack poseStack, MultiBufferSource bufferSource, T livingEntity, EquipmentSlot slot, int packedLight, A p_model, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        citresewn$cachedTextures = null;
        if (!TypeArmor.CONTAINER.active())
            return;

        ItemStack equippedStack = TypeArmor.CONTAINER.getVisualItemInSlot(livingEntity, slot);

        CIT<TypeArmor> cit = TypeArmor.CONTAINER.getCIT(new CITContext(equippedStack, livingEntity.level(), livingEntity));
        if (cit != null)
            citresewn$cachedTextures = cit.type.textures;
    }

    @WrapOperation(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;getArmorTexture(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ArmorMaterial$Layer;ZLnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/resources/ResourceLocation;"))
    public ResourceLocation citresewn$replaceArmorTexture(Entity entity, ItemStack armor, ArmorMaterial.Layer layer, boolean innerModel, EquipmentSlot slot, Operation<ResourceLocation> original) {
        if (citresewn$cachedTextures != null) {
            String layerPath = layer.texture(innerModel).getPath();
            ResourceLocation identifier = citresewn$cachedTextures.get(layerPath.substring("textures/models/armor/".length(), layerPath.length() - ".png".length()));
            if (identifier != null)
                return identifier;
        }
        return original.call(entity,armor, layer, innerModel,slot);
    }
    /*?}*/
}
