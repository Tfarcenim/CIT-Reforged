package shcm.shsupercm;

import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.io.IOUtils;
import shcm.shsupercm.fabric.citresewn.ActiveCITs;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;
import shcm.shsupercm.fabric.citresewn.mixin.cititem.JsonUnbakedModelAccessor;
import shcm.shsupercm.fabric.citresewn.pack.CITPack;
import shcm.shsupercm.fabric.citresewn.pack.CITParser;
import shcm.shsupercm.fabric.citresewn.pack.ResewnItemModelIdentifier;
import shcm.shsupercm.fabric.citresewn.pack.ResewnTextureIdentifier;
import shcm.shsupercm.fabric.citresewn.pack.cits.CIT;
import shcm.shsupercm.fabric.citresewn.pack.cits.CITItem;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static shcm.shsupercm.fabric.citresewn.CITResewn.info;

public class CITHooks {

    public static void initCITS(ResourceLocation eventId) {
        if (eventId != ModelBakery.MISSING_MODEL_LOCATION) return;
        if (CITResewn.INSTANCE.activeCITs != null) {
            info("Clearing active CITs..");
            CITResewn.INSTANCE.activeCITs.dispose();
            CITResewn.INSTANCE.activeCITs = null;
        }

        if (!CITResewnConfig.INSTANCE().enabled)
            return;

        info("Parsing CITs...");
        List<CITPack> parsedPacks = CITParser.parseCITs(Minecraft.getInstance().getResourceManager().listPacks().collect(Collectors.toCollection(ArrayList::new)));
        List<CIT> parsed = parsedPacks.stream().flatMap(pack -> pack.cits.stream()).collect(Collectors.toCollection(ArrayList::new));

        if (parsed.size() > 0) {
            info("Activating CITs...");
            CITResewn.INSTANCE.activeCITs = new ActiveCITs(parsedPacks, parsed);
        } else
            info("No cit packs found.");
    }

    public static void linkBakedCITItemModels(ModelBakery bakery) {
        if (CITResewn.INSTANCE.activeCITs == null)
            return;

        //     profiler.push("citresewn_linking");
        info("Linking baked models to CITItems...");

        for (Map.Entry<ResourceLocation,BakedModel> entry : bakery.getBakedTopLevelModels().entrySet()) {
            if (entry.getKey().getPath().contains("optifine")) {
                System.out.println(entry.getValue().getParticleIcon());
            }
        }

        List<CITItem> citItems = CITResewn.INSTANCE.activeCITs.citItems.values().stream().flatMap(Collection::stream).distinct().toList();
        for (CITItem citItem : citItems) {
            for (Map.Entry<List<ItemOverride.Predicate>, BlockModel> citModelEntry : citItem.unbakedAssets.entrySet()) {
                ResewnItemModelIdentifier resewnItemModelIdentifier = new ResewnItemModelIdentifier(citModelEntry.getValue().name);
                BakedModel newBakedModel = bakery.getBakedTopLevelModels().get(resewnItemModelIdentifier);

                if (citModelEntry.getKey() == null) {
                    citItem.bakedModel = newBakedModel;
                } else {

                    if (newBakedModel == null)
                        CITResewn.logWarnLoading("Skipping sub cit: Failed loading model for \"" + citModelEntry.getValue().name + "\" in " + citItem.pack.resourcePack.packId() + " -> " + citItem.propertiesIdentifier.getPath());
                    else
                        citItem.bakedSubModels.override(citModelEntry.getKey(), newBakedModel);
                }
            }
            citItem.unbakedAssets = null;
        }
    }

    public static BlockModel forceLiteralResewnModelIdentifier(ResewnItemModelIdentifier identifier) {
        InputStream is = null;
        Resource resource = null;
        try {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            BlockModel json = BlockModel.fromString(IOUtils.toString(is = (resource = resourceManager.getResource(identifier).get()).open(), StandardCharsets.UTF_8));
            json.name = identifier.toString();
            json.name = json.name.substring(0, json.name.length() - 5);

            ((JsonUnbakedModelAccessor) json).getTextureMap().replaceAll((layer, original) -> {
                Optional<Material> left = original.left();
                if (left.isPresent()) {
                    String originalPath = left.get().texture().getPath();
                    String[] split = originalPath.split("/");
                    if (originalPath.startsWith("./") || (split.length > 2 && split[1].equals("cit"))) {
                        ResourceLocation resolvedIdentifier = CIT.resolvePath(identifier, originalPath, ".png",  resourceManager);
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
                    parentId = CIT.resolvePath(identifier, parentId.getPath(), ".json", resourceManager);
                    if (parentId != null)
                        ((JsonUnbakedModelAccessor) json).setParentLocation(new ResewnItemModelIdentifier(parentId));
                }
            }

            json.getOverrides().replaceAll(override -> {
                String[] modelIdPathSplit = override.getModel().getPath().split("/");
                if (override.getModel().getPath().startsWith("./") || (modelIdPathSplit.length > 2 && modelIdPathSplit[1].equals("cit"))) {
                    ResourceLocation resolvedOverridePath = CIT.resolvePath(identifier, override.getModel().getPath(), ".json", resourceManager);
                    if (resolvedOverridePath != null)
                        return new ItemOverride(new ResewnItemModelIdentifier(resolvedOverridePath), override.getPredicates().collect(Collectors.toList()));
                }

                return override;
            });

            return json;
        } catch (Exception ignored) {
        } finally {
            IOUtils.closeQuietly(is);
        }
        return null;
    }

    public static void addCITItemModels(ModelBakery bakery, ResourceLocation eventModelId, Map<ResourceLocation, UnbakedModel> unbakedCache, Set<ResourceLocation> loadingStack, Map<ResourceLocation, UnbakedModel> topLevelModels) {
        if (eventModelId != ModelBakery.MISSING_MODEL_LOCATION) return;
        if (CITResewn.INSTANCE.activeCITs == null)
            return;

        info("Loading CITItem models...");

        CITResewn.INSTANCE.activeCITs.citItems.values().stream()
                .flatMap(Collection::stream)
                .distinct()
                .forEach(citItem -> {
                    try {
                        citItem.loadUnbakedAssets(Minecraft.getInstance().getResourceManager());

                        for (BlockModel unbakedModel : citItem.unbakedAssets.values()) {
                            ResewnItemModelIdentifier id = new ResewnItemModelIdentifier(unbakedModel.name);
                            unbakedCache.put(id, unbakedModel);
                          //  loadingStack.addAll(unbakedModel.getDependencies());
                            topLevelModels.put(id, unbakedModel);
                        }
                    } catch (Exception e) {
                        CITResewn.logErrorLoading(e.getMessage());
                    }
                });

        CITItem.GENERATED_SUB_CITS_SEEN.clear();
    }
}
