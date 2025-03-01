package shcm.shsupercm.fabric.citresewn.mixin.broken_paths;

import net.minecraft.ResourceLocationException;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shcm.shsupercm.fabric.citresewn.config.BrokenPaths;

/**
 * Adds a resourcepack compatibility error message when broken paths are enabled and are detected in a pack.
 * @see BrokenPaths
 * @see ResourcePackCompatibilityMixin
 */
@Mixin(AbstractPackResources.class)
public abstract class AbstractFileResourcePackMixin implements PackResources {

    @SuppressWarnings({"unchecked"})
    @Inject(method = "parseMetadata(Lnet/minecraft/resource/metadata/ResourceMetadataReader;)Ljava/lang/Object;", cancellable = true, at = @At("RETURN"))
    public <T extends PackMetadataSection> void citresewn$brokenpaths$parseMetadata(MetadataSectionSerializer<T> metaReader, CallbackInfoReturnable<T> cir) {
        if (cir.getReturnValue() != null) try {
            for (String namespace : getNamespaces(PackType.CLIENT_RESOURCES)) {
                listResources(PackType.CLIENT_RESOURCES, namespace, "", (identifier, inputStreamInputSupplier) -> {
                });
            }
        } catch (ResourceLocationException e) {
            cir.setReturnValue((T) new PackMetadataSection(cir.getReturnValue()./*? >=1.20.4 {*/description()/*?} else {*//*getDescription()*//*?}*/, Integer.MAX_VALUE - 53/*? >=1.20.4 {*/, cir.getReturnValue().supportedFormats()/*?}*/));
        } catch (Exception ignored) { }
    }
}
