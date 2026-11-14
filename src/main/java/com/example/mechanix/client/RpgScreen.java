package com.example.mechanix.client;

import com.example.mechanix.network.ActionPacket;
import com.example.mechanix.network.ModNetwork;
import com.example.mechanix.rpg.Race;
import com.example.mechanix.rpg.RpgClass;
import com.example.mechanix.rpg.RpgData;
import com.example.mechanix.rpg.Spell;
import com.example.mechanix.rpg.Stat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Меню персонажа (клавиша K): характеристики, класс, заклинания. */
public class RpgScreen extends Screen {
    private record Row(Spell spell, int y) {}

    private final List<Row> rows = new ArrayList<>();
    private int lastVersion = -1;

    public RpgScreen() {
        super(Component.translatable("gui.mechanix.character"));
    }

    private static void send(int type, int a, String s) {
        ModNetwork.CHANNEL.sendToServer(new ActionPacket(type, a, s));
    }

    @Override
    protected void init() {
        lastVersion = ClientHooks.version;
        rows.clear();
        if (minecraft == null || minecraft.player == null) return;
        RpgData d = RpgData.of(minecraft.player);

        int topY = 70;
        Stat[] stats = Stat.values();
        for (int i = 0; i < stats.length; i++) {
            final int idx = i;
            if (d.statPoints() > 0) {
                addRenderableWidget(Button.builder(Component.literal("+"), b -> send(ActionPacket.STAT, idx, ""))
                        .bounds(112, topY + i * 24, 20, 20).build());
            }
        }

        int rx = 150;
        RpgClass cls = d.rpgClass();
        if (cls == null) {
            if (d.level() >= 3) {
                RpgClass[] cs = RpgClass.values();
                for (int i = 0; i < cs.length; i++) {
                    final RpgClass c = cs[i];
                    addRenderableWidget(Button.builder(Component.translatable("class.mechanix." + c.id),
                                    b -> send(ActionPacket.CLASS, c.ordinal(), ""))
                            .bounds(rx, topY + i * 24, 130, 20).build());
                }
            }
        } else {
            List<Spell> list = Spell.forClass(cls);
            for (int i = 0; i < list.size(); i++) {
                final Spell sp = list.get(i);
                int y = topY + i * 22;
                rows.add(new Row(sp, y));
                if (!d.knows(sp)) {
                    Button learn = Button.builder(Component.translatable("gui.mechanix.learn"),
                                    b -> send(ActionPacket.LEARN, 0, sp.id))
                            .bounds(rx + 140, y, 60, 18).build();
                    learn.active = d.skillPoints() > 0 && d.level() >= sp.level;
                    addRenderableWidget(learn);
                } else {
                    for (int s = 0; s < 4; s++) {
                        final int slot = s;
                        Button bind = Button.builder(Component.literal(String.valueOf(s + 1)),
                                        b -> send(ActionPacket.BIND, slot, sp.id))
                                .bounds(rx + 140 + s * 20, y, 18, 18).build();
                        bind.active = d.slot(s) != sp;
                        addRenderableWidget(bind);
                    }
                }
            }
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.mechanix.close"), b -> onClose())
                .bounds(width - 70, height - 26, 60, 20).build());
    }

    @Override
    public void tick() {
        if (lastVersion != ClientHooks.version) {
            rebuildWidgets();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        if (minecraft == null || minecraft.player == null) return;
        RpgData d = RpgData.of(minecraft.player);
        Race race = d.race();

        g.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        if (race != null) {
            RpgClass cls = d.rpgClass();
            Component line = Component.translatable("race.mechanix." + race.id)
                    .append(Component.literal(" · "))
                    .append(cls == null ? Component.translatable("gui.mechanix.no_class") : Component.translatable("class.mechanix." + cls.id))
                    .append(Component.literal(" · " + Component.translatable("gui.mechanix.lvl").getString() + " " + d.level()));
            g.drawString(font, line, 12, 24, 0xFFE066, true);
            g.drawString(font, Component.translatable("gui.mechanix.xp_line", d.xp(), RpgData.xpNeeded(d.level())), 12, 36, 0x9BE04A, true);
            g.drawString(font, Component.translatable("gui.mechanix.points", d.statPoints(), d.skillPoints()), 12, 48, 0xFFFFFF, true);
        }

        // характеристики
        Stat[] stats = Stat.values();
        for (int i = 0; i < stats.length; i++) {
            Component label = Component.translatable("stat.mechanix." + stats[i].key())
                    .append(Component.literal(": " + d.total(stats[i])));
            g.drawString(font, label, 12, 70 + i * 24 + 6, 0xFFFFFF, true);
        }

        // класс / заклинания
        int rx = 150;
        if (d.rpgClass() == null) {
            Component hint = d.level() >= 3
                    ? Component.translatable("gui.mechanix.choose_class")
                    : Component.translatable("gui.mechanix.class_locked");
            g.drawString(font, hint, rx, 56, 0xFFE066, true);
        } else {
            g.drawString(font, Component.translatable("gui.mechanix.spells"), rx, 56, 0xFFE066, true);
            for (Row row : rows) {
                boolean known = d.knows(row.spell());
                int color = known ? 0x9BE04A : (d.level() >= row.spell().level ? 0xDDDDDD : 0x777777);
                String name = font.plainSubstrByWidth(Component.translatable("spell.mechanix." + row.spell().id).getString(), 128);
                g.drawString(font, name, rx, row.y() + 5, color, true);
            }
        }
        super.render(g, mouseX, mouseY, partialTick);

        for (Row row : rows) {
            if (mouseX >= 150 && mouseX <= 280 && mouseY >= row.y() && mouseY < row.y() + 18) {
                Spell sp = row.spell();
                g.renderTooltip(font, List.of(
                        Component.translatable("spell.mechanix." + sp.id).getVisualOrderText(),
                        Component.translatable("spell.mechanix." + sp.id + ".desc").getVisualOrderText(),
                        Component.translatable("gui.mechanix.spell_info", sp.level, sp.mana, sp.cooldown / 20).getVisualOrderText()
                ), mouseX, mouseY);
            }
        }
    }
}
