package fokuso.fokuso.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ChatFilterSystem {
    public static final String FILE_EXTENSION = ".filters";
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
            if (filter.getEnabled() && filter.filter(text) && !ChatFilter.isOwnMessage(text)) {
                FokusoClient.LOGGER.debug("Filtered message: '{}'", text.getString());
                return true;
            }
        }
        FokusoClient.LOGGER.debug("Message not filtered: '{}'", text.getString());
        return false;
    }
    
    public static List<ChatFilter> getFilters() {
        return filters;
    }
    
    public static ChatFilterGroup getFilterGroupOrNull(String name) {
        for (ChatFilterGroup group : getGroups()) {
            if (group.getName().equals(name)) {
                return group;
            }
        }
        return null;
    }
    
    public static List<ChatFilterGroup> reloadFilterGroups() {
        groups = loadChatFilterGroups();
        return groups;
    }
    
    public static List<ChatFilterGroup> loadChatFilterGroups() {
        FokusoClient.LOGGER.debug("Loading chat filter groups...");
        List<ChatFilterGroup> groups = new ArrayList<>();
        try {
            File config = new File(MinecraftClient.getInstance().runDirectory, "config");
            File fokuso = new File(config, "fokuso");
            if (fokuso.exists() && fokuso.isDirectory()) {
                FokusoClient.LOGGER.debug("Found fokuso config directory");
                
                List<File> filterLists = Files.walk(fokuso.toPath())
                                              .map(Path::toFile)
                                              .filter(File::isFile)
                                              .filter(f -> f.getName().endsWith(FILE_EXTENSION))
                                              .toList();
    
                FokusoClient.LOGGER.debug("Found " + filterLists.size() + " filter lists");
                
                for (File file : filterLists) {
                    List<ChatFilter> filters = new ArrayList<>();
    
                    FokusoClient.LOGGER.debug("Loading filter list from {}", file.getName());
                    Scanner scanner = new Scanner(file);
                    while (scanner.hasNext()) {
                        String line = scanner.nextLine();
                        FokusoClient.LOGGER.debug("Loading filter: '{}'", line);
                        // Allow comments and blank lines
                        if (line.startsWith("#") || line.matches("\\s*")) continue;
                        ChatFilter filter = new RegexChatFilter(line);
                        filters.add(filter);
                    }
                    
                    boolean isDisabled = file.getName().endsWith(".disabled");
                    String name = file.getName().replaceFirst("\\"+FILE_EXTENSION+"$", "").replaceAll("\s", "-");
                    ChatFilterGroup group = new ChatFilterGroup(name, filters, true);
                    if (isDisabled) {
                        group.setEnabled(false);
                    }
                    groups.add(group);
                }
            }
            else {
                if (fokuso.exists()) {
                    if (fokuso.delete()) {
                        FokusoClient.LOGGER.warn("Fokuso config directory was a file, and thus, deleted.");
                    }
                    else {
                        FokusoClient.LOGGER.error("Fokuso config directory was a file, but could not be deleted.");
                    }
                }
                if (fokuso.mkdirs()) {
                    FokusoClient.LOGGER.info("Created Fokuso config directory");
                }
                else {
                    FokusoClient.LOGGER.error("Could not create Fokuso directory");
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            FokusoClient.LOGGER.error("Failed to load filters");
        }
        return groups;
    }
}
