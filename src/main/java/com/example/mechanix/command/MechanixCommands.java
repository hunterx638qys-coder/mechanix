package com.example.mechanix.command;

import com.example.mechanix.rpg.Race;
import com.example.mechanix.rpg.Rpg;
import com.example.mechanix.rpg.RpgClass;
import com.example.mechanix.rpg.RpgData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;

/**
 * Команды отладки (только операторы):
 * /mechanix level|xp|points &lt;n&gt;, /mechanix race|class &lt;id&gt;, /mechanix mana, /mechanix reset, /mechanix info
 */
public class MechanixCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mechanix")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("level").then(Commands.argument("n", IntegerArgumentType.integer(1, 200))
                        .executes(c -> setLevel(c.getSource(), IntegerArgumentType.getInteger(c, "n")))))
                .then(Commands.literal("xp").then(Commands.argument("n", IntegerArgumentType.integer(1, 1000000))
                        .executes(c -> addXp(c.getSource(), IntegerArgumentType.getInteger(c, "n")))))
                .then(Commands.literal("points").then(Commands.argument("n", IntegerArgumentType.integer(1, 1000))
                        .executes(c -> addPoints(c.getSource(), IntegerArgumentType.getInteger(c, "n")))))
                .then(Commands.literal("race").then(Commands.argument("id", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(Race.values()).map(r -> r.id), b))
                        .executes(c -> setRace(c.getSource(), StringArgumentType.getString(c, "id")))))
                .then(Commands.literal("class").then(Commands.argument("id", StringArgumentType.word())
                        .suggests((c, b) -> SharedSuggestionProvider.suggest(Arrays.stream(RpgClass.values()).map(r -> r.id), b))
                        .executes(c -> setClass(c.getSource(), StringArgumentType.getString(c, "id")))))
                .then(Commands.literal("mana").executes(c -> fillMana(c.getSource())))
                .then(Commands.literal("reset").executes(c -> reset(c.getSource()))));
    }

    private static int setLevel(CommandSourceStack src, int n) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        RpgData d = RpgData.of(p);
        if (!d.hasRace()) return noRace(src);
        int old = d.level();
        d.setLevel(n);
        int diff = d.level() - old;
        if (diff > 0) {
            d.setStatPoints(d.statPoints() + diff * 3);
            d.setSkillPoints(d.skillPoints() + diff);
        }
        d.setXp(0);
        d.setMana(d.maxMana());
        Rpg.refresh(p);
        src.sendSuccess(() -> Component.literal("Level = " + d.level()), false);
        return 1;
    }

    private static int addXp(CommandSourceStack src, int n) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        if (!RpgData.of(p).hasRace()) return noRace(src);
        Rpg.addXp(p, n);
        return 1;
    }

    private static int addPoints(CommandSourceStack src, int n) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        RpgData d = RpgData.of(p);
        d.setStatPoints(d.statPoints() + n);
        d.setSkillPoints(d.skillPoints() + n);
        Rpg.sync(p);
        src.sendSuccess(() -> Component.literal("+" + n + " stat/skill points"), false);
        return 1;
    }

    private static int setRace(CommandSourceStack src, String id) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        Race r = Race.byId(id);
        if (r == null) {
            src.sendFailure(Component.literal("Unknown race: " + id));
            return 0;
        }
        RpgData d = RpgData.of(p);
        d.setRace(r);
        d.setMana(d.maxMana());
        Rpg.refresh(p);
        src.sendSuccess(() -> Component.translatable("msg.mechanix.race_chosen", Component.translatable("race.mechanix." + r.id)), false);
        return 1;
    }

    private static int setClass(CommandSourceStack src, String id) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        RpgClass c = RpgClass.byId(id);
        if (c == null) {
            src.sendFailure(Component.literal("Unknown class: " + id));
            return 0;
        }
        RpgData d = RpgData.of(p);
        d.setClass(c);
        Rpg.refresh(p);
        src.sendSuccess(() -> Component.literal("Class = " + c.id), false);
        return 1;
    }

    private static int fillMana(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        RpgData d = RpgData.of(p);
        d.setMana(d.maxMana());
        Rpg.sync(p);
        return 1;
    }

    private static int reset(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer p = src.getPlayerOrException();
        p.getPersistentData().remove(RpgData.KEY);
        Rpg.refresh(p);
        src.sendSuccess(() -> Component.literal("RPG data reset. Relog to choose a race again."), false);
        return 1;
    }

    private static int noRace(CommandSourceStack src) {
        src.sendFailure(Component.translatable("msg.mechanix.no_race"));
        return 0;
    }
}
