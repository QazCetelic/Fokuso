package fokuso.fokuso.client;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

// TODO consider using Predicate<Text> instead of custom class
public abstract class ChatFilter {
    private boolean enabled = true;
    
    static boolean isOwnMessage(Text text) {
        return text.getString().startsWith("<" + MinecraftClient.getInstance().player.getName().getString() + "> ");
    }
    
    abstract public boolean ignoreOwnMessages();
    
    abstract public boolean filter(Text text);
    
    public boolean getEnabled() {
        return enabled;
    }
    
    public boolean setEnabled(boolean enabled) {
        boolean oldEnabled = this.enabled;
        this.enabled = enabled;
        return oldEnabled != enabled;
    }
    
    /**
     * Registers a command to toggle the filter.
     *
     * @param filter     The filter to register the command for.
     * @param filterName The name of the filter.
     */
    public static void registerCommand(String filterName, ChatFilter filter) {
        String commandName = "toggle-" + filterName + "-filter";
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
            ClientCommandManager.literal(commandName).executes(context -> {
            filter.setEnabled(!filter.getEnabled());
            context.getSource().sendFeedback(Text.translatable("Filter " + filterName + " is now " + (filter.getEnabled() ? "enabled" : "disabled")));
            return 0;
        })));
    }
}
