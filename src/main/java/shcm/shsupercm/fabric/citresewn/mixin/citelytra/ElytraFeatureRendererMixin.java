package shcm.shsupercm.fabric.citresewn.mixin.citelytra;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;

import java.lang.ref.WeakReference;

@Mixin(ElytraFeatureRenderer.class)
public class ElytraFeatureRendererMixin {
    private WeakReference<ItemStack> elytraItemCached = new WeakReference<>(null);
    private WeakReference<LivingEntity> livingEntityCached = new WeakReference<>(null);

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at =
    @At("HEAD"))
    private void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (!CITResewnConfig.INSTANCE().enabled || CITResewn.INSTANCE.activeCITs == null)
            return;

        ItemStack itemStack = livingEntity.getEquippedStack(EquipmentSlot.CHEST);

        this.elytraItemCached = new WeakReference<>(itemStack);
        this.livingEntityCached = new WeakReference<>(livingEntity);
    }

    @ModifyArg(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/ItemRenderer;getArmorGlintConsumer(Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/RenderLayer;ZZ)Lnet/minecraft/client/render/VertexConsumer;"), index = 1)
    private RenderLayer getArmorCutoutNoCull(RenderLayer original) {
        if (CITResewnConfig.INSTANCE().enabled && CITResewn.INSTANCE.activeCITs != null) {
            ItemStack itemStack = this.elytraItemCached.get();
            LivingEntity livingEntity = this.livingEntityCached.get();
            if (itemStack != null && itemStack.isOf(Items.ELYTRA) && livingEntity != null) {
                Identifier elytraTexture = CITResewn.INSTANCE.activeCITs.getElytraTextureCached(itemStack, livingEntity.world, livingEntity);
                this.elytraItemCached = new WeakReference<>(null);
                this.livingEntityCached = new WeakReference<>(null);
                if (elytraTexture != null)
                    return RenderLayer.getArmorCutoutNoCull(elytraTexture);
            }
        }

        return original;
    }
}
