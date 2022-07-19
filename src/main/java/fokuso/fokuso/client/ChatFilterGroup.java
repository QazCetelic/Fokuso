package fokuso.fokuso.client;

import java.util.ArrayList;
import java.util.List;

public class ChatFilterGroup {
    private final List<ChatFilter> filters;
    private final String name;
    
    public String getName() {
        return name;
    }
    
    public ChatFilterGroup(String name, List<ChatFilter> filters) {
        this.filters = new ArrayList<>(filters);
        this.name = name;
    }
    
    public List<ChatFilter> getFilters() {
        return filters;
    }
    
    public void setEnabled(boolean enabled) {
        for (ChatFilter filter : filters) {
            filter.setEnabled(enabled);
        }
    }
    
    public boolean isEnabled() {
        return filters.stream().allMatch(ChatFilter::getEnabled);
    }
    
    @Override
    public String toString() {
        return String.format("%s (%d\u2714/%d)", getName(), filters.stream().filter(ChatFilter::getEnabled).count(), filters.size());
    }
}
