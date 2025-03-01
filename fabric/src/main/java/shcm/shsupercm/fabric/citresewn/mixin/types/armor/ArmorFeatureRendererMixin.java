package shcm.shsupercm.fabric.citresewn.mixin.types.armor;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schm.shsupercm.citresewn.cit.CIT;
import schm.shsupercm.citresewn.cit.CITContext;
import shcm.shsupercm.fabric.citresewn.defaults.cit.types.TypeArmor;

import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

import static shcm.shsupercm.fabric.citresewn.defaults.cit.types.TypeArmor.CONTAINER;

@Mixin(HumanoidArmorLayer.class)
public class ArmorFeatureRendererMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    @Unique
    private Map<String, ResourceLocation> citresewn$cachedTextures = null;

    @Inject(method = "renderArmorPiece", at = @At("HEAD"))
    public void citresewn$renderArmor(PoseStack matrices, MultiBufferSource vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        citresewn$cachedTextures = null;
        if (!CONTAINER.active())
            return;

        ItemStack equippedStack = CONTAINER.getVisualItemInSlot(entity, armorSlot);

        CIT<TypeArmor> cit = CONTAINER.getCIT(new CITContext(equippedStack, entity.level(), entity));
        if (cit != null)
            citresewn$cachedTextures = cit.type.textures;
    }

    /*? <1.21 {*/
    /*@Inject(method = "getArmorTexture", cancellable = true, at = @At("HEAD"))
    private void citresewn$replaceArmorTexture(ArmorItem item, boolean legs, String overlay, CallbackInfoReturnable<Identifier> cir) {
        if (citresewn$cachedTextures == null)
            return;

        Identifier identifier = citresewn$cachedTextures.get(item.getMaterial().getName() + "_layer_" + (legs ? "2" : "1") + (overlay == null ? "" : "_" + overlay));
        if (identifier != null)
            cir.setReturnValue(identifier);
    }
    *//*?} else {*/
    @WrapOperation(method = "renderArmorPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ArmorMaterial$Layer;texture(Z)Lnet/minecraft/resources/ResourceLocation;"))
    public ResourceLocation citresewn$replaceArmorTexture(ArmorMaterial.Layer layer, boolean secondLayer, Operation<ResourceLocation> original) {
        if (citresewn$cachedTextures != null) {
            String layerPath = layer.texture(secondLayer).getPath();
            ResourceLocation identifier = citresewn$cachedTextures.get(layerPath.substring("textures/models/armor/".length(), layerPath.length() - ".png".length()));
            if (identifier != null)
                return identifier;
        }
        return original.call(layer, secondLayer);
    }
    /*?}*/
}
