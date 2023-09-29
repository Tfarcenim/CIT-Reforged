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
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.io.IOUtils;
import shcm.shsupercm.forge.citresewn.ActiveCITs;
import shcm.shsupercm.forge.citresewn.CITResewn;
import shcm.shsupercm.forge.citresewn.config.CITResewnConfig;
import shcm.shsupercm.forge.citresewn.mixin.cititem.JsonUnbakedModelAccessor;
import shcm.shsupercm.forge.citresewn.pack.CITPack;
import shcm.shsupercm.forge.citresewn.pack.CITParser;
import shcm.shsupercm.forge.citresewn.pack.ResewnItemModelIdentifier;
import shcm.shsupercm.forge.citresewn.pack.cits.CIT;
import shcm.shsupercm.forge.citresewn.pack.cits.CITItem;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static shcm.shsupercm.forge.citresewn.CITResewn.info;

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

        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();


        info("Parsing CITs...");
        List<CITPack> parsedPacks = CITParser.parseCITs(resourceManager.listPacks().collect(Collectors.toCollection(ArrayList::new)));
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
        try {
            ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
            BlockModel json = BlockModel.fromString(IOUtils.toString(is = resourceManager.getResource(identifier).get().open(), StandardCharsets.UTF_8));
            json.name = identifier.toString();
            json.name = json.name.substring(0, json.name.length() - 5);

            json.textureMap.replaceAll((layer, original) -> {
                Optional<Material> left = original.left();
                if (left.isPresent()) {
                    String originalPath = left.get().texture().getPath();
                    String[] split = originalPath.split("/");
                    if (originalPath.startsWith("./") || (split.length > 2 && split[1].equals("cit"))) {
                        ResourceLocation resolvedIdentifier = CIT.resolvePath(identifier, originalPath, ".png",  resourceManager);
                        if (resolvedIdentifier != null)
                            return Either.left(new Material(left.get().atlasLocation(), new ResewnItemModelIdentifier(resolvedIdentifier)));
                    }
                }
                return original;
            });

            ResourceLocation parentId = json.getParentLocation();
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
        if (eventModelId != ModelBakery.MISSING_MODEL_LOCATION)return;

        CITHooks.initCITS(eventModelId);

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
                            loadingStack.addAll(unbakedModel.getDependencies());
                            topLevelModels.put(id, unbakedModel);
                        }
                    } catch (Exception e) {
                        CITResewn.logErrorLoading(e.getMessage());
                    }
                });
        CITItem.GENERATED_SUB_CITS_SEEN.clear();
    }

    public static ResourceLocation getArmorTextures(ItemStack item, EquipmentSlot layer2, String overlay, WeakReference<Map<String, ResourceLocation>> armorTexturesCached) {
        if (armorTexturesCached == null)
            return null;
        Map<String, ResourceLocation> armorTextures = armorTexturesCached.get();
        if (armorTextures == null)
            return null;
        ArmorItem armorItem = (ArmorItem)item.getItem();
        ResourceLocation identifier = armorTextures.get(armorItem.getMaterial().getName() + "_layer_" + (layer2== EquipmentSlot.LEGS ? "2" : "1")
                + (overlay == null ? "" : "_" + overlay));
        return identifier;
    }

}
