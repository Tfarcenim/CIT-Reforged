package shcm.shsupercm.forge.citresewn.mixin.citarmor;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shcm.shsupercm.CITHooks;
import shcm.shsupercm.forge.citresewn.CITResewn;
import shcm.shsupercm.forge.citresewn.config.CITResewnConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.ref.WeakReference;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

@Mixin(HumanoidArmorLayer.class)
public class ArmorFeatureRendererMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    private WeakReference<Map<String, ResourceLocation>> armorTexturesCached = null;

    @Inject(method = "renderArmorPiece", at = @At("HEAD"))
    private void renderArmor(PoseStack matrices, MultiBufferSource vertexConsumers, T entity, EquipmentSlot armorSlot, int light, A model, CallbackInfo ci) {
        if (!CITResewnConfig.INSTANCE().enabled || CITResewn.INSTANCE.activeCITs == null)
            return;

        ItemStack itemStack = entity.getItemBySlot(armorSlot);

        Map<String, ResourceLocation> armorTextures = CITResewn.INSTANCE.activeCITs.getArmorTexturesCached(itemStack, entity.level(), entity);
        if (armorTextures != null) {
            armorTexturesCached = new WeakReference<>(armorTextures);
            return;
        }

        armorTexturesCached = null;
    }

    @Inject(method = "getArmorResource", cancellable = true, at = @At("HEAD"),remap = false)
    private void getArmorTexture(Entity entity, ItemStack stack, EquipmentSlot slot, String type, CallbackInfoReturnable<ResourceLocation> cir) {
        ResourceLocation identifier = CITHooks.getArmorTextures(stack,slot,type,armorTexturesCached);
        if (identifier != null) cir.setReturnValue(identifier);
    }
}
