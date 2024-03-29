package fokuso.fokuso.mixin;

import fokuso.fokuso.client.ChatFilterSystem;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    // "addMessage(Lnet/minecraft/text/Text;IIZ)V"
    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;)V", cancellable = true)
    //private void addMessage(Text message, int messageId, int timestamp, boolean refresh, CallbackInfo info) {
    private void addMessage(Text message, CallbackInfo ci) {
        if (ChatFilterSystem.filter(message)) {
            ci.cancel();
        }
    }
}
