package shcm.shsupercm.forge.citresewn.mixin.citenchantment;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shcm.shsupercm.forge.citresewn.CITResewn;
import shcm.shsupercm.forge.citresewn.config.CITResewnConfig;
import shcm.shsupercm.forge.citresewn.pack.cits.CITEnchantment;

@Mixin(ElytraLayer.class)
public class ElytraFeatureRendererMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"))
    private void setAppliedContextAndStartApplyingElytra(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (CITResewnConfig.INSTANCE().enabled && CITResewn.INSTANCE.activeCITs != null) {
            CITResewn.INSTANCE.activeCITs.setEnchantmentAppliedContextCached(livingEntity.getItemBySlot(EquipmentSlot.CHEST), livingEntity.level(), livingEntity);
            CITEnchantment.shouldApply = true;
        }
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("RETURN"))
    private void stopApplyingElytra(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        CITEnchantment.shouldApply = false;
        if (CITResewn.INSTANCE.activeCITs != null)
            CITResewn.INSTANCE.activeCITs.setEnchantmentAppliedContextCached(null, null, null);
    }
}
