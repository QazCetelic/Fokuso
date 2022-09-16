package fokuso.fokuso.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fokuso.fokuso.Fokuso;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.LiteralText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

@Environment(EnvType.CLIENT)
public class FokusoClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(FokusoClient.class);

    @Override
    public void onInitializeClient() {
        {
            List<ChatFilterGroup> groups = ChatFilterSystem.reloadFilterGroups();
            LOGGER.info("Loaded " + groups.size() + " chat filter groups.");
        }
        CommandDispatcher<FabricClientCommandSource> commandDispatcher = ClientCommandManager.DISPATCHER;
        commandDispatcher.register(literal("chatfilters")
            .then(literal("toggle").then(argument("list", StringArgumentType.word())
                .suggests((context, builder) -> getFilterListSuggestions(builder))
                .then(argument("enabled", BoolArgumentType.bool())
                .executes(context -> {
                    String listName = StringArgumentType.getString(context, "list");
                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                    ChatFilterGroup group = ChatFilterSystem.getFilterGroupOrNull(listName);
                    if (group == null) {
                        String error = "Unable to find " + listName + " filter group";
                        context.getSource().sendError(new LiteralText(error));
                        return -1;
                    }
                    else {
                        group.setEnabled(enabled);
                        String feedback = "Filters " + listName + " are now " + (enabled ? "enabled" : "disabled");
                        context.getSource().sendFeedback(new LiteralText(feedback));
                        return 1;
                    }
                }))
            ))
            .then(literal("reload").executes(context -> {
                List<ChatFilterGroup> groups = ChatFilterSystem.reloadFilterGroups();
                context.getSource().sendFeedback(new LiteralText(groups.size() + " filters loaded!"));
                return 1;
            }))
            .then(literal("list").executes(context -> {
                List<ChatFilterGroup> groups = ChatFilterSystem.getGroups();
                String groupsString = groups.stream().map(ChatFilterGroup::toString).collect(Collectors.joining(", "));
                context.getSource().sendFeedback(new LiteralText(groupsString));
                return 1;
            }))
        );
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
