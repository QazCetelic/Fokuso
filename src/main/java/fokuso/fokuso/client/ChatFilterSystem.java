package fokuso.fokuso.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChatFilterSystem {
    public static List<ChatFilter> filters = new ArrayList<>();
    private static List<ChatFilterGroup> groups = null;
    
    public static List<ChatFilterGroup> getGroups() {
        if (groups == null) groups = loadChatFilterGroups();
        return groups;
    }
    
    /**
     * @param text The text to filter.
     * @return Whether the messages should be filtered out.
     */
    public static boolean filter(Text text) {
        for (ChatFilter filter : filters) {
            if (filter.getEnabled() && filter.filter(text) && !isOwnMessage(text)) {
                return true;
            }
        }
        return false;
    }
    
    public static List<ChatFilter> getFilters() {
        return filters;
    }
    
    public static void addFilterGroup(ChatFilterGroup group) {
        filters.addAll(group.getFilters());
    }
    
    public static ChatFilterGroup getFilterGroupOrNull(String name) {
        for (ChatFilterGroup group : getGroups()) {
            if (group.getName().equals(name)) {
                return group;
            }
        }
        return null;
    }
    
    private static boolean isOwnMessage(Text text) {
        return text.getString().startsWith("<" + MinecraftClient.getInstance().player.getName().getString() + "> ");
    }
    
    public static List<ChatFilterGroup> reloadFilterGroups() {
        groups = loadChatFilterGroups();
        return groups;
    }
    
    public static List<ChatFilterGroup> loadChatFilterGroups() {
        List<ChatFilterGroup> groups = new ArrayList<>();
        try {
            File config = new File(MinecraftClient.getInstance().runDirectory, "config");
            File fokuso = new File(config, "fokuso");
            if (fokuso.exists() && fokuso.isDirectory()) {
                System.out.println("Fokuso directory exists, loading configuration files...");
                for (File file : fokuso.listFiles()) {
                    List<ChatFilter> filters = new ArrayList<>();
    
                    Scanner scanner = new Scanner(file);
                    while (scanner.hasNext()) {
                        String line = scanner.nextLine();
                        // Allow comments and blank lines
                        if (line.startsWith("#") || line.matches("\\s*")) continue;
                        ChatFilter filter = new RegexChatFilter(line);
                        filters.add(filter);
                    }
                    
                    boolean isDisabled = file.getName().endsWith(".disabled");
                    String name = file.getName().replaceFirst("\\.\\w+$", "").replace(" ", "-");
                    ChatFilterGroup group = new ChatFilterGroup(name, filters);
                    if (isDisabled) {
                        group.setEnabled(false);
                    }
                    groups.add(group);
                    addFilterGroup(group);
                    System.out.println("Loading filters: " + file.getName());
                }
            }
            else {
                if (fokuso.exists()) {
                    System.out.println("Deleting Fokuso file");
                    fokuso.delete();
                }
                System.out.println("Creating Fokuso config directory");
                fokuso.mkdirs();
            }
        }
        catch (Exception e) {
            System.err.println("Failed to load filters:");
        }
        return groups;
    }
}
