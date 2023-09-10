package shcm.shsupercm.fabric.citresewn.mixin.citelytra;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.ref.WeakReference;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Mixin(ElytraLayer.class)
public class ElytraFeatureRendererMixin {
    private WeakReference<ItemStack> elytraItemCached = new WeakReference<>(null);
    private WeakReference<LivingEntity> livingEntityCached = new WeakReference<>(null);

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at =
    @At("HEAD"))
    private void render(PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i, LivingEntity livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (!CITResewnConfig.INSTANCE().enabled || CITResewn.INSTANCE.activeCITs == null)
            return;

        ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.CHEST);

        this.elytraItemCached = new WeakReference<>(itemStack);
        this.livingEntityCached = new WeakReference<>(livingEntity);
    }

    @ModifyArg(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;getArmorFoilBuffer(Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/RenderType;ZZ)Lcom/mojang/blaze3d/vertex/VertexConsumer;"), index = 1)
    private RenderType getArmorCutoutNoCull(RenderType original) {
        if (CITResewnConfig.INSTANCE().enabled && CITResewn.INSTANCE.activeCITs != null) {
            ItemStack itemStack = this.elytraItemCached.get();
            LivingEntity livingEntity = this.livingEntityCached.get();
            if (itemStack != null && itemStack.is(Items.ELYTRA) && livingEntity != null) {
                ResourceLocation elytraTexture = CITResewn.INSTANCE.activeCITs.getElytraTextureCached(itemStack, livingEntity.level(), livingEntity);
                this.elytraItemCached = new WeakReference<>(null);
                this.livingEntityCached = new WeakReference<>(null);
                if (elytraTexture != null)
                    return RenderType.armorCutoutNoCull(elytraTexture);
            }
        }

        return original;
    }
}
