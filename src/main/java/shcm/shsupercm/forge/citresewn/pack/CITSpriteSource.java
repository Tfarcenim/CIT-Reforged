package shcm.shsupercm.forge.citresewn.pack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import shcm.shsupercm.forge.citresewn.CITResewn;

import java.util.Map;

public class CITSpriteSource implements SpriteSource {

    public static final Codec<CITSpriteSource> CODEC = RecordCodecBuilder.create((sourceApp) -> sourceApp.group(Codec.STRING.fieldOf("root")
            .forGetter((spriteSource) -> spriteSource.root))
            .apply(sourceApp, CITSpriteSource::new));

    private final String root;

    public CITSpriteSource(String root) {
        this.root = root;
    }

    @Override
    public void run(ResourceManager resourceManager, Output regions) {
            FileToIdConverter resourceFinder = new FileToIdConverter(root + "/cit", ".png");
            for (Map.Entry<ResourceLocation, Resource> entry : resourceFinder.listMatchingResources(resourceManager).entrySet())
                regions.add(resourceFinder.fileToId(entry.getKey()).withPrefix(root + "/cit/"), entry.getValue());
    }

    @Override
    public SpriteSourceType type() {
        return CITResewn.CIT;
    }
}
