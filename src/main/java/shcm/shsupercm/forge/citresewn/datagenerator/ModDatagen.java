package shcm.shsupercm.forge.citresewn.datagenerator;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

public class ModDatagen {

    public static void gather(GatherDataEvent e) {
        DataGenerator dataGenerator = e.getGenerator();
        PackOutput packOutput = dataGenerator.getPackOutput();
        ExistingFileHelper existingFileHelper = e.getExistingFileHelper();
        dataGenerator.addProvider(e.includeClient(),new ModSpriteSourceProvider(packOutput,existingFileHelper));
    }
}
