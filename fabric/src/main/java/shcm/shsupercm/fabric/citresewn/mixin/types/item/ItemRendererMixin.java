package shcm.shsupercm.fabric.citresewn.mixin.types.item;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import schm.shsupercm.citresewn.cit.CIT;
import schm.shsupercm.citresewn.cit.CITContext;
import shcm.shsupercm.fabric.citresewn.defaults.cit.types.TypeItem;

import java.lang.ref.WeakReference;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Do not go through this class, it looks awful because it was ported from a "proof of concept".<br>
 * The whole type will be rewritten at some point.
 */
@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Shadow @Final private ItemModelShaper itemModelShaper;

    @Unique
    private WeakReference<BakedModel> citresewn$mojankCITModel = null;

    @Inject(method = "getModel", cancellable = true, at = @At("HEAD"))
    private void citresewn$getItemModel(ItemStack stack, Level world, LivingEntity entity, int seed, CallbackInfoReturnable<BakedModel> cir) {
        if (!TypeItem.CONTAINER.active())
            return;

        CITContext context = new CITContext(stack, world, entity);
        CIT<TypeItem> cit = TypeItem.CONTAINER.getCIT(context, seed);
        citresewn$mojankCITModel = null;
        if (cit != null) {
            BakedModel citModel = cit.type.getItemModel(context, seed);

            if (citModel != null) {
                if (stack.is(Items.TRIDENT) || stack.is(Items.SPYGLASS)) {
                    citresewn$mojankCITModel = new WeakReference<>(citModel);
                } else
                    cir.setReturnValue(citModel);
            }
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void citresewn$fixMojankCITsContext(ItemStack stack, ItemDisplayContext renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo ci) {
        if (!TypeItem.CONTAINER.active() || citresewn$mojankCITModel == null)
            return;

        if (renderMode == ItemDisplayContext.GUI || renderMode == ItemDisplayContext.GROUND || renderMode == ItemDisplayContext.FIXED)
            ((TypeItem.BakedModelManagerMixinAccess) this.itemModelShaper.getModelManager()).citresewn$forceMojankModel(citresewn$mojankCITModel.get());

        citresewn$mojankCITModel = null;
    }
}
