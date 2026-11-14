package com.example.mechanix.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {
    private static final String CATEGORY = "key.categories.mechanix";

    public static final KeyMapping[] SPELLS = {
            new KeyMapping("key.mechanix.spell1", GLFW.GLFW_KEY_Z, CATEGORY),
            new KeyMapping("key.mechanix.spell2", GLFW.GLFW_KEY_X, CATEGORY),
            new KeyMapping("key.mechanix.spell3", GLFW.GLFW_KEY_C, CATEGORY),
            new KeyMapping("key.mechanix.spell4", GLFW.GLFW_KEY_V, CATEGORY)
    };
    public static final KeyMapping ABILITY = new KeyMapping("key.mechanix.ability", GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping MENU = new KeyMapping("key.mechanix.menu", GLFW.GLFW_KEY_K, CATEGORY);

    private KeyBindings() {}
}
