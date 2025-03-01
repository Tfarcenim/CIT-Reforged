package schm.shsupercm.citresewn.mixin;

import schm.shsupercm.citresewn.CITResewn;
import schm.shsupercm.citresewn.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;


/**
 * Opens the config screen when running the "/citresewn config" command.
 * @see CITResewn#openConfig
 */
@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    /**
     * If {@link CITResewn#openConfig} is true, changes the screen that's opened when the chat is closed to the config screen.
     * @see CITResewn#openConfig
     */
    @ModifyArg(method = "keyPressed", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V"))
    public Screen citresewn$redirectConfigScreen(Screen original) {
        if (CITResewn.openConfig) {
            CITResewn.openConfig = false;
            return Services.PLATFORM.isModLoaded("cloth-config2") ?
                    Services.PLATFORM.create(null) :
                    new AlertScreen(() -> Minecraft.getInstance().setScreen(null), Component.nullToEmpty("CIT Resewn"), Component.nullToEmpty("CIT Resewn requires Cloth Config to be able to show the config."));
        }

        return original;
    }
}
