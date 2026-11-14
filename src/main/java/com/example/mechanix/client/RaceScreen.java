package com.example.mechanix.client;

import com.example.mechanix.network.ActionPacket;
import com.example.mechanix.network.ModNetwork;
import com.example.mechanix.rpg.Race;
import com.example.mechanix.rpg.Stat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/** Экран выбора расы при первом входе. Закрыть без выбора нельзя. */
public class RaceScreen extends Screen {
    private Race selected;
    private Button confirm;

    public RaceScreen() {
        super(Component.translatable("gui.mechanix.choose_race"));
    }

    @Override
    protected void init() {
        Race[] races = Race.values();
        int colW = 104;
        for (int i = 0; i < races.length; i++) {
            final Race r = races[i];
            int col = i / 7;
            int row = i % 7;
            addRenderableWidget(Button.builder(Component.translatable("race.mechanix." + r.id), b -> selected = r)
                    .bounds(12 + col * (colW + 4), 34 + row * 23, colW, 20).build());
        }
        confirm = addRenderableWidget(Button.builder(Component.translatable("gui.mechanix.confirm"), b -> {
            if (selected != null) {
                ModNetwork.CHANNEL.sendToServer(new ActionPacket(ActionPacket.RACE, selected.ordinal(), ""));
                onClose();
            }
        }).bounds(width / 2 - 60, height - 28, 120, 20).build());
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
        confirm.active = selected != null;
        super.render(g, mouseX, mouseY, partialTick);
        if (selected != null) drawDetails(g);
    }

    private void drawDetails(GuiGraphics g) {
        int x = 12 + 2 * 108 + 8;
        int w = width - x - 10;
        if (w < 80) return;
        int y = 34;
        String id = selected.id;

        g.drawString(font, Component.translatable("race.mechanix." + id).withStyle(net.minecraft.ChatFormatting.GOLD, net.minecraft.ChatFormatting.BOLD), x, y, 0xFFFFFF, true);
        y += 14;
        y = wrap(g, Component.translatable("race.mechanix." + id + ".desc"), x, y, w, 0xDDDDDD) + 4;

        StringBuilder sb = new StringBuilder();
        for (Stat s : Stat.values()) {
            int v = selected.bonus(s);
            if (v != 0) sb.append(I18n.get("stat.mechanix." + s.key())).append(' ').append(v > 0 ? "+" : "").append(v).append("   ");
        }
        if (selected.health != 0) sb.append(I18n.get("gui.mechanix.health")).append(' ').append(selected.health > 0 ? "+" : "").append((int) selected.health).append("   ");
        if (selected.speedPct != 0) sb.append(I18n.get("gui.mechanix.speed")).append(' ').append(selected.speedPct > 0 ? "+" : "").append(selected.speedPct).append("%   ");
        if (selected.mana != 0) sb.append(I18n.get("gui.mechanix.mana")).append(' ').append(selected.mana > 0 ? "+" : "").append(selected.mana).append("   ");
        if (selected.xpPct != 0) sb.append(I18n.get("gui.mechanix.xp")).append(" +").append(selected.xpPct).append("%");
        y = wrap(g, Component.literal(sb.toString()), x, y, w, 0x9BE04A) + 4;

        g.drawString(font, Component.translatable("gui.mechanix.passive"), x, y, 0xFFE066, true);
        y += 11;
        y = wrap(g, Component.translatable("race.mechanix." + id + ".passive"), x, y, w, 0xDDDDDD) + 4;

        g.drawString(font, Component.translatable("gui.mechanix.ability", Component.translatable("race.mechanix." + id + ".ability")), x, y, 0x66CCFF, true);
        y += 11;
        y = wrap(g, Component.translatable("race.mechanix." + id + ".ability.desc"), x, y, w, 0xDDDDDD) + 2;
        g.drawString(font, Component.translatable("gui.mechanix.cooldown", selected.cooldownSec), x, y, 0x888888, true);
    }

    private int wrap(GuiGraphics g, Component text, int x, int y, int w, int color) {
        List<FormattedCharSequence> lines = font.split(text, w);
        for (FormattedCharSequence line : lines) {
            g.drawString(font, line, x, y, color, true);
            y += 10;
        }
        return y;
    }
}
