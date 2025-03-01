package shcm.shsupercm.fabric.citresewn;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.network.chat.Component;
import schm.shsupercm.citresewn.CITResewn;
import schm.shsupercm.citresewn.CITResewnCommandCommon;
import schm.shsupercm.citresewn.cit.*;
import schm.shsupercm.citresewn.config.CITResewnConfig;
import schm.shsupercm.citresewn.pack.format.PropertyKey;
import schm.shsupercm.citresewn.pack.format.PropertyValue;
import schm.shsupercm.citresewn.platform.Services;

import java.util.*;

/**
 * Logic for the /citresewn client command. Only enabled when Fabric API is present.<br>
 * Structure:
 * <pre>
 * /citresewn - General info command
 * /citresewn config - Opens the config gui(only when Cloth Config is present)
 * /citresewn analyze pack &lt;pack&gt; - Displays data for the given loaded cit pack.
 * </pre>
 */
public class CITResewnCommandFabric {

    /**
     * Registers all of CIT Resewn's commands.
     */
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal(CITResewn.MOD_ID).executes(context -> {
                    context.getSource().sendFeedback(Component.nullToEmpty("CIT Resewn v" + Services.PLATFORM.getModVersion() + ":"));
                    context.getSource().sendFeedback(Component.nullToEmpty("  Registered: " + CITRegistry.TYPES.values().stream().distinct().count() + " types and " + CITRegistry.CONDITIONS.values().stream().distinct().count() + " conditions"));

                    final boolean active = CITResewnConfig.INSTANCE.enabled && ActiveCITs.isActive();
                    context.getSource().sendFeedback(Component.nullToEmpty("  Active: " + (active ? "yes" : ("no, " + (CITResewnConfig.INSTANCE.enabled ? "no cit packs loaded" : "disabled in config")))));
                    if (active) {
                        context.getSource().sendFeedback(Component.nullToEmpty("   Loaded: " + ActiveCITs.getActive().cits.values().stream().mapToLong(Collection::size).sum() + " CITs from " + ActiveCITs.getActive().cits.values().stream().flatMap(Collection::stream).map(cit -> cit.packName).distinct().count() + " resourcepacks"));
                    }
                    context.getSource().sendFeedback(Component.nullToEmpty(""));

                    return 1;
                })
                .then(ClientCommandManager.literal("config")
                        .executes(context -> { //citresewn config
                            CITResewn.openConfig = true;

                            return 1;
                        }))
                .then(ClientCommandManager.literal("analyze")
                        .then(ClientCommandManager.literal("pack")
                                .then(ClientCommandManager.argument("pack", new CITResewnCommandCommon.LoadedCITPackArgument())
                                        .executes(context -> { //citresewn analyze <pack>
                                            final String pack = context.getArgument("pack", String.class);
                                            if (ActiveCITs.isActive()) {
                                                context.getSource().sendFeedback(Component.nullToEmpty("Analyzed CIT data of \"" + pack + "\u00a7r\":"));

                                                List<Component> builder = new ArrayList<>();

                                                for (Map.Entry<PropertyKey, Set<PropertyValue>> entry : ActiveCITs.getActive().globalProperties.properties.entrySet())
                                                    for (PropertyValue value : entry.getValue())
                                                        if (value.packName().equals(pack))
                                                            builder.add(Component.nullToEmpty("  " + entry.getKey().toString() + (value.keyMetadata() == null ? "" : "." + value.keyMetadata()) + " = " + value.value()));
                                                if (!builder.isEmpty()) {
                                                    context.getSource().sendFeedback(Component.nullToEmpty(" Global Properties:"));
                                                    for (Component text : builder)
                                                        context.getSource().sendFeedback(text);

                                                    builder.clear();
                                                }

                                                for (Map.Entry<Class<? extends CITType>, List<CIT<?>>> entry : ActiveCITs.getActive().cits.entrySet())
                                                    if (!entry.getValue().isEmpty()) {
                                                        long count = entry.getValue().stream().filter(cit -> cit.packName.equals(pack)).count();
                                                        if (count > 0)
                                                            builder.add(Component.nullToEmpty("  " + CITRegistry.idOfType(entry.getKey()).toString() + " = " + count));
                                                    }
                                                if (!builder.isEmpty()) {
                                                    context.getSource().sendFeedback(Component.nullToEmpty(" Types:"));
                                                    for (Component text : builder)
                                                        context.getSource().sendFeedback(text);

                                                    builder.clear();
                                                }

                                                List<CITCondition> conditions = ActiveCITs.getActive().cits.values().stream()
                                                        .flatMap(Collection::stream)
                                                        .filter(cit -> cit.packName.equals(pack))
                                                        .flatMap(cit -> Arrays.stream(cit.conditions))
                                                        .toList();
                                                if (!conditions.isEmpty())
                                                    context.getSource().sendFeedback(Component.nullToEmpty(" Utilizing " + conditions.size() + " conditions(" + conditions.stream().map(Object::getClass).distinct().count() + " unique condition types)"));
                                            } else
                                                context.getSource().sendFeedback(Component.nullToEmpty("Not active"));

                                            return 1;
                                        })
                                )
                        )
                  )
            );
        });
    }

}
