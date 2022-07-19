package fokuso.fokuso.client;

import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ChatFilterGroup extends ChatFilter {
    private final List<ChatFilter> filters;
    private final String name;
    private final boolean ignoreOwnMessages;
    
    public String getName() {
        return name;
    }
    
    public ChatFilterGroup(String name, List<ChatFilter> filters, boolean ignoreOwnMessage) {
        this.filters = new ArrayList<>(filters);
        this.name = name;
        this.ignoreOwnMessages = ignoreOwnMessage;
    }
    
    public List<ChatFilter> getFilters() {
        return filters;
    }
    
    @Override
    public boolean ignoreOwnMessages() {
        return ignoreOwnMessages;
    }
    
    @Override
    public boolean filter(Text text) {
        for (ChatFilter filter : filters) {
            if (filter.getEnabled() && filter.filter(text) && !ChatFilter.isOwnMessage(text)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean setEnabled(boolean enabled) {
        for (ChatFilter filter : filters) {
            filter.setEnabled(enabled);
        }
        return enabled;
    }
    
    public boolean isEnabled() {
        return filters.stream().allMatch(ChatFilter::getEnabled);
    }
    
    @Override
    public String toString() {
        return String.format("%s (%d\u2714/%d)", getName(), filters.stream().filter(ChatFilter::getEnabled).count(), filters.size());
    }
}
