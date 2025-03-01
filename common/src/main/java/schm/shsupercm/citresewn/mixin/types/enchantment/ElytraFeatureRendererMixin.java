package schm.shsupercm.citresewn.mixin.types.enchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schm.shsupercm.citresewn.cit.CITContext;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import schm.shsupercm.citresewn.defaults.cit.types.TypeEnchantment;

@Mixin(ElytraLayer.class)
public class ElytraFeatureRendererMixin {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"))
    private void citresewn$enchantment$setAppliedContextAndStartApplyingElytra(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (TypeEnchantment.CONTAINER.active())
            TypeEnchantment.CONTAINER.setContext(new CITContext(livingEntity.getItemBySlot(EquipmentSlot.CHEST), livingEntity.level(), livingEntity)).apply();
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("RETURN"))
    private void citresewn$enchantment$stopApplyingElytra(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (TypeEnchantment.CONTAINER.active())
            TypeEnchantment.CONTAINER.setContext(null);
    }
}
