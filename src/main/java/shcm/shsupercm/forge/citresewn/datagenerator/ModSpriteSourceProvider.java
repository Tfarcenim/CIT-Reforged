package shcm.shsupercm.forge.citresewn.datagenerator;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;
import shcm.shsupercm.forge.citresewn.CITResewn;
import shcm.shsupercm.forge.citresewn.pack.CITParser;
import shcm.shsupercm.forge.citresewn.pack.CITSpriteSource;

public class ModSpriteSourceProvider extends SpriteSourceProvider {
    public ModSpriteSourceProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, fileHelper, CITResewn.MODID);
    }

    @Override
    protected void addSources() {
        for (String root : CITParser.ROOTS) {
            atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new CITSpriteSource(root));
        }
    }
}
