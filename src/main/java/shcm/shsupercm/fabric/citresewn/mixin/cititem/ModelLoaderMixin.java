package shcm.shsupercm.fabric.citresewn.mixin.cititem;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shcm.shsupercm.CITHooks;
import shcm.shsupercm.fabric.citresewn.pack.ResewnItemModelIdentifier;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

@Mixin(ModelBakery.class)
public class ModelLoaderMixin {
    @Shadow @Final private Set<ResourceLocation> loadingStack;
    @Shadow @Final private Map<ResourceLocation, UnbakedModel> topLevelModels;
    @Shadow @Final private Map<ResourceLocation, UnbakedModel> unbakedCache;

    @Inject(method = "loadTopLevel", at = @At("TAIL"))//todo: is this correct
    public void addCITItemModels(ModelResourceLocation eventModelId, CallbackInfo ci) {
        CITHooks.addCITItemModels((ModelBakery) (Object) this,eventModelId,unbakedCache,loadingStack,topLevelModels);
    }

    @Inject(method = "bakeModels", at = @At("RETURN"))
    public void linkBakedCITItemModels(BiFunction<ResourceLocation, Material, TextureAtlasSprite> map, CallbackInfo ci) {
        CITHooks.linkBakedCITItemModels((ModelBakery) (Object)this);
    }


    @Inject(method = "loadBlockModel", cancellable = true, at = @At("HEAD"))
    public void forceLiteralResewnModelIdentifier(ResourceLocation id, CallbackInfoReturnable<BlockModel> cir) {
        if (id instanceof ResewnItemModelIdentifier) {
            cir.setReturnValue(CITHooks.forceLiteralResewnModelIdentifier((ResewnItemModelIdentifier) id));
        }
    }

    /*@ModifyArg(method = "loadModelFromJson", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourceManager;getResource(Lnet/minecraft/util/Identifier;)Lnet/minecraft/resource/Resource;"))
    public Identifier fixDuplicatePrefixSuffix(Identifier original) {
        if (original.getPath().startsWith("models/models/") && original.getPath().endsWith(".json.json") && original.getPath().contains("cit"))
            return new Identifier(original.getNamespace(), original.getPath().substring(7, original.getPath().length() - 5));

        return original;
    }*/
}
