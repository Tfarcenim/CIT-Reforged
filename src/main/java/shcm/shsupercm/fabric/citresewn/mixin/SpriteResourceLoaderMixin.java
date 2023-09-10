package shcm.shsupercm.fabric.citresewn.mixin;

import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import shcm.shsupercm.fabric.citresewn.pack.CITParser;

import java.util.List;
import java.util.Map;

@Mixin(SpriteResourceLoader.class)
public class SpriteResourceLoaderMixin {
    @Inject(method = "load", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private static void citresewn$atlasSource(ResourceManager resourceManager, ResourceLocation id,
                                              CallbackInfoReturnable<SpriteResourceLoader> cir, ResourceLocation identifier, List<SpriteSource> list) {
        if (id.getPath().equals("blocks") && id.getNamespace().equals("minecraft")) {
            list.add(new SpriteSource() {
                @Override
                public void run(ResourceManager resourceManager, Output regions) {
                    for (String root : CITParser.ROOTS) {
                        FileToIdConverter resourceFinder = new FileToIdConverter(root + "/cit", ".png");
                        for (Map.Entry<ResourceLocation, Resource> entry : resourceFinder.listMatchingResources(resourceManager).entrySet())
                            regions.add(resourceFinder.fileToId(entry.getKey()).withPrefix(root + "/cit/"), entry.getValue());
                    }
                }

                @Override
                public SpriteSourceType type() {
                    return null;
                }
            });
        }
    }
}
