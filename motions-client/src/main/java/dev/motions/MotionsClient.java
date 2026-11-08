package dev.motions;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class MotionsClient implements ClientModInitializer {
    public static KeyBinding openKey;
    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of("motions", "main"));

    @Override
    public void onInitializeClient() {
        MotionsConfig.load();
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.motions.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new MotionsScreen());
            }
            AuctionManager.tick(mc);
        });
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of("motions", "auction_hud"), AuctionHud::render);
        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> AuctionManager.onMessage(msg.getString()));
    }
}
