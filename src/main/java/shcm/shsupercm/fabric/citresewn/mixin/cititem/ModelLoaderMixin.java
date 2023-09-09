package shcm.shsupercm.fabric.citresewn.mixin.cititem;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.pack.ResewnItemModelIdentifier;
import shcm.shsupercm.fabric.citresewn.pack.ResewnTextureIdentifier;
import shcm.shsupercm.fabric.citresewn.pack.cits.CIT;
import shcm.shsupercm.fabric.citresewn.pack.cits.CITItem;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import static shcm.shsupercm.fabric.citresewn.CITResewn.info;

@Mixin(ModelBakery.class)
public class ModelLoaderMixin {
    protected ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
    @Shadow @Final private Set<ResourceLocation> loadingStack;
    @Shadow @Final private Map<ResourceLocation, UnbakedModel> topLevelModels;
    @Shadow @Final private Map<ResourceLocation, UnbakedModel> unbakedCache;
    @Shadow @Final private Map<ResourceLocation, BakedModel> bakedTopLevelModels;

    @Inject(method = "loadTopLevel", at = @At("TAIL"))//todo: is this correct
    public void addCITItemModels(ModelResourceLocation eventModelId, CallbackInfo ci) { if (eventModelId != ModelBakery.MISSING_MODEL_LOCATION) return;
        if (CITResewn.INSTANCE.activeCITs == null)
            return;

        info("Loading CITItem models...");
        CITResewn.INSTANCE.activeCITs.citItems.values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .forEach(citItem -> {
                    try {
                        citItem.loadUnbakedAssets(resourceManager);

                        for (BlockModel unbakedModel : citItem.unbakedAssets.values()) {
                            ResewnItemModelIdentifier id = new ResewnItemModelIdentifier(unbakedModel.name);
                            this.unbakedCache.put(id, unbakedModel);
                            this.loadingStack.addAll(unbakedModel.getDependencies());
                            this.topLevelModels.put(id, unbakedModel);
                        }
                    } catch (Exception e) {
                        CITResewn.logErrorLoading(e.getMessage());
                    }
                });

        CITItem.GENERATED_SUB_CITS_SEEN.clear();
    }

    @Inject(method = "bakeModels", at = @At("RETURN"))
    public void linkBakedCITItemModels(BiFunction<ResourceLocation, Material, TextureAtlasSprite> map, CallbackInfo ci) {
        if (CITResewn.INSTANCE.activeCITs == null)
            return;

   //     profiler.push("citresewn_linking");
        info("Linking baked models to CITItems...");

        if (CITResewn.INSTANCE.activeCITs != null) {
            for (CITItem citItem : CITResewn.INSTANCE.activeCITs.citItems.values().stream().flatMap(Collection::stream).distinct().collect(Collectors.toList())) {
                for (Map.Entry<List<ItemOverride.Predicate>, BlockModel> citModelEntry : citItem.unbakedAssets.entrySet()) {
                    if (citModelEntry.getKey() == null) {
                        citItem.bakedModel = this.bakedTopLevelModels.get(new ResewnItemModelIdentifier(citModelEntry.getValue().name));
                    } else {
                        BakedModel bakedModel = bakedTopLevelModels.get(new ResewnItemModelIdentifier(citModelEntry.getValue().name));

                        if (bakedModel == null)
                            CITResewn.logWarnLoading("Skipping sub cit: Failed loading model for \"" + citModelEntry.getValue().name + "\" in " + citItem.pack.resourcePack.packId() + " -> " + citItem.propertiesIdentifier.getPath());
                        else
                            citItem.bakedSubModels.override(citModelEntry.getKey(), bakedModel);
                    }
                }
                citItem.unbakedAssets = null;
            }
        }

     //   profiler.pop();
    }


    @Inject(method = "loadBlockModel", cancellable = true, at = @At("HEAD"))
    public void forceLiteralResewnModelIdentifier(ResourceLocation id, CallbackInfoReturnable<BlockModel> cir) {
        if (id instanceof ResewnItemModelIdentifier) {
            InputStream is = null;
            Resource resource = null;
            try {
                BlockModel json = BlockModel.fromString(IOUtils.toString(is = (resource = resourceManager.getResource(id).get()).open(), StandardCharsets.UTF_8));
                json.name = id.toString();
                json.name = json.name.substring(0, json.name.length() - 5);

                ((JsonUnbakedModelAccessor) json).getTextureMap().replaceAll((layer, original) -> {
                    Optional<Material> left = original.left();
                    if (left.isPresent()) {
                        String originalPath = left.get().texture().getPath();
                        String[] split = originalPath.split("/");
                        if (originalPath.startsWith("./") || (split.length > 2 && split[1].equals("cit"))) {
                            ResourceLocation resolvedIdentifier = CIT.resolvePath(id, originalPath, ".png",  resourceManager);
                            if (resolvedIdentifier != null)
                                return Either.left(new Material(left.get().atlasLocation(), new ResewnTextureIdentifier(resolvedIdentifier)));
                        }
                    }
                    return original;
                });

                ResourceLocation parentId = ((JsonUnbakedModelAccessor) json).getParentLocation();
                if (parentId != null) {
                    String[] parentIdPathSplit = parentId.getPath().split("/");
                    if (parentId.getPath().startsWith("./") || (parentIdPathSplit.length > 2 && parentIdPathSplit[1].equals("cit"))) {
                        parentId = CIT.resolvePath(id, parentId.getPath(), ".json", resourceManager);
                        if (parentId != null)
                            ((JsonUnbakedModelAccessor) json).setParentLocation(new ResewnItemModelIdentifier(parentId));
                    }
                }

                json.getOverrides().replaceAll(override -> {
                    String[] modelIdPathSplit = override.getModel().getPath().split("/");
                    if (override.getModel().getPath().startsWith("./") || (modelIdPathSplit.length > 2 && modelIdPathSplit[1].equals("cit"))) {
                        ResourceLocation resolvedOverridePath = CIT.resolvePath(id, override.getModel().getPath(), ".json", resourceManager);
                        if (resolvedOverridePath != null)
                            return new ItemOverride(new ResewnItemModelIdentifier(resolvedOverridePath), override.getPredicates().collect(Collectors.toList()));
                    }

                    return override;
                });

                cir.setReturnValue(json);
            } catch (Exception ignored) {
            } finally {
                IOUtils.closeQuietly(is);
            }
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
