package com.example.mechanix.client;

import com.example.mechanix.MechanixConfig;
import com.example.mechanix.rpg.Race;
import com.example.mechanix.rpg.RpgClass;
import com.example.mechanix.rpg.RpgData;
import com.example.mechanix.rpg.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/** Полоски маны и опыта, слоты заклинаний и способность расы. */
public final class RpgHud {
    private RpgHud() {}

    public static void render(ForgeGui gui, GuiGraphics g, float partialTick, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        if (!MechanixConfig.SHOW_HUD.get()) return;

        RpgData d = RpgData.of(mc.player);
        Race race = d.race();
        if (race == null) return;
        Font f = mc.font;
        long now = mc.level.getGameTime();

        // ----- заголовок -----
        RpgClass cls = d.rpgClass();
        Component title = Component.translatable("race.mechanix." + race.id)
                .append(cls == null ? Component.empty() : Component.literal(" · ").append(Component.translatable("class.mechanix." + cls.id)))
                .append(Component.literal("  " + Component.translatable("gui.mechanix.lvl").getString() + " " + d.level()));
        g.drawString(f, title, 6, 6, 0xFFFFFF, true);

        // ----- мана -----
        int bx = 6, bw = 110;
        double mana = d.mana(), max = Math.max(1.0D, d.maxMana());
        g.fill(bx - 1, 18, bx + bw + 1, 29, 0xFF000000);
        g.fill(bx, 19, bx + bw, 28, 0xFF1B2A55);
        g.fill(bx, 19, bx + (int) (bw * (mana / max)), 28, 0xFF2F7BFF);
        g.drawString(f, (int) mana + " / " + (int) max, bx + 3, 20, 0xFFFFFF, true);

        // ----- опыт -----
        int need = RpgData.xpNeeded(d.level());
        g.fill(bx - 1, 31, bx + bw + 1, 38, 0xFF000000);
        g.fill(bx, 32, bx + bw, 37, 0xFF2B3B14);
        g.fill(bx, 32, bx + (int) (bw * Math.min(1.0D, d.xp() / (double) need)), 37, 0xFF9BE04A);

        // ----- очки -----
        if (d.statPoints() > 0 || d.skillPoints() > 0) {
            g.drawString(f, Component.translatable("gui.mechanix.hud_points", d.statPoints(), d.skillPoints()), bx, 41, 0xFFE066, true);
        }

        // ----- слоты способностей -----
        int slotW = 50;
        int total = slotW * 5 + 4 * 2;
        int x0 = w - total - 6;
        int y0 = h - 58;

        // способность расы (R)
        drawSlot(g, f, x0, y0, slotW,
                KeyBindings.ABILITY.getTranslatedKeyMessage().getString(),
                Component.translatable("race.mechanix." + race.id + ".ability").getString(),
                d.abilityReady() - now, true);

        for (int i = 0; i < 4; i++) {
            Spell sp = d.slot(i);
            int x = x0 + (i + 1) * (slotW + 2);
            if (sp == null) {
                drawSlot(g, f, x, y0, slotW, KeyBindings.SPELLS[i].getTranslatedKeyMessage().getString(), "-", 0, true);
            } else {
                boolean enough = d.mana() >= sp.mana;
                drawSlot(g, f, x, y0, slotW, KeyBindings.SPELLS[i].getTranslatedKeyMessage().getString(),
                        Component.translatable("spell.mechanix." + sp.id).getString(),
                        d.cooldownReady(sp.id) - now, enough);
            }
        }
    }

    private static void drawSlot(GuiGraphics g, Font f, int x, int y, int w, String key, String name, long cdTicks, boolean ok) {
        g.fill(x, y, x + w, y + 26, 0xAA000000);
        g.fill(x, y, x + w, y + 1, 0xFF6B6B6B);
        g.fill(x, y + 25, x + w, y + 26, 0xFF6B6B6B);
        g.drawString(f, key, x + 3, y + 3, 0xFFE066, true);
        String shown = f.plainSubstrByWidth(name, w - 6);
        g.drawString(f, shown, x + 3, y + 15, ok ? 0xFFFFFF : 0xFF6666, true);
        if (cdTicks > 0) {
            g.fill(x, y, x + w, y + 26, 0xAA000000);
            String t = ((cdTicks + 19) / 20) + "s";
            g.drawString(f, t, x + w - f.width(t) - 3, y + 3, 0xFFFFFF, true);
        }
    }
}
