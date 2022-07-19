package fokuso.fokuso.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.text.LiteralText;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

@Environment(EnvType.CLIENT)
public class FokusoClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        {
            List<ChatFilterGroup> groups = ChatFilterSystem.reloadFilterGroups();
            System.out.println(groups.size() + " filters loaded!");
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
        List<String> groupNames = ChatFilterSystem
                .getGroups()
                .stream()
                .map(ChatFilterGroup::getName)
                .toList();
        
        for (String s : groupNames) {
            builder.suggest(s);
        }
        
        return builder.buildFuture();
    }
}
