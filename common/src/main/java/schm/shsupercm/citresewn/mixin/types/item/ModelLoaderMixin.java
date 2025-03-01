package schm.shsupercm.citresewn.mixin.types.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import schm.shsupercm.citresewn.CITResewn;
import schm.shsupercm.citresewn.cit.CIT;
import schm.shsupercm.citresewn.defaults.cit.types.TypeItem;
import schm.shsupercm.citresewn.defaults.common.ResewnItemModelIdentifier;

import java.util.*;

@Mixin(ModelBakery.class)
public class ModelLoaderMixin {
    @Shadow @Final private Map<ResourceLocation, UnbakedModel> unbakedCache;
    @Shadow @Final private Map</*? >=1.21 {*/ModelResourceLocation/*?} else {*//*Identifier*//*?}*/, UnbakedModel> topLevelModels;
    @Shadow @Final private Map</*? >=1.21 {*/ModelResourceLocation/*?} else {*//*Identifier*//*?}*/, BakedModel> bakedTopLevelModels;

    @Inject(method = "<init>", at =
    @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",ordinal = 0))
    public void citresewn$addTypeItemModels(BlockColors blockColors, ProfilerFiller profiler, Map map, Map map2, CallbackInfo ci) {
        profiler.popPush("citresewn:type_item_models");
        if (!TypeItem.CONTAINER.active())
            return;

        CITResewn.info("Loading item CIT models...");
        for (CIT<TypeItem> cit : TypeItem.CONTAINER.loaded)
            try {
                cit.type.loadUnbakedAssets(Minecraft.getInstance().getResourceManager());

                for (BlockModel unbakedModel : cit.type.unbakedAssets.values()) {
                    ResourceLocation id = ResewnItemModelIdentifier.pack(ResourceLocation.tryParse(unbakedModel.name));
                    this.unbakedCache.put(id, unbakedModel);
                    this.topLevelModels.put(/*? >=1.21 {*/ModelResourceLocation.inventory(id)/*?} else {*//*id*//*?}*/, unbakedModel);
                }
            } catch (Exception e) {
                CITResewn.logErrorLoading("Errored loading model in " + cit.propertiesIdentifier + " from " + cit.packName);
                e.printStackTrace();
            }

        TypeItem.GENERATED_SUB_CITS_SEEN.clear();
    }

    @Inject(method = "bakeModels", at = @At("RETURN"))
    public void citresewn$linkTypeItemModels(/*? <1.21 {*//*BiFunction<Identifier, SpriteIdentifier, Sprite> spriteLoader*//*?} else {*/ModelBakery.TextureGetter spriteGetter/*?}*/, CallbackInfo ci) {
        if (!TypeItem.CONTAINER.active())
            return;

        CITResewn.info("Linking baked models to item CITs...");

        for (CIT<TypeItem> cit : TypeItem.CONTAINER.loaded) {
            for (Map.Entry<List<ItemOverride.Predicate>, BlockModel> citModelEntry : cit.type.unbakedAssets.entrySet()) {
                var modelIdentifier = /*? >=1.21 {*/ModelResourceLocation.inventory(ResewnItemModelIdentifier.pack(ResourceLocation.parse(citModelEntry.getValue().name)))/*?} else {*//*ResewnItemModelIdentifier.pack(Identifier.tryParse(citModelEntry.getValue().id))*//*?}*/;
                if (citModelEntry.getKey() == null) {
                    cit.type.bakedModel = this.bakedTopLevelModels.get(modelIdentifier);
                } else {
                    BakedModel bakedModel = this.bakedTopLevelModels.get(modelIdentifier);
                    if (bakedModel == null)
                        CITResewn.logWarnLoading("Skipping sub cit: Failed loading model for \"" + citModelEntry.getValue().name + "\" in " + cit.propertiesIdentifier + " from " + cit.packName);
                    else
                        cit.type.bakedSubModels.override(citModelEntry.getKey(), bakedModel);
                }
            }
            cit.type.unbakedAssets = null;
        }
    }

    @ModifyArg(method = "loadBlockModel", at =
    @At(value = "INVOKE", ordinal = 1, target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    public Object citresewn$fixDuplicatePrefixSuffix(Object key) {
        ResourceLocation original = (ResourceLocation) key;
        if (TypeItem.CONTAINER.active() && original.getPath().startsWith("models/models/") && original.getPath().endsWith(".json.json") && original.getPath().contains("cit"))
            return ResourceLocation.fromNamespaceAndPath(original.getNamespace(), original.getPath().substring(7, original.getPath().length() - 5));

        return original;
    }
}
