package schm.shsupercm.citresewn;


import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import schm.shsupercm.citresewn.cit.*;
import schm.shsupercm.citresewn.config.CITResewnConfig;
import schm.shsupercm.citresewn.platform.Services;

import java.util.*;

@Mod(value = CITResewn.MOD_ID, dist = Dist.CLIENT)
public class CITResewnNeoforge {

    public CITResewnNeoforge(IEventBus eventBus) {
        eventBus.addListener(this::commands);
        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        CITResewn.init();

    }

    void commands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CommandBuildContext buildContext = event.getBuildContext();
        dispatcher.register(
                Commands.literal(CITResewn.MOD_ID).executes(context -> {
                            context.getSource().sendSystemMessage(Component.nullToEmpty("CIT Resewn v" + Services.PLATFORM.getModVersion() + ":"));
                            context.getSource().sendSystemMessage(Component.nullToEmpty("  Registered: " + CITRegistry.TYPES.values().stream().distinct().count() + " types and " + CITRegistry.CONDITIONS.values().stream().distinct().count() + " conditions"));

                            final boolean active = CITResewnConfig.INSTANCE.enabled && ActiveCITs.isActive();
                            context.getSource().sendSystemMessage(Component.nullToEmpty("  Active: " + (active ? "yes" : ("no, " + (CITResewnConfig.INSTANCE.enabled ? "no cit packs loaded" : "disabled in config")))));
                            if (active) {
                                context.getSource().sendSystemMessage(Component.nullToEmpty("   Loaded: " + ActiveCITs.getActive().cits.values().stream().mapToLong(Collection::size).sum() + " CITs from " + ActiveCITs.getActive().cits.values().stream().flatMap(Collection::stream).map(cit -> cit.packName).distinct().count() + " resourcepacks"));
                            }
                            context.getSource().sendSystemMessage(Component.nullToEmpty(""));

                            return 1;
                        })
                        .then(Commands.literal("config")
                                .executes(context -> { //citresewn config
                                    CITResewn.openConfig = true;

                                    return 1;
                                }))
                        .then(Commands.literal("analyze")
                                .then(Commands.literal("pack")
                                        .then(Commands.argument("pack", new CITResewnCommandCommon.LoadedCITPackArgument())
                                                .executes(CITResewnCommandCommon::analyze)
                                        )
                                )
                        )
        );
    }

}