package schm.shsupercm.citresewn.mixin.types.enchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import schm.shsupercm.citresewn.cit.CITContext;
import schm.shsupercm.citresewn.defaults.cit.types.TypeEnchantment;

@Mixin(HumanoidArmorLayer.class)
public class ArmorFeatureRendererMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    @Inject(method = "renderArmorPiece", at = @At("HEAD"))
    private void citresewn$enchantment$setAppliedContextAndStartApplyingArmor(PoseStack matrices, MultiBufferSource vertexConsumers, T livingEntity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        if (TypeEnchantment.CONTAINER.active())
            TypeEnchantment.CONTAINER.setContext(new CITContext(livingEntity.getItemBySlot(armorSlot), livingEntity.level(), livingEntity)).apply();
    }

    @Inject(method = "renderArmorPiece", at = @At("RETURN"))
    private void citresewn$enchantment$stopApplyingArmor(PoseStack matrices, MultiBufferSource vertexConsumers, T livingEntity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        if (TypeEnchantment.CONTAINER.active())
            TypeEnchantment.CONTAINER.setContext(null);
    }
}
