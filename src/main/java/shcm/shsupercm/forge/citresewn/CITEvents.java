package shcm.shsupercm.forge.citresewn;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shcm.shsupercm.forge.citresewn.pack.cits.CITEnchantment;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class CITEvents {
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            CITEnchantment.MergeMethod.ticks++;
        }
    }
}
