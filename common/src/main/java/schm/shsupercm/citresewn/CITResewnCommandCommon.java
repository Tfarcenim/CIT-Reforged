package schm.shsupercm.citresewn;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import schm.shsupercm.citresewn.cit.*;
import schm.shsupercm.citresewn.pack.format.PropertyKey;
import schm.shsupercm.citresewn.pack.format.PropertyValue;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CITResewnCommandCommon {


    public static int analyze(CommandContext<CommandSourceStack> context) { //citresewn analyze <pack>
        final String pack = context.getArgument("pack", String.class);
        CommandSourceStack source = context.getSource();
        if (ActiveCITs.isActive()) {
            source.sendSystemMessage(Component.nullToEmpty("Analyzed CIT data of \"" + pack + "\u00a7r\":"));

            List<Component> builder = new ArrayList<>();

            for (Map.Entry<PropertyKey, Set<PropertyValue>> entry : ActiveCITs.getActive().globalProperties.properties.entrySet())
                for (PropertyValue value : entry.getValue())
                    if (value.packName().equals(pack))
                        builder.add(Component.nullToEmpty("  " + entry.getKey().toString() + (value.keyMetadata() == null ? "" : "." + value.keyMetadata()) + " = " + value.value()));
            if (!builder.isEmpty()) {
                source.sendSystemMessage(Component.nullToEmpty(" Global Properties:"));
                for (Component text : builder)
                    source.sendSystemMessage(text);

                builder.clear();
            }

            for (Map.Entry<Class<? extends CITType>, List<CIT<?>>> entry : ActiveCITs.getActive().cits.entrySet())
                if (!entry.getValue().isEmpty()) {
                    long count = entry.getValue().stream().filter(cit -> cit.packName.equals(pack)).count();
                    if (count > 0)
                        builder.add(Component.nullToEmpty("  " + CITRegistry.idOfType(entry.getKey()).toString() + " = " + count));
                }
            if (!builder.isEmpty()) {
                source.sendSystemMessage(Component.nullToEmpty(" Types:"));
                for (Component text : builder)
                    source.sendSystemMessage(text);

                builder.clear();
            }

            List<CITCondition> conditions = ActiveCITs.getActive().cits.values().stream()
                    .flatMap(Collection::stream)
                    .filter(cit -> cit.packName.equals(pack))
                    .flatMap(cit -> Arrays.stream(cit.conditions))
                    .toList();
            if (!conditions.isEmpty())
                source.sendSystemMessage(Component.nullToEmpty(" Utilizing " + conditions.size() + " conditions(" + conditions.stream().map(Object::getClass).distinct().count() + " unique condition types)"));
        } else
            source.sendSystemMessage(Component.nullToEmpty("Not active"));

        return 1;
    }

    /**
     * Greedy string argument that is limited to cit pack names loaded in {@link ActiveCITs}.
     */
    public static class LoadedCITPackArgument implements ArgumentType<String> {
        @Override
        public String parse(StringReader reader) throws CommandSyntaxException {
            StringBuilder builder = new StringBuilder();
            while (reader.canRead())
                builder.append(reader.read());

            String pack = builder.toString().trim();

            if (!getPacks().contains(pack)) {
                LiteralMessage message = new LiteralMessage("Could not find CIT pack");
                throw new CommandSyntaxException(new SimpleCommandExceptionType(message), message);
            }

            return pack;
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            return CompletableFuture.supplyAsync(() -> {
                for (String pack : getPacks()) {
                    builder.suggest(pack);
                }
                return builder.build();
            });
        }

        private static Set<String> getPacks() {
            if (ActiveCITs.isActive())
                return ActiveCITs.getActive().cits.values().stream()
                        .flatMap(Collection::stream)
                        .map(cit -> cit.packName)
                        .collect(Collectors.toSet());
            else
                return Collections.emptySet();
        }
    }
}
