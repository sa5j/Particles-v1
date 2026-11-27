package com.example.particlecustomizer;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ParticleCustomizerClient implements ClientModInitializer {
    private static ParticleConfig config;
    private static KeyMapping openGuiKey;

    @Override
    public void onInitializeClient() {
        config = ParticleConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Component.literal("Particle Customizer")
        );

        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.particlecustomizer.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                category
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKey.consumeClick()) {
                if (client.player != null && client.gui != null) {
                    client.gui.setScreen(new ParticleCustomizerScreen(null));
                }
            }
        });
    }

    public static ParticleConfig getConfig() {
        return config;
    }
}
