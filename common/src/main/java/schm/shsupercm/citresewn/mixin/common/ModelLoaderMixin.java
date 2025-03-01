package schm.shsupercm.citresewn.mixin.common;

import com.mojang.datafixers.util.Either;
import org.apache.commons.io.IOUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import schm.shsupercm.citresewn.cit.CITType;
import schm.shsupercm.citresewn.defaults.common.ResewnItemModelIdentifier;
import schm.shsupercm.citresewn.mixin.types.item.JsonUnbakedModelAccessor;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;

/**
 * Will be rewritten at some point.
 */
@Mixin(ModelBakery.class)
public class ModelLoaderMixin {
    @Inject(method = "loadBlockModel", cancellable = true, at = @At("HEAD"))
    public void citresewn$forceLiteralResewnModelIdentifier(ResourceLocation originalId, CallbackInfoReturnable<BlockModel> cir) {
        if (ResewnItemModelIdentifier.marked(originalId)) {
            final ResourceLocation id = ResewnItemModelIdentifier.unpack(originalId);
            try (InputStream is = Minecraft.getInstance().getResourceManager().getResource(id).orElseThrow().open()) {
                BlockModel json = BlockModel.fromString(IOUtils.toString(is, StandardCharsets.UTF_8));
                json.name = id.toString();
                json.name = json.name.substring(0, json.name.length() - 5);

                ((JsonUnbakedModelAccessor) json).getTextureMap().replaceAll((layer, original) -> {
                    Optional<Material> left = original.left();
                    if (left.isPresent()) {
                        String originalPath = left.get().texture().getPath();
                        String[] split = originalPath.split("/");
                        if (originalPath.startsWith("./") || (split.length > 2 && split[1].equals("cit"))) {
                            ResourceLocation resolvedIdentifier = CITType.resolveAsset(id, originalPath, "textures", ".png", Minecraft.getInstance().getResourceManager());
                            if (resolvedIdentifier != null)
                                return Either.left(new Material(left.get().atlasLocation(), resolvedIdentifier));
                        }
                    }
                    return original;
                });

                ResourceLocation parentId = ((JsonUnbakedModelAccessor) json).getParentLocation();
                if (parentId != null) {
                    String[] parentIdPathSplit = parentId.getPath().split("/");
                    if (parentId.getPath().startsWith("./") || (parentIdPathSplit.length > 2 && parentIdPathSplit[1].equals("cit"))) {
                        parentId = CITType.resolveAsset(id, parentId.getPath(), "models", ".json", Minecraft.getInstance().getResourceManager());
                        if (parentId != null)
                            ((JsonUnbakedModelAccessor) json).setParentLocation(ResewnItemModelIdentifier.pack(parentId));
                    }
                }

                json.getOverrides().replaceAll(override -> {
                    String[] modelIdPathSplit = override.getModel().getPath().split("/");
                    if (override.getModel().getPath().startsWith("./") || (modelIdPathSplit.length > 2 && modelIdPathSplit[1].equals("cit"))) {
                        ResourceLocation resolvedOverridePath = CITType.resolveAsset(id, override.getModel().getPath(), "models", ".json", Minecraft.getInstance().getResourceManager());
                        if (resolvedOverridePath != null)
                            return new ItemOverride(ResewnItemModelIdentifier.pack(resolvedOverridePath), override.getPredicates().collect(Collectors.toList()));
                    }

                    return override;
                });

                cir.setReturnValue(json);
            } catch (Exception ignored) {
            }
        }
    }
}
