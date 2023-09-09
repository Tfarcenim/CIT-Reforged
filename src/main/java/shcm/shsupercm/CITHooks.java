package shcm.shsupercm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import shcm.shsupercm.fabric.citresewn.ActiveCITs;
import shcm.shsupercm.fabric.citresewn.CITResewn;
import shcm.shsupercm.fabric.citresewn.config.CITResewnConfig;
import shcm.shsupercm.fabric.citresewn.pack.CITPack;
import shcm.shsupercm.fabric.citresewn.pack.CITParser;
import shcm.shsupercm.fabric.citresewn.pack.cits.CIT;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static shcm.shsupercm.fabric.citresewn.CITResewn.info;

public class CITHooks {

    public static void initCITS(ResourceLocation eventId) {
        if (eventId != ModelBakery.MISSING_MODEL_LOCATION) return;
        if (CITResewn.INSTANCE.activeCITs != null) {
            info("Clearing active CITs..");
            CITResewn.INSTANCE.activeCITs.dispose();
            CITResewn.INSTANCE.activeCITs = null;
        }

        if (!CITResewnConfig.INSTANCE().enabled)
            return;

        info("Parsing CITs...");
        List<CITPack> parsedPacks = CITParser.parseCITs(Minecraft.getInstance().getResourceManager().listPacks().collect(Collectors.toCollection(ArrayList::new)));
        List<CIT> parsed = parsedPacks.stream().flatMap(pack -> pack.cits.stream()).collect(Collectors.toCollection(ArrayList::new));

        if (parsed.size() > 0) {
            info("Activating CITs...");
            CITResewn.INSTANCE.activeCITs = new ActiveCITs(parsedPacks, parsed);
        } else
            info("No cit packs found.");
    }

}
