package shcm.shsupercm.fabric.citresewn.mixin.broken_paths;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;

/* if (CITResewnConfig.read().broken_paths) */ @Mixin(AbstractPackResources.class)
public abstract class AbstractFileResourcePackMixin implements PackResources {
    @Shadow @Final protected File base;

    @SuppressWarnings({"unchecked"})
    @Inject(method = "parseMetadata(Lnet/minecraft/resource/metadata/ResourceMetadataReader;)Ljava/lang/Object;", cancellable = true, at = @At("RETURN"))
    public <T extends PackMetadataSection> void parseMetadata(MetadataSectionSerializer<T> metaReader, CallbackInfoReturnable<T> cir) {
        if (cir.getReturnValue() != null)
            try {
                if (this.getClass().equals(FilePackResources.class)) {
                    try (ZipFile zipFile = new ZipFile(base)) {
                        zipFile.stream()
                                .forEach(entry -> {
                                    if (entry.getName().startsWith("assets"))
                                        new ResourceLocation("minecraft", entry.getName());
                                });
                    }
                } else if (this.getClass().equals(FolderPackResources.class)) {
                    final Path assets = new File(base, "assets").toPath();
                    Files.walk(assets)
                            .forEach(path -> new ResourceLocation("minecraft", assets.relativize(path).toString().replace('\\', '/')));
                }
            } catch (ResourceLocationException e) {
                cir.setReturnValue((T) new PackMetadataSection(cir.getReturnValue().getDescription(), Integer.MAX_VALUE - 53));
            } catch (Exception ignored) {}
    }
}
