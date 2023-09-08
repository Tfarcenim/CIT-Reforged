package shcm.shsupercm.fabric.citresewn.mixin.citenchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static shcm.shsupercm.fabric.citresewn.pack.cits.CITEnchantment.MergeMethod.ticks;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        ticks++;
    }
}
