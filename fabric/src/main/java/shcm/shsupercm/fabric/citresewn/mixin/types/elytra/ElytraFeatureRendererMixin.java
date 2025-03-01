package shcm.shsupercm.fabric.citresewn.mixin.types.elytra;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schm.shsupercm.citresewn.cit.CIT;
import schm.shsupercm.citresewn.cit.CITContext;
import shcm.shsupercm.fabric.citresewn.defaults.cit.types.TypeElytra;

import static shcm.shsupercm.fabric.citresewn.defaults.cit.types.TypeElytra.CONTAINER;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(ElytraLayer.class)
public class ElytraFeatureRendererMixin {
    @Shadow @Mutable @Final private static ResourceLocation WINGS_LOCATION;

    @Unique
    private final static ResourceLocation citresewn$ORIGINAL_SKIN = WINGS_LOCATION;

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"))
    public void citresewn$render(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (!CONTAINER.active())
            return;

        ItemStack equippedStack = CONTAINER.getVisualElytraItem(livingEntity);
        if (!equippedStack.is(Items.ELYTRA))
            return;

        CIT<TypeElytra> cit = CONTAINER.getCIT(new CITContext(equippedStack, livingEntity.level(), livingEntity));
        WINGS_LOCATION = cit == null ? citresewn$ORIGINAL_SKIN : cit.type.texture;
    }

    /**
     * Fix cape elytra skin replacing cit elytra.
     */
    @ModifyVariable(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "STORE"))
    public ResourceLocation citresewn$overrideCapeElytra(ResourceLocation used) {
        return WINGS_LOCATION != citresewn$ORIGINAL_SKIN && WINGS_LOCATION != used ? WINGS_LOCATION : used;
    }
}