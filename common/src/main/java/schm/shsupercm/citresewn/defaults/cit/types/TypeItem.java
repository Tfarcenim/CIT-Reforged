package schm.shsupercm.citresewn.defaults.cit.types;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.io.IOUtils;
import schm.shsupercm.citresewn.CITResewn;
import schm.shsupercm.citresewn.api.CITTypeContainer;
import schm.shsupercm.citresewn.cit.*;
import schm.shsupercm.citresewn.defaults.cit.conditions.ConditionItems;
import schm.shsupercm.citresewn.defaults.common.ResewnItemModelIdentifier;
import schm.shsupercm.citresewn.pack.format.PropertyGroup;
import schm.shsupercm.citresewn.pack.format.PropertyKey;
import schm.shsupercm.citresewn.pack.format.PropertyValue;
import schm.shsupercm.citresewn.mixin.types.item.JsonUnbakedModelAccessor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * ///// PORTED FROM BETA \\\\\
 * This shit was ported from the
 * beta and will be rewritten at
 * some point!
 * \\\\\                  /////
 */
public class TypeItem extends CITType {
    //@Entrypoint(CITTypeContainer.ENTRYPOINT)
    public static final Container CONTAINER = new Container();

    private static final String GENERATED_SUB_CITS_PREFIX = "sub_cititem_generated_";
    public static final Set<ResourceLocation> GENERATED_SUB_CITS_SEEN = new HashSet<>();

    private final List<Item> items = new ArrayList<>();

    public Map<ResourceLocation, ResourceLocation> assetIdentifiers = new LinkedHashMap<>();
    public Map<List<ItemOverride.Predicate>, BlockModel> unbakedAssets = new LinkedHashMap<>();
    private Map<String, Either<Material, String>> textureOverrideMap = new HashMap<>();
    private boolean isTexture = false;

    public BakedModel bakedModel = null;
    public CITOverrideList bakedSubModels = new CITOverrideList();

    @Override
    public Set<PropertyKey> typeProperties() {
        return Set.of(PropertyKey.of("model"), PropertyKey.of("texture"), PropertyKey.of("tile"));
    }

    @Override
    public void load(List<CITCondition> conditions, PropertyGroup properties, ResourceManager resourceManager) throws CITParsingException {
        for (CITCondition condition : conditions)
            if (condition instanceof ConditionItems conditionItems)
                items.addAll(Arrays.asList(conditionItems.items));

        if (this.items.isEmpty())
            try {
                ResourceLocation propertiesName = ResourceLocation.tryParse(properties.stripName());
                if (!BuiltInRegistries.ITEM.containsKey(propertiesName))
                    throw new Exception();
                Item item = BuiltInRegistries.ITEM.get(propertiesName);
                conditions.add(new ConditionItems(item));
                this.items.add(item);
            } catch (Exception ignored) {
                throw new CITParsingException("Not targeting any item type", properties, -1);
            }

        ResourceLocation assetIdentifier;
        PropertyValue modelProp = properties.getLastWithoutMetadata("citresewn", "model");
        boolean containsTexture = modelProp == null && !properties.get("citresewn", "texture", "tile").isEmpty();

        if (!containsTexture) {
            assetIdentifier = resolveAsset(properties.identifier, modelProp, "models", ".json", resourceManager);
            if (assetIdentifier != null)
                assetIdentifiers.put(null, assetIdentifier);
            else if (modelProp != null) {
                assetIdentifier = resolveAsset(properties.identifier, modelProp, "models", ".json", resourceManager);
                if (assetIdentifier != null)
                    assetIdentifiers.put(null, assetIdentifier);
            }
        }

        for (PropertyValue property : properties.get("citresewn", "model")) {
            ResourceLocation subIdentifier = resolveAsset(properties.identifier, property, "models", ".json", resourceManager);
            if (subIdentifier == null)
                throw new CITParsingException("Cannot resolve path", properties, property.position());

            String subItem = property.keyMetadata();
            ResourceLocation subItemIdentifier = fixDeprecatedSubItem(subItem, properties, property.position());
            assetIdentifiers.put(subItemIdentifier == null ? ResourceLocation.fromNamespaceAndPath("minecraft", "item/" + subItem) : subItemIdentifier, subIdentifier);
        }

        if (assetIdentifiers.size() == 0) { // attempt to load texture
            isTexture = true;
            PropertyValue textureProp = properties.getLastWithoutMetadata("citresewn", "texture", "tile");
            assetIdentifier = resolveAsset(properties.identifier, textureProp, "textures", ".png", resourceManager);
            if (assetIdentifier != null)
                assetIdentifiers.put(null, assetIdentifier);

            for (PropertyValue property : properties.get("citresewn", "texture", "tile")) {
                if (property.keyMetadata() == null)
                    continue;
                ResourceLocation subIdentifier = resolveAsset(properties.identifier, property, "textures", ".png", resourceManager);
                if (subIdentifier == null)
                    throw new CITParsingException("Cannot resolve path", properties, property.position());

                String subItem = property.keyMetadata();
                ResourceLocation subItemIdentifier = fixDeprecatedSubItem(subItem, properties, property.position());
                assetIdentifiers.put(subItemIdentifier == null ? ResourceLocation.fromNamespaceAndPath("minecraft", "item/" + subItem) : subItemIdentifier, subIdentifier);
            }
        } else { // attempt to load textureOverrideMap from textures
            PropertyValue textureProp = properties.getLastWithoutMetadata("citresewn", "texture", "tile");
            if (textureProp != null) {
                assetIdentifier = resolveAsset(properties.identifier, textureProp, "textures", ".png", resourceManager);
                if (assetIdentifier != null)
                    textureOverrideMap.put(null, Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, assetIdentifier)));
                else
                    throw new CITParsingException("Cannot resolve path", properties, textureProp.position());
            }

            for (PropertyValue property : properties.get("citresewn", "texture", "tile")) {
                textureProp = property;
                ResourceLocation subIdentifier = resolveAsset(properties.identifier, textureProp, "textures", ".png", resourceManager);
                if (subIdentifier == null)
                    throw new CITParsingException("Cannot resolve path", properties, property.position());

                textureOverrideMap.put(property.keyMetadata(), Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, subIdentifier)));
            }
        }

        if (assetIdentifiers.size() == 0)
            throw new CITParsingException("Could not resolve a replacement model/texture", properties, -1);
    }

    public void loadUnbakedAssets(ResourceManager resourceManager) throws Exception {
        try {
            if (isTexture) {
                BlockModel itemJson = getModelForFirstItemType(resourceManager);
                if (((JsonUnbakedModelAccessor) itemJson).getTextureMap().size() > 1) { // use(some/all of) the asset identifiers to build texture override in layered models
                    textureOverrideMap = ((JsonUnbakedModelAccessor) itemJson).getTextureMap();
                    ResourceLocation defaultAsset = assetIdentifiers.get(null);
                    textureOverrideMap.replaceAll((layerName, originalTextureEither) -> {
                        ResourceLocation textureIdentifier = assetIdentifiers.remove(originalTextureEither.map(Material::texture, ResourceLocation::tryParse));
                        if (textureIdentifier != null)
                            return Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, textureIdentifier));
                        if (defaultAsset != null)
                            return Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, defaultAsset));
                        return null;
                    });

                    if (assetIdentifiers.size() == 0 || (assetIdentifiers.size() == 1 && assetIdentifiers.containsKey(null))) {
                        unbakedAssets.put(null, itemJson);
                        return;
                    }
                }

                ResourceLocation baseIdentifier = assetIdentifiers.remove(null);

                if (baseIdentifier != null) {
                    unbakedAssets.put(null, loadUnbakedAsset(resourceManager, baseIdentifier, null));

                    if (this.items.contains(Items.GOAT_HORN) && !assetIdentifiers.containsKey(ResourceLocation.fromNamespaceAndPath("minecraft", "item/tooting_goat_horn"))) // HOTFIX: TOOTING GOAT HORN WITH TEXTURE ASSET
                        assetIdentifiers.put(ResourceLocation.fromNamespaceAndPath("minecraft", "item/tooting_goat_horn"), baseIdentifier);
                }

                if (!assetIdentifiers.isEmpty()) { // contains sub models
                    LinkedHashMap<ResourceLocation, List<ItemOverride.Predicate>> overrideConditions = new LinkedHashMap<>();
                    for (Item item : this.items) {
                        ResourceLocation itemIdentifier = BuiltInRegistries.ITEM.getKey(item);
                        overrideConditions.put(ResourceLocation.fromNamespaceAndPath(itemIdentifier.getNamespace(), "item/" + itemIdentifier.getPath()), Collections.emptyList());

                        ResourceLocation itemModelIdentifier = ResourceLocation.fromNamespaceAndPath(itemIdentifier.getNamespace(), "models/item/" + itemIdentifier.getPath() + ".json");
                        try (Reader resourceReader = new InputStreamReader(resourceManager.getResource(itemModelIdentifier).orElseThrow().open())) {
                            BlockModel itemModelJson = BlockModel.fromStream(resourceReader);

                            if (itemModelJson.getOverrides() != null && !itemModelJson.getOverrides().isEmpty())
                                for (ItemOverride override : itemModelJson.getOverrides())
                                    overrideConditions.put(override.getModel(), override.getPredicates().toList());
                        }
                    }

                    ArrayList<ResourceLocation> overrideModels = new ArrayList<>(overrideConditions.keySet());
                    Collections.reverse(overrideModels);

                    for (ResourceLocation overrideModel : overrideModels) {
                        ResourceLocation replacement = assetIdentifiers.remove(overrideModel);
                        if (replacement == null)
                            continue;

                        List<ItemOverride.Predicate> conditions = overrideConditions.get(overrideModel);

                        if (overrideModel != null) {
                            GENERATED_SUB_CITS_SEEN.add(replacement);
                            replacement = ResourceLocation.fromNamespaceAndPath(replacement.getNamespace(), GENERATED_SUB_CITS_PREFIX + GENERATED_SUB_CITS_SEEN.size() + "_" + replacement.getPath());
                        }

                        unbakedAssets.put(conditions, loadUnbakedAsset(resourceManager, replacement, overrideModel));
                    }
                }
            } else { // isModel
                ResourceLocation baseIdentifier = assetIdentifiers.remove(null);

                if (baseIdentifier != null) {
                    if (!GENERATED_SUB_CITS_SEEN.add(baseIdentifier)) // cit generated duplicate
                        baseIdentifier = ResourceLocation.fromNamespaceAndPath(baseIdentifier.getNamespace(), GENERATED_SUB_CITS_PREFIX + GENERATED_SUB_CITS_SEEN.size() + "_" + baseIdentifier.getPath());
                    GENERATED_SUB_CITS_SEEN.add(baseIdentifier);

                    BlockModel model = loadUnbakedAsset(resourceManager, baseIdentifier, null);
                    unbakedAssets.put(null, model);

                    if (model.getOverrides().size() > 0 && textureOverrideMap.size() > 0) {
                        LinkedHashMap<ResourceLocation, List<ItemOverride.Predicate>> overrideConditions = new LinkedHashMap<>();

                        for (ItemOverride override : model.getOverrides())
                            overrideConditions.put(override.getModel(), override.getPredicates().toList());

                        ArrayList<ResourceLocation> overrideModels = new ArrayList<>(overrideConditions.keySet());
                        Collections.reverse(overrideModels);

                        for (ResourceLocation overrideModel : overrideModels) {
                            ResourceLocation replacement = resolveAsset(baseIdentifier, overrideModel.toString(), "models", ".json", resourceManager);
                            if (replacement != null) {
                                String subTexturePath = replacement.toString().substring(0, replacement.toString().lastIndexOf('.'));
                                final String subTextureName = subTexturePath.substring(subTexturePath.lastIndexOf('/') + 1);

                                replacement = baseIdentifier;
                                if (!GENERATED_SUB_CITS_SEEN.add(replacement)) // cit generated duplicate
                                    replacement = ResourceLocation.fromNamespaceAndPath(replacement.getNamespace(), GENERATED_SUB_CITS_PREFIX + GENERATED_SUB_CITS_SEEN.size() + "_" + replacement.getPath());
                                GENERATED_SUB_CITS_SEEN.add(replacement);

                                BlockModel jsonModel = loadUnbakedAsset(resourceManager, replacement, null);
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
                        overrideConditions.put(ResourceLocation.fromNamespaceAndPath(itemIdentifier.getNamespace(), "item/" + itemIdentifier.getPath()), Collections.emptyList());

                        ResourceLocation itemModelIdentifier = ResourceLocation.fromNamespaceAndPath(itemIdentifier.getNamespace(), "models/item/" + itemIdentifier.getPath() + ".json");
                        try (Reader resourceReader = new InputStreamReader( resourceManager.getResource(itemModelIdentifier).orElseThrow().open())) {
                            BlockModel itemModelJson = BlockModel.fromStream(resourceReader);

                            if (itemModelJson.getOverrides() != null && !itemModelJson.getOverrides().isEmpty())
                                for (ItemOverride override : itemModelJson.getOverrides())
                                    overrideConditions.put(override.getModel(), override.getPredicates().toList());
                        }
                    }

                    ArrayList<ResourceLocation> overrideModels = new ArrayList<>(overrideConditions.keySet());
                    Collections.reverse(overrideModels);

                    for (ResourceLocation overrideModel : overrideModels) {
                        ResourceLocation replacement = assetIdentifiers.remove(overrideModel);
                        if (replacement == null)
                            continue;

                        if (!GENERATED_SUB_CITS_SEEN.add(replacement)) // cit generated duplicate
                            replacement = ResourceLocation.fromNamespaceAndPath(replacement.getNamespace(), GENERATED_SUB_CITS_PREFIX + GENERATED_SUB_CITS_SEEN.size() + "_" + replacement.getPath());
                        GENERATED_SUB_CITS_SEEN.add(replacement);

                        List<ItemOverride.Predicate> conditions = overrideConditions.get(overrideModel);
                        unbakedAssets.put(conditions, loadUnbakedAsset(resourceManager, replacement, null));
                    }
                }
            }
        } finally {
            assetIdentifiers = null;
            textureOverrideMap = null;
        }
    }

    private BlockModel loadUnbakedAsset(ResourceManager resourceManager, ResourceLocation assetIdentifier, ResourceLocation overrideModel) throws Exception {
        final ResourceLocation identifier;
        {
            ResourceLocation possibleIdentifier = assetIdentifier;
            while (possibleIdentifier.getPath().startsWith(GENERATED_SUB_CITS_PREFIX))
                possibleIdentifier = ResourceLocation.fromNamespaceAndPath(possibleIdentifier.getNamespace(), possibleIdentifier.getPath().substring(possibleIdentifier.getPath().substring(GENERATED_SUB_CITS_PREFIX.length()).indexOf('_') + GENERATED_SUB_CITS_PREFIX.length() + 1));
            identifier = possibleIdentifier;
        }
        BlockModel json;
        if (identifier.getPath().endsWith(".json")) {
            try (InputStream is = resourceManager.getResource(identifier).orElseThrow().open()) {
                json = BlockModel.fromString(IOUtils.toString(is, StandardCharsets.UTF_8));
                json.name = assetIdentifier.toString();
                json.name = json.name.substring(0, json.name.length() - 5);

                ((JsonUnbakedModelAccessor) json).getTextureMap().replaceAll((layer, original) -> {
                    Optional<Material> left = original.left();
                    if (left.isPresent()) {
                        ResourceLocation resolvedIdentifier = resolveAsset(identifier, left.get().texture().getPath(), "textures", ".png", resourceManager);
                        if (resolvedIdentifier != null)
                            return Either.left(new Material(left.get().atlasLocation(), resolvedIdentifier));
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
                        parentId = resolveAsset(identifier, parentId.getPath(), "models", ".json", resourceManager);
                        if (parentId != null)
                            ((JsonUnbakedModelAccessor) json).setParentLocation(ResewnItemModelIdentifier.pack(parentId));
                    }
                }

                json.getOverrides().replaceAll(override -> {
                    String[] modelIdPathSplit = override.getModel().getPath().split("/");
                    if (override.getModel().getPath().startsWith("./") || (modelIdPathSplit.length > 2 && modelIdPathSplit[1].equals("cit"))) {
                        ResourceLocation resolvedOverridePath = resolveAsset(identifier, override.getModel().getPath(), "models", ".json", resourceManager);
                        if (resolvedOverridePath != null)
                            return new ItemOverride(ResewnItemModelIdentifier.pack(resolvedOverridePath), override.getPredicates().collect(Collectors.toList()));
                    }

                    return override;
                });

                return json;
            }
        } else if (identifier.getPath().endsWith(".png")) {
            json = overrideModel == null ? getModelForFirstItemType(resourceManager) : getModelFromOverrideModel(resourceManager, overrideModel);
            if (json == null)
                json = new BlockModel(ResourceLocation.fromNamespaceAndPath("minecraft", "item/generated"), new ArrayList<>(), ImmutableMap.of("layer0", Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, identifier))), true, BlockModel.GuiLight.FRONT, ItemTransforms.NO_TRANSFORMS, new ArrayList<>());
            json.getOverrides().clear();
            json.name = assetIdentifier.toString();
            json.name = json.name.substring(0, json.name.length() - 4);

            ((JsonUnbakedModelAccessor) json).getTextureMap().replaceAll((layerName, originalTextureEither) -> {
                if (!textureOverrideMap.isEmpty()) {
                    Either<Material, String> textureOverride = textureOverrideMap.get(layerName);
                    if (textureOverride == null)
                        textureOverride = textureOverrideMap.get(null);
                    return textureOverride == null ? originalTextureEither : textureOverride;
                } else
                    return Either.left(new Material(TextureAtlas.LOCATION_BLOCKS, identifier));
            });
            return json;
        }

        throw new Exception("Unknown asset type");
    }

    public ResourceLocation fixDeprecatedSubItem(String subItem, PropertyGroup properties, int position) {
        if (subItem == null)
            return null;
        String replacement = switch (subItem) {
            case "bow_standby" -> "bow";
            case "crossbow_standby" -> "crossbow";
            case "potion_bottle_drinkable" -> "potion";
            case "potion_bottle_splash" -> "splash_potion";
            case "potion_bottle_lingering" -> "lingering_potion";


            default -> null;
        };

        if (replacement != null) {
            CITResewn.logWarnLoading(properties.messageWithDescriptorOf("Warning: Using deprecated sub item id \"" + subItem + "\" instead of \"" + replacement + "\"", position));

            return ResourceLocation.fromNamespaceAndPath("minecraft", "item/" + replacement);
        }

        return null;
    }

    private BlockModel getModelForFirstItemType(ResourceManager resourceManager) {
        ResourceLocation firstItemIdentifier = BuiltInRegistries.ITEM.getKey(this.items.iterator().next()), firstItemModelIdentifier = ResourceLocation.fromNamespaceAndPath(firstItemIdentifier.getNamespace(), "models/item/" + firstItemIdentifier.getPath() + ".json");
        try (InputStream is = resourceManager.getResource(firstItemModelIdentifier).orElseThrow().open()) {
            BlockModel json = BlockModel.fromString(IOUtils.toString(is, StandardCharsets.UTF_8));

            if (((JsonUnbakedModelAccessor) json).getParentLocation().equals(ResourceLocation.fromNamespaceAndPath("minecraft", "item/template_spawn_egg"))) { // HOTFIX: Fixes not being able to change spawn eggs using texture cits
                try (InputStream parentInputStream = resourceManager.getResource(ResourceLocation.fromNamespaceAndPath("minecraft", "models/item/template_spawn_egg.json")).orElseThrow().open()) {
                    json = BlockModel.fromString(IOUtils.toString(parentInputStream, StandardCharsets.UTF_8));
                    ((JsonUnbakedModelAccessor) json).getTextureMap().remove("layer1"); // PARITY
                }
            }

            if (!GENERATED_SUB_CITS_SEEN.add(firstItemModelIdentifier)) // cit generated duplicate
                firstItemModelIdentifier = ResourceLocation.fromNamespaceAndPath(firstItemModelIdentifier.getNamespace(), GENERATED_SUB_CITS_PREFIX + GENERATED_SUB_CITS_SEEN.size() + "_" + firstItemModelIdentifier.getPath());
            GENERATED_SUB_CITS_SEEN.add(firstItemModelIdentifier);

            json.name = firstItemModelIdentifier.toString();
            json.name = json.name.substring(0, json.name.length() - 5);
            return json;
        } catch (Exception e) {
            return null;
        }
    }

    private BlockModel getModelFromOverrideModel(ResourceManager resourceManager, ResourceLocation overrideModel) {
        ResourceLocation modelIdentifier = ResourceLocation.fromNamespaceAndPath(overrideModel.getNamespace(), "models/" + overrideModel.getPath() + ".json");
        try (InputStream is = resourceManager.getResource(modelIdentifier).orElseThrow().open()) {
            BlockModel json = BlockModel.fromString(IOUtils.toString(is, StandardCharsets.UTF_8));

            if (!GENERATED_SUB_CITS_SEEN.add(modelIdentifier)) // cit generated duplicate
                modelIdentifier = ResourceLocation.fromNamespaceAndPath(modelIdentifier.getNamespace(), GENERATED_SUB_CITS_PREFIX + GENERATED_SUB_CITS_SEEN.size() + "_" + modelIdentifier.getPath());
            GENERATED_SUB_CITS_SEEN.add(modelIdentifier);

            json.name = modelIdentifier.toString();
            json.name = json.name.substring(0, json.name.length() - 5);
            return json;
        } catch (Exception e) {
            return null;
        }
    }

    public BakedModel getItemModel(CITContext context, int seed) {
        ClientLevel world = context.world instanceof ClientLevel clientWorld ? clientWorld : null;
        // get sub items or bakedModel if no sub item matches @Nullable
        BakedModel bakedModel = bakedSubModels.resolve(this.bakedModel, context.stack, world, context.entity, seed);

        // apply model overrides
        if (bakedModel != null && bakedModel.getOverrides() != null)
            bakedModel = bakedModel.getOverrides().resolve(bakedModel, context.stack, world, context.entity, seed);

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

    public static class Container extends CITTypeContainer<TypeItem> {
        public Container() {
            super(TypeItem.class, TypeItem::new, "item");
        }

        public Set<CIT<TypeItem>> loaded = new HashSet<>();
        public Map<Item, Set<CIT<TypeItem>>> loadedTyped = new IdentityHashMap<>();

        @Override
        public void load(List<CIT<TypeItem>> parsedCITs) {
            loaded.addAll(parsedCITs);
            for (CIT<TypeItem> cit : parsedCITs)
                for (CITCondition condition : cit.conditions)
                    if (condition instanceof ConditionItems items)
                        for (Item item : items.items)
                            if (item != null)
                                loadedTyped.computeIfAbsent(item, i -> new LinkedHashSet<>()).add(cit);
        }

        @Override
        public void dispose() {
            loaded.clear();
            loadedTyped.clear();
        }

        public CIT<TypeItem> getCIT(CITContext context, int seed) {
            return ((CITCacheItem) (Object) context.stack).citresewn$getCacheTypeItem().get(context).get();
        }

        public CIT<TypeItem> getRealTimeCIT(CITContext context) {
            Set<CIT<TypeItem>> loadedForItemType = loadedTyped.get(context.stack.getItem());
            if (loadedForItemType != null)
                for (CIT<TypeItem> cit : loadedForItemType)
                    if (cit.test(context))
                        return cit;

            return null;
        }
    }

    public interface CITCacheItem {
        CITCache.Single<TypeItem> citresewn$getCacheTypeItem();
    }

    public interface BakedModelManagerMixinAccess {
        void citresewn$forceMojankModel(BakedModel model);
    }
}