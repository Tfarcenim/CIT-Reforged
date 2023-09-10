package shcm.shsupercm.fabric.citresewn.mixin.citenchantment;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;
import shcm.shsupercm.fabric.citresewn.pack.cits.CITEnchantment;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(method = "getModel", at = @At("TAIL"))
    private void setAppliedContext(ItemStack stack, Level world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        if (CITResewnConfig.INSTANCE().enabled && CITResewn.INSTANCE.activeCITs != null)
            CITResewn.INSTANCE.activeCITs.setEnchantmentAppliedContextCached(stack, world, entity);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void startApplyingItem(ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (CITResewnConfig.INSTANCE().enabled)
            CITEnchantment.shouldApply = true;
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void stopApplyingItem(ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        CITEnchantment.shouldApply = false;
        if (CITResewn.INSTANCE.activeCITs != null)
            CITResewn.INSTANCE.activeCITs.setEnchantmentAppliedContextCached(null, null, null);
    }

    @Inject(method = "getArmorFoilBuffer", cancellable = true, at = @At("RETURN"))
    private static void getArmorGlintConsumer(MultiBufferSource provider, RenderType layer, boolean solid, boolean glint, CallbackInfoReturnable<VertexConsumer> cir) {
        if (!CITEnchantment.shouldApply)
            return;
        VertexConsumer vertexConsumer = solid ? CITEnchantment.Dummy.GlintRenderLayer.ARMOR_GLINT.tryApply(cir.getReturnValue(), layer, provider) :
                CITEnchantment.Dummy.GlintRenderLayer.ARMOR_ENTITY_GLINT.tryApply(cir.getReturnValue(), layer, provider);
        if (vertexConsumer != null)
            cir.setReturnValue(vertexConsumer);
    }

    @Inject(method = "getCompassFoilBuffer", cancellable = true, at = @At("RETURN"))
    private static void getCompassGlintConsumer(MultiBufferSource provider, RenderType layer, PoseStack.Pose entry, CallbackInfoReturnable<VertexConsumer> cir) {
        if (!CITEnchantment.shouldApply)
            return;
        VertexConsumer vertexConsumer = CITEnchantment.Dummy.GlintRenderLayer.GLINT.tryApply(null, layer, provider);
        if (vertexConsumer != null)
            cir.setReturnValue(VertexMultiConsumer.create(new SheetedDecalTextureGenerator(vertexConsumer, entry.pose(), entry.normal(),1), cir.getReturnValue()));
    }

    @Inject(method = "getCompassFoilBufferDirect", cancellable = true, at = @At("RETURN"))
    private static void getDirectCompassGlintConsumer(MultiBufferSource provider, RenderType layer, PoseStack.Pose entry, CallbackInfoReturnable<VertexConsumer> cir) {
        if (!CITEnchantment.shouldApply)
            return;
        VertexConsumer vertexConsumer = CITEnchantment.Dummy.GlintRenderLayer.DIRECT_GLINT.tryApply(null, layer, provider);
        if (vertexConsumer != null)
            cir.setReturnValue(VertexMultiConsumer.create(new SheetedDecalTextureGenerator(vertexConsumer, entry.pose(), entry.normal(),1), cir.getReturnValue()));
    }

    @Inject(method = "getFoilBuffer", cancellable = true, at = @At("RETURN"))
    private static void getItemGlintConsumer(MultiBufferSource provider, RenderType layer, boolean solid, boolean glint, CallbackInfoReturnable<VertexConsumer> cir) {
        if (!CITEnchantment.shouldApply)
            return;
        VertexConsumer vertexConsumer = Minecraft.useShaderTransparency() && layer == Sheets.translucentItemSheet() ? CITEnchantment.Dummy.GlintRenderLayer.GLINT_TRANSLUCENT.tryApply(cir.getReturnValue(), layer, provider) : (solid ? CITEnchantment.Dummy.GlintRenderLayer.GLINT.tryApply(cir.getReturnValue(), layer, provider) : CITEnchantment.Dummy.GlintRenderLayer.ENTITY_GLINT.tryApply(cir.getReturnValue(), layer, provider));
        if (vertexConsumer != null)
            cir.setReturnValue(vertexConsumer);
    }

    @Inject(method = "getFoilBufferDirect", cancellable = true, at = @At("RETURN"))
    private static void getDirectItemGlintConsumer(MultiBufferSource provider, RenderType layer, boolean solid, boolean glint, CallbackInfoReturnable<VertexConsumer> cir) {
        if (!CITEnchantment.shouldApply)
            return;
        VertexConsumer vertexConsumer = solid ? CITEnchantment.Dummy.GlintRenderLayer.DIRECT_GLINT.tryApply(cir.getReturnValue(), layer, provider) : CITEnchantment.Dummy.GlintRenderLayer.DIRECT_ENTITY_GLINT.tryApply(cir.getReturnValue(), layer, provider);
        if (vertexConsumer != null)
            cir.setReturnValue(vertexConsumer);
    }
}