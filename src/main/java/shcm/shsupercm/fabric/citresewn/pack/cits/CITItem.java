package shcm.shsupercm.fabric.citresewn.pack.cits;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import org.apache.commons.io.IOUtils;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.ex.CITLoadException;
import shcm.shsupercm.fabric.citresewn.ex.CITParseException;
import shcm.shsupercm.fabric.citresewn.mixin.cititem.JsonUnbakedModelAccessor;
import shcm.shsupercm.fabric.citresewn.pack.CITPack;
import shcm.shsupercm.fabric.citresewn.pack.ResewnItemModelIdentifier;
import shcm.shsupercm.fabric.citresewn.pack.ResewnTextureIdentifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("deprecation")
public class CITItem extends CIT {
    private static final String GENERATED_SUB_CITS_PREFIX = "sub_cititem_generated_";
    public static final Set<ResourceLocation> GENERATED_SUB_CITS_SEEN = new HashSet<>();

    public Map<ResourceLocation, ResourceLocation> assetIdentifiers = new LinkedHashMap<>();
    public Map<List<ItemOverride.Predicate>, BlockModel> unbakedAssets = new LinkedHashMap<>();
    private Map<String, Either<Material, String>> textureOverrideMap = new HashMap<>();
    private boolean isTexture = false;

    public BakedModel bakedModel = null;
    public CITOverrideList bakedSubModels = new CITOverrideList();

    public CITItem(CITPack pack, ResourceLocation identifier, Properties properties) throws CITParseException {
        super(pack, identifier, properties);


        try {
            if (this.items.size() == 0)
                throw new Exception("CIT must target at least one item type");

            ResourceLocation assetIdentifier;
            boolean containsTexture = false;
            String modelProp = properties.getProperty("model");
            if (modelProp == null)
                for (Object o : properties.keySet())
                    if (o instanceof String property && (property.startsWith("texture") || property.startsWith("tile"))) {
                        containsTexture = true;
                        break;
                    }
            if (!containsTexture) {
                assetIdentifier = resolvePath(identifier, modelProp, ".json",resourceManager);
                if (assetIdentifier != null)
                    assetIdentifiers.put(null, assetIdentifier);
                else if (modelProp != null) {
                    assetIdentifier = resolvePath(identifier, modelProp, ".json", resourceManager);
                    if (assetIdentifier != null)
                        assetIdentifiers.put(null, assetIdentifier);
                }
            }

            for (Object o : properties.keySet())
                if (o instanceof String property && property.startsWith("model.")) {
                    ResourceLocation subIdentifier = resolvePath(identifier, properties.getProperty(property), ".json", resourceManager);
                    if (subIdentifier == null)
                        throw new Exception("Cannot resolve path for " + property);

                    String subItem = property.substring(6);
                    ResourceLocation subItemIdentifier = fixDeprecatedSubItem(subItem);
                    assetIdentifiers.put(subItemIdentifier == null ? new ResourceLocation("minecraft", "item/" + subItem) : subItemIdentifier, subIdentifier);
                }

            if (assetIdentifiers.size() == 0) { // attempt to load texture
                isTexture = true;
                String textureProp = properties.getProperty("texture");
                if (textureProp == null)
                    textureProp = properties.getProperty("tile");
                assetIdentifier = resolvePath(identifier, textureProp, ".png", resourceManager);
                if (assetIdentifier != null)
                    assetIdentifiers.put(null, assetIdentifier);

                for (Object o : properties.keySet())
                    if (o instanceof String property && property.startsWith("texture.")) {
                        ResourceLocation subIdentifier = resolvePath(identifier, properties.getProperty(property), ".png", resourceManager);
                        if (subIdentifier == null)
                            throw new Exception("Cannot resolve path for " + property);

                        String subItem = property.substring(8);
                        ResourceLocation subItemIdentifier = fixDeprecatedSubItem(subItem);
                        assetIdentifiers.put(subItemIdentifier == null ? new ResourceLocation("minecraft", "item/" + subItem) : subItemIdentifier, subIdentifier);
                    }
            } else { // attempt to load textureOverrideMap from textures
                String textureProp = properties.getProperty("texture");
                if (textureProp == null)
                    textureProp = properties.getProperty("tile");
                if (textureProp != null) {
                    assetIdentifier = resolvePath(identifier, textureProp, ".png",resourceManager);
                    if (assetIdentifier != null)
                        textureOverrideMap.put(null, Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, new ResewnTextureIdentifier(assetIdentifier))));
                    else
                        throw new Exception("Cannot resolve path for texture");
                }

                for (Object o : properties.keySet())
                    if (o instanceof String property && property.startsWith("texture.")) {
                        textureProp = properties.getProperty(property);
                        ResourceLocation subIdentifier = resolvePath(identifier, textureProp, ".png",resourceManager);
                        if (subIdentifier == null)
                            throw new Exception("Cannot resolve path for " + property);

                        textureOverrideMap.put(property.substring(8), Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, new ResewnTextureIdentifier(subIdentifier))));
                    }
            }

            if (assetIdentifiers.size() == 0)
                throw new Exception("Cannot resolve path for model/texture");
        } catch (Exception e) {
            throw new CITParseException(pack.resourcePack, identifier, (e.getClass() == Exception.class ? "" : e.getClass().getSimpleName() + ": ") + e.getMessage());
        }
    }
    
    public ResourceManager resourceManager;
    
    public boolean containsResource(ResourceLocation identifier) {
    	return resourceManager.getResource(identifier).isPresent();
    }

    public void loadUnbakedAssets(ResourceManager resourceManager) throws CITLoadException {
        try {
            if (isTexture) {
                BlockModel itemJson = getModelForFirstItemType(resourceManager);
                if (((JsonUnbakedModelAccessor) itemJson).getTextureMap().size() > 1) { // use(some/all of) the asset identifiers to build texture override in layered models
                    textureOverrideMap = ((JsonUnbakedModelAccessor) itemJson).getTextureMap();
                    ResourceLocation defaultAsset = assetIdentifiers.get(null);
                    textureOverrideMap.replaceAll((layerName, originalTextureEither) -> {
                        ResourceLocation textureIdentifier = assetIdentifiers.remove(originalTextureEither.map(Material::texture, ResourceLocation::new));
                        if (textureIdentifier != null)
                            return Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, new ResewnTextureIdentifier(textureIdentifier)));
                        if (defaultAsset != null)
                            return Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, new ResewnTextureIdentifier(defaultAsset)));
                        return null;
                    });

                    if (assetIdentifiers.size() == 0 || (assetIdentifiers.size() == 1 && assetIdentifiers.containsKey(null))) {
                        unbakedAssets.put(null, itemJson);
                        return;
                    }
                }

                ResourceLocation baseIdentifier = assetIdentifiers.remove(null);

                if (baseIdentifier != null)
                    unbakedAssets.put(null, loadUnbakedAsset(resourceManager, baseIdentifier));

                if (!assetIdentifiers.isEmpty()) { // contains sub models
                    LinkedHashMap<ResourceLocation, List<ItemOverride.Predicate>> overrideConditions = new LinkedHashMap<>();
                    for (Item item : this.items) {
                        ResourceLocation itemIdentifier = BuiltInRegistries.ITEM.getKey(item);
                        overrideConditions.put(new ResourceLocation(itemIdentifier.getNamespace(), "item/" + itemIdentifier.getPath()), Collections.emptyList());

                        Resource itemModelResource = null;
                        ResourceLocation itemModelIdentifier = new ResourceLocation(itemIdentifier.getNamespace(), "models/item/" + itemIdentifier.getPath() + ".json");
                        try {
                        	itemModelResource = resourceManager.getResource(itemModelIdentifier).get();
                        	Reader resourceReader = new InputStreamReader(itemModelResource.open());
                            BlockModel itemModelJson = BlockModel.fromStream(resourceReader);

                            if (itemModelJson.getOverrides() != null && !itemModelJson.getOverrides().isEmpty())
                                for (ItemOverride override : itemModelJson.getOverrides())
                                    overrideConditions.put(override.getModel(), override.getPredicates().toList());
                        }finally {
                        }
                    }

                    ArrayList<ResourceLocation> overrideModels = new ArrayList<>(overrideConditions.keySet());
                    Collections.reverse(overrideModels);

                    for (ResourceLocation overrideModel : overrideModels) {
                        ResourceLocation replacement = assetIdentifiers.remove(overrideModel);
                        if (replacement == null)
                            continue;

                        List<ItemOverride.Predicate> conditions = overrideConditions.get(overrideModel);
                        unbakedAssets.put(conditions, loadUnbakedAsset(resourceManager, replacement));
                    }
                }
            } else { // isModel
                ResourceLocation baseIdentifier = assetIdentifiers.remove(null);

                if (baseIdentifier != null) {
                    if (!GENERATED_SUB_CITS_SEEN.add(baseIdentifier)) // cit generated duplicate
                        baseIdentifier = new ResourceLocation(baseIdentifier.getNamespace(), GENERATED_SUB_CITS_PREFIX + GENERATED_SUB_CITS_SEEN.size() + "_" + baseIdentifier.getPath());
                    GENERATED_SUB_CITS_SEEN.add(baseIdentifier);

                    BlockModel model = loadUnbakedAsset(resourceManager, baseIdentifier);
                    unbakedAssets.put(null, model);

                    if (model.getOverrides().size() > 0 && textureOverrideMap.size() > 0) {
                        LinkedHashMap<ResourceLocation, List<ItemOverride.Predicate>> overrideConditions = new LinkedHashMap<>();

                        for (ItemOverride override : model.getOverrides())
                            overrideConditions.put(override.getModel(), override.getPredicates().toList());

                        ArrayList<ResourceLocation> overrideModels = new ArrayList<>(overrideConditions.keySet());
                        Collections.reverse(overrideModels);

                        for (ResourceLocation overrideModel : overrideModels) {
                        	this.resourceManager = resourceManager;
                            ResourceLocation replacement = resolvePath(baseIdentifier, overrideModel.toString(), ".json", resourceManager);
                            if (replacement != null) {
                                String subTexturePath = replacement.toString().substring(0, replacement.toString().lastIndexOf('.'));
                                final String subTextureName = subTexturePath.substring(subTexturePath.lastIndexOf('/') + 1);

                                replacement = baseIdentifier;
                                if (!GENERATED_SUB_CITS_SEEN.add(replacement)) // cit generated duplicate
                                    replacement = new ResourceLocation(replacement.getNamespace(), GENERATED_SUB_CITS_PREFIX + GENERATED_SUB_CITS_SEEN.size() + "_" + replacement.getPath());
                                GENERATED_SUB_CITS_SEEN.add(replacement);

                                BlockModel jsonModel = loadUnbakedAsset(resourceManager, replacement);
                                jsonModel.getOverrides().clear();

                                ((JsonUnbakedModelAccessor) jsonModel).getTextureMap().replaceAll((layerName, texture) -> {
                                    if (layerName != null)
                                        try {
                                            for (String subTexture : textureOverrideMap.keySet())
                                                if (subTextureName.equals(subTexture))
                                                    return textureOverrideMap.get(subTexture);
                                        } catch (Exception ignored) { }
                                    return texture;
                                });

                                unbakedAssets.put(overrideConditions.get(overrideModel), jsonModel);
                            }
                        }
                    }
                }

                if (!assetIdentifiers.isEmpty()) { // contains sub models
                    LinkedHashMap<ResourceLocation, List<ItemOverride.Predicate>> overrideConditions = new LinkedHashMap<>();
                    for (Item item : this.items) {
                        ResourceLocation itemIdentifier = BuiltInRegistries.ITEM.getKey(item);
                        overrideConditions.put(new ResourceLocation(itemIdentifier.getNamespace(), "item/" + itemIdentifier.getPath()), Collections.emptyList());

                        ResourceLocation itemModelIdentifier = new ResourceLocation(itemIdentifier.getNamespace(), "models/item/" + itemIdentifier.getPath() + ".json");
                        try {
                            Resource itemModelResource = resourceManager.getResource(itemModelIdentifier).get();
                            Reader resourceReader = new InputStreamReader(itemModelResource.open());
                            BlockModel itemModelJson = BlockModel.fromStream(resourceReader);

                            if (itemModelJson.getOverrides() != null && !itemModelJson.getOverrides().isEmpty())
                                for (ItemOverride override : itemModelJson.getOverrides())
                                    overrideConditions.put(override.getModel(), override.getPredicates().toList());
                        }finally{
                        }
                    }

                    ArrayList<ResourceLocation> overrideModels = new ArrayList<>(overrideConditions.keySet());
                    Collections.reverse(overrideModels);

                    for (ResourceLocation overrideModel : overrideModels) {
                        ResourceLocation replacement = assetIdentifiers.remove(overrideModel);
                        if (replacement == null)
                            continue;

                        if (!GENERATED_SUB_CITS_SEEN.add(replacement)) // cit generated duplicate
                            replacement = new ResourceLocation(replacement.getNamespace(), GENERATED_SUB_CITS_PREFIX + GENERATED_SUB_CITS_SEEN.size() + "_" + replacement.getPath());
                        GENERATED_SUB_CITS_SEEN.add(replacement);

                        List<ItemOverride.Predicate> conditions = overrideConditions.get(overrideModel);
                        unbakedAssets.put(conditions, loadUnbakedAsset(resourceManager, replacement));
                    }
                }
            }
        } catch (Exception e) {
            throw new CITLoadException(pack.resourcePack, propertiesIdentifier, (e.getClass() == Exception.class ? "" : e.getClass().getSimpleName() + ": ") + e.getMessage());
        } finally {
            assetIdentifiers = null;
            textureOverrideMap = null;
        }
    }

    private BlockModel loadUnbakedAsset(ResourceManager resourceManager, ResourceLocation assetIdentifier) throws Exception {
        final ResourceLocation identifier;
        {
            ResourceLocation possibleIdentifier = assetIdentifier;
            while (possibleIdentifier.getPath().startsWith(GENERATED_SUB_CITS_PREFIX))
                possibleIdentifier = new ResourceLocation(possibleIdentifier.getNamespace(), possibleIdentifier.getPath().substring(possibleIdentifier.getPath().substring(GENERATED_SUB_CITS_PREFIX.length()).indexOf('_') + GENERATED_SUB_CITS_PREFIX.length() + 1));
            identifier = possibleIdentifier;
        }
        BlockModel json;
        if (identifier.getPath().endsWith(".json")) {
            InputStream is = null;
            Resource resource = null;
            try {
                json = BlockModel.fromString(IOUtils.toString(is = (resource = resourceManager.getResource(identifier).get()).open(), StandardCharsets.UTF_8));
                json.name = assetIdentifier.toString();
                json.name = json.name.substring(0, json.name.length() - 5);

                ((JsonUnbakedModelAccessor) json).getTextureMap().replaceAll((layer, original) -> {
                    Optional<Material> left = original.left();
                    if (left.isPresent()) {
                    	this.resourceManager = resourceManager;
                        ResourceLocation resolvedIdentifier = resolvePath(identifier, left.get().texture().getPath(), ".png", resourceManager);
                        if (resolvedIdentifier != null)
                            return Either.left(new Material(left.get().atlasLocation(), new ResewnTextureIdentifier(resolvedIdentifier)));
                    }
                    return original;
                });

                if (textureOverrideMap.size() > 0) {
                    Map<String, Either<Material, String>> jsonTextureMap = ((JsonUnbakedModelAccessor) json).getTextureMap();
                    if (jsonTextureMap.size() == 0)
                        jsonTextureMap.put("layer0", null);

                    final Either<Material, String> defaultTextureOverride = textureOverrideMap.get(null);
                    if (defaultTextureOverride != null)
                        jsonTextureMap.replaceAll((layerName, spriteIdentifierStringEither) -> defaultTextureOverride);

                    //jsonTextureMap.putAll(textureOverrideMap);
                    jsonTextureMap.replaceAll((layerName, texture) -> {
                        if (layerName != null)
                            try {
                                String[] split = texture.map(id -> id.texture().getPath(), s -> s).split("/");
                                String textureName = split[split.length - 1];
                                if (textureName.endsWith(".png"))
                                    textureName = textureName.substring(0, textureName.length() - 4);
                                return Objects.requireNonNull(textureOverrideMap.get(textureName));
                            } catch (Exception ignored) { }
                        return texture;
                    });
                    jsonTextureMap.values().removeIf(Objects::isNull);
                }

                ResourceLocation parentId = ((JsonUnbakedModelAccessor) json).getParentLocation();
                if (parentId != null) {
                    String[] parentIdPathSplit = parentId.getPath().split("/");
                    if (parentId.getPath().startsWith("./") || (parentIdPathSplit.length > 2 && parentIdPathSplit[1].equals("cit"))) {
                        parentId = resolvePath(identifier, parentId.getPath(), ".json",resourceManager);
                        if (parentId != null)
                            ((JsonUnbakedModelAccessor) json).setParentLocation(new ResewnItemModelIdentifier(parentId));
                    }
                }

                json.getOverrides().replaceAll(override -> {
                    String[] modelIdPathSplit = override.getModel().getPath().split("/");
                    if (override.getModel().getPath().startsWith("./") || (modelIdPathSplit.length > 2 && modelIdPathSplit[1].equals("cit"))) {
                        ResourceLocation resolvedOverridePath = resolvePath(identifier, override.getModel().getPath(), ".json", resourceManager);
                        if (resolvedOverridePath != null)
                            return new ItemOverride(new ResewnItemModelIdentifier(resolvedOverridePath), override.getPredicates().collect(Collectors.toList()));
                    }

                    return override;
                });

                return json;
            } finally {
                IOUtils.closeQuietly(is);
            }
        } else if (identifier.getPath().endsWith(".png")) {
            json = getModelForFirstItemType(resourceManager);
            if (json == null)
                json = new BlockModel(new ResourceLocation("minecraft", "item/generated"), new ArrayList<>(), ImmutableMap.of("layer0", Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, new ResewnTextureIdentifier(identifier)))), true, BlockModel.GuiLight.FRONT, ItemTransforms.NO_TRANSFORMS, new ArrayList<>());
            json.getOverrides().clear();
            json.name = identifier.toString();
            json.name = json.name.substring(0, json.name.length() - 4);

            ((JsonUnbakedModelAccessor) json).getTextureMap().replaceAll((layerName, originalTextureEither) -> {
                if (textureOverrideMap.size() > 0) {
                    Either<Material, String> textureOverride = textureOverrideMap.get(layerName);
                    if (textureOverride == null)
                        textureOverride = textureOverrideMap.get(null);
                    return textureOverride == null ? originalTextureEither : textureOverride;
                } else
                    return Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, new ResewnTextureIdentifier(identifier)));
            });
            return json;
        }

        throw new Exception("Unknown asset type");
    }

    public ResourceLocation fixDeprecatedSubItem(String subItem) {
        String replacement = switch (subItem) {
            case "bow_pulling_standby" -> "bow";
            case "crossbow_standby" -> "crossbow";
            case "potion_bottle_drinkable" -> "potion";
            case "potion_bottle_splash" -> "splash_potion";
            case "potion_bottle_lingering" -> "lingering_potion";


            default -> null;
        };

        if (replacement != null) {
            CITResewn.logWarnLoading("CIT Warning: Using deprecated sub item id \"" + subItem + "\" instead of \"" + replacement + "\" in " + pack.resourcePack.packId() + " -> " + propertiesIdentifier.toString());

            return new ResourceLocation("minecraft", "item/" + replacement);
        }

        return null;
    }

    private BlockModel getModelForFirstItemType(ResourceManager resourceManager) {
        ResourceLocation firstItemIdentifier = BuiltInRegistries.ITEM.getKey(this.items.iterator().next()), firstItemModelIdentifier = new ResourceLocation(firstItemIdentifier.getNamespace(), "models/item/" + firstItemIdentifier.getPath() + ".json");
        Resource itemModelResource = null;
        try {
            BlockModel json = BlockModel.fromString(IOUtils.toString((itemModelResource = resourceManager.getResource(firstItemModelIdentifier).get()).open(), StandardCharsets.UTF_8));

            if (!GENERATED_SUB_CITS_SEEN.add(firstItemModelIdentifier)) // cit generated duplicate
                firstItemModelIdentifier = new ResourceLocation(firstItemModelIdentifier.getNamespace(), GENERATED_SUB_CITS_PREFIX + GENERATED_SUB_CITS_SEEN.size() + "_" + firstItemModelIdentifier.getPath());
            GENERATED_SUB_CITS_SEEN.add(firstItemModelIdentifier);

            json.name = firstItemModelIdentifier.toString();
            json.name = json.name.substring(0, json.name.length() - 5);
            return json;
        } catch (Exception e) {
            return null;
        }
    }

    public BakedModel getItemModel(ItemStack stack, ClientLevel world, LivingEntity entity, int seed) {
        // get sub items or bakedModel if no sub item matches @Nullable
        BakedModel bakedModel = bakedSubModels.resolve(this.bakedModel, stack, world, entity, seed);

        // apply model overrides
        if (bakedModel != null && bakedModel.getOverrides() != null)
            bakedModel = bakedModel.getOverrides().resolve(bakedModel, stack, world, entity, seed);

        return bakedModel;
    }

    public static class CITOverrideList extends ItemOverrides {
        public void override(List<ItemOverride.Predicate> key, BakedModel bakedModel) {
            Set<ResourceLocation> conditionTypes = new LinkedHashSet<>(Arrays.asList(this.properties));
            for (ItemOverride.Predicate condition : key)
                conditionTypes.add(condition.getProperty());
            this.properties = conditionTypes.toArray(new ResourceLocation[0]);

            this.overrides = Arrays.copyOf(this.overrides, this.overrides.length + 1);

            Object2IntMap<ResourceLocation> object2IntMap = new Object2IntOpenHashMap<>();
            for(int i = 0; i < this.properties.length; ++i)
                object2IntMap.put(this.properties[i], i);

            this.overrides[this.overrides.length - 1] = new BakedOverride(
                    key.stream()
                        .map((condition) -> new PropertyMatcher(object2IntMap.getInt(condition.getProperty()), condition.getValue()))
                        .toArray(PropertyMatcher[]::new)
                    , bakedModel);
        }
    }

    public interface Cached {
        CITItem citresewn_getCachedCITItem(Supplier<CITItem> realtime);

        boolean citresewn_isMojankCIT();
        void citresewn_setMojankCIT(boolean mojankCIT);
    }
}
