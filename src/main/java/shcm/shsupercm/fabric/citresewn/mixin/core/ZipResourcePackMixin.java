package shcm.shsupercm.fabric.citresewn.mixin.core;

import com.google.common.collect.Lists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;

@Mixin(FilePackResources.class)
public abstract class ZipResourcePackMixin {
    @Shadow protected abstract ZipFile getOrCreateZipFile() throws IOException;

    @Inject(method = "getResources", cancellable = true, at = @At("HEAD"))
    public void fixDepthBug(PackType type, String namespace, String prefix, Predicate<String> pathFilter, CallbackInfoReturnable<Collection<ResourceLocation>> cir) {
        ZipFile zipFile2;
        try {
            zipFile2 = this.getOrCreateZipFile();
        } catch (IOException var15) {
            cir.setReturnValue(Collections.emptySet()); return;
        }

        Enumeration<? extends ZipEntry> enumeration = zipFile2.entries();
        List<ResourceLocation> list = Lists.newArrayList();
        String var10000 = type.getDirectory();
        String string = var10000 + "/" + namespace + "/";
        String string2 = string + prefix + "/";

        while(enumeration.hasMoreElements()) {
            ZipEntry zipEntry = enumeration.nextElement();
            if (!zipEntry.isDirectory()) {
                String string3 = zipEntry.getName();
                if (!string3.endsWith(".mcmeta") && string3.startsWith(string2)) {
                    String string4 = string3.substring(string.length());
                    String[] strings = string4.split("/");
                    if (pathFilter.test(strings[strings.length - 1])) {
                        list.add(new ResourceLocation(namespace, string4));
                    }
                }
            }
        }

        cir.setReturnValue(list); return;
    }
}
