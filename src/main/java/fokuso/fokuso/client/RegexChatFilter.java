package fokuso.fokuso.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class RegexChatFilter extends ChatFilter {
    // language=RegExp
    public static final String USERNAME_PREFIX_REGEX = "<[^>\s\n]{2,}> ";
    // using: https://help.minecraft.net/hc/en-us/articles/4408950195341-Minecraft-Java-Edition-Username-VS-Gamertag-FAQ
    // language=RegExp
    public static final String USERNAME_REGEX = "[a-zA-Z0-9_]{3,16}";
    
    public final Predicate<String> predicate;
    public final String regex;
    private final boolean ignoreOwnMessages;
    private final boolean stripFormattingCodes;
    
    /**
     * @param regex The regex to use. It's possible to use several special classes:
     *              <ul>
     *                  <li>p: Username prefix</li>
     *                  <li>u: Username</li>
     *              </ul>
     * @param ignoreOwnMessages Whether to ignore messages from the player itself.
     * @param stripFormattingCodes Whether to strip formatting codes from the message before regex match.
     */
    public RegexChatFilter(String regex, boolean ignoreOwnMessages, boolean stripFormattingCodes) {
        this.regex = regex
                .replace("\\p", USERNAME_PREFIX_REGEX)
                .replace("\\u", USERNAME_REGEX);
        predicate = Pattern.compile(this.regex).asPredicate();
        this.ignoreOwnMessages = ignoreOwnMessages;
        this.stripFormattingCodes = stripFormattingCodes;
    }
    
    /**
     * @param regex The regex to use, you can use the p character class to match a player prefix e.g. <QazCetelic>.
     */
    public RegexChatFilter(String regex) {
        this(regex, true, true);
    }
    
    @Override
    public boolean filter(Text text) {
        String string = text.getString();
        if (stripFormattingCodes) {
            string = string.replaceAll("[§?][0-9a-or]", "");
        }
        boolean matches = predicate.test(string);
        System.out.println("Regex '" + regex + "':'" + string + "' matches: " + matches);
        return matches;
    }
    
    @Override
    public boolean ignoreOwnMessages() {
        return ignoreOwnMessages;
    }
    
    @Override
    public String toString() {
        return "/" + regex + "/";
    }
    
    @Override
    public int hashCode() {
        return regex.hashCode();
    }
}
