package shcm.shsupercm.fabric.citresewn.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shcm.shsupercm.fabric.citresewn.pack.PackParser;

import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(SpriteSourceList.class)
public class AtlasLoaderMixin {
    @Shadow @Final private List<SpriteSource> sources;

    @Inject(method = "of", at = @At("RETURN"), cancellable = true)
    private static void citresewn$atlasSource(ResourceManager resourceManager, ResourceLocation id, CallbackInfoReturnable<SpriteSourceList> cir) {
        if (id.getPath().equals("blocks") && id.getNamespace().equals("minecraft")) {
            ((AtlasLoaderMixin) (Object) cir.getReturnValue()).sources.add(new SpriteSource() {
                @Override
                public void run(ResourceManager resourceManager, Output regions) {
                    for (String root : PackParser.ROOTS) {
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
