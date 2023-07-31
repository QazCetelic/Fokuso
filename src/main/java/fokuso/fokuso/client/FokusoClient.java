package fokuso.fokuso.client;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@Environment(EnvType.CLIENT)
public class FokusoClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(FokusoClient.class);

    @Override
    public void onInitializeClient() {
        ChatFilterSystem.reloadFilterGroups();
        
        //CommandDispatcher<FabricClientCommandSource> commandDispatcher = ClientCommandManager.getActiveDispatcher();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("chatfilters")
            .then(literal("toggle").then(argument("list", StringArgumentType.word())
                .suggests((context, builder) -> getFilterListSuggestions(builder))
                .then(argument("enabled", BoolArgumentType.bool())
                .executes(context -> {
                    String listName = StringArgumentType.getString(context, "list");
                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                    Optional<ChatFilterGroup> group = ChatFilterSystem.getFilterGroup(listName);
                    if (group.isEmpty()) {
                        String error = "Unable to find " + listName + " filter group";
                        context.getSource().sendError(Text.of(error));
                        return -1;
                    }
                    else {
                        group.get().setEnabled(enabled);
                        String feedback = "Filters " + listName + " are now " + (enabled ? "enabled" : "disabled");
                        context.getSource().sendFeedback(Text.of(feedback));
                        return 1;
                    }
                }))
            ))
            .then(literal("reload").executes(context -> {
                ChatFilterSystem.reloadFilterGroups();
                context.getSource().sendFeedback(Text.of(ChatFilterSystem.getGroups().size() + " filters loaded!"));
                return 1;
            }))
            .then(literal("list").executes(context -> {
                List<ChatFilterGroup> groups = ChatFilterSystem.getGroups();
                String groupsString = groups.stream().map(ChatFilterGroup::toString).collect(Collectors.joining(", "));
                context.getSource().sendFeedback(Text.of(groupsString));
                return 1;
            })));
        });

    }
    
    private CompletableFuture<Suggestions> getFilterListSuggestions(SuggestionsBuilder builder) {
        ChatFilterSystem
                .getGroups()
                .stream()
                .map(ChatFilterGroup::getName)
                .forEach(builder::suggest);
        
        return builder.buildFuture();
    }
}
