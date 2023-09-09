package shcm.shsupercm.fabric.citresewn.mixin.cititem;

import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;
import shcm.shsupercm.fabric.citresewn.pack.cits.CITItem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.ref.WeakReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Shadow @Final private ItemModelShaper itemModelShaper;

    private static WeakReference<BakedModel> mojankCITModel = null;

    @Inject(method = "getModel", cancellable = true, at = @At("HEAD"))
    private void getItemModel(ItemStack stack, Level world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        if (!CITResewnConfig.INSTANCE().enabled || CITResewn.INSTANCE.activeCITs == null)
            return;

        BakedModel citModel = CITResewn.INSTANCE.activeCITs.getItemModelCached(stack, world == null ? Minecraft.getInstance().level : world, entity, seed);
        if (citModel != null)
            cir.setReturnValue(citModel);
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void fixMojankCITsContext(ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        mojankCITModel = null;
        if (((CITItem.Cached) (Object) stack).citresewn_isMojankCIT()) {
            boolean bl = renderMode == ItemDisplayContext.GUI || renderMode == ItemDisplayContext.GROUND || renderMode == ItemDisplayContext.FIXED;
            if (bl)
                mojankCITModel = new WeakReference<>(model);
            else { // rendered in hand model of trident/spyglass
                if (stack.is(Items.TRIDENT))
                    mojankCITModel = new WeakReference<>(this.itemModelShaper.getModelManager().getModel(ModelResourceLocation.vanilla("trident","inventory")));
                else if (stack.is(Items.SPYGLASS))
                    mojankCITModel = new WeakReference<>(this.itemModelShaper.getModelManager().getModel(ModelResourceLocation.vanilla("spyglass","inventory")));
            }
        } else
            mojankCITModel = null;
    }

    @ModifyVariable(method = "render", at = @At(value = "LOAD", ordinal = 0, target = "Lnet/minecraft/client/render/model/BakedModel;getTransformation()Lnet/minecraft/client/render/model/json/ModelTransformation;"), argsOnly = true,remap = false)
    private BakedModel fixMojankCITs(BakedModel original) {
        if (mojankCITModel != null)
            return mojankCITModel.get();

        return original;
    }
}
