package shcm.shsupercm.fabric.citresewn.mixin.citenchantment;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;
import shcm.shsupercm.fabric.citresewn.pack.cits.CITEnchantment;

@Mixin(HumanoidArmorLayer.class)
public class ArmorFeatureRendererMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    @Inject(method = "renderArmor", at = @At("HEAD"))
    private void setAppliedContextAndStartApplyingArmor(PoseStack matrices, MultiBufferSource vertexConsumers, T livingEntity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        if (CITResewnConfig.INSTANCE().enabled && CITResewn.INSTANCE.activeCITs != null) {
            CITResewn.INSTANCE.activeCITs.setEnchantmentAppliedContextCached(livingEntity.getItemBySlot(armorSlot), livingEntity.level, livingEntity);
            CITEnchantment.shouldApply = true;
        }
    }

    @Inject(method = "renderArmor", at = @At("RETURN"))
    private void stopApplyingArmor(PoseStack matrices, MultiBufferSource vertexConsumers, T livingEntity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        CITEnchantment.shouldApply = false;
        if (CITResewn.INSTANCE.activeCITs != null)
            CITResewn.INSTANCE.activeCITs.setEnchantmentAppliedContextCached(null, null, null);
    }
}
