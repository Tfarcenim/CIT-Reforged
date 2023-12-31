package shcm.shsupercm.forge.citresewn.mixin.core;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import shcm.shsupercm.forge.citresewn.pack.ResewnItemModelIdentifier;

@Mixin(TextureAtlas.class)
public class SpriteAtlasTextureMixin {
    @Inject(method = "getSprite", cancellable = true, at = @At("HEAD"))
    public void forceLiteralResewnTextureIdentifier(ResourceLocation id, CallbackInfoReturnable<ResourceLocation> cir) {
        if (id instanceof ResewnItemModelIdentifier)
            cir.setReturnValue(id);
    }
}
