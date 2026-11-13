package com.example.mechanix.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * /mechanix sethome  — запомнить текущую точку
 * /mechanix home     — телепорт к сохранённой точке (в т.ч. между измерениями)
 * /mechanix heal     — полное лечение и сытость (только операторам)
 */
public class MechanixCommands {
    private static final String HOME_KEY = "mechanix_home";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mechanix")
                .then(Commands.literal("sethome").executes(ctx -> setHome(ctx.getSource())))
                .then(Commands.literal("home").executes(ctx -> goHome(ctx.getSource())))
                .then(Commands.literal("heal")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> heal(ctx.getSource()))));
    }

    private static int setHome(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayerOrException();
        CompoundTag tag = new CompoundTag();
        tag.putDouble("x", player.getX());
        tag.putDouble("y", player.getY());
        tag.putDouble("z", player.getZ());
        tag.putFloat("yaw", player.getYRot());
        tag.putString("dim", player.level().dimension().location().toString());
        player.getPersistentData().put(HOME_KEY, tag);

        src.sendSuccess(() -> Component.translatable("message.mechanix.home_set"), false);
        return 1;
    }

    private static int goHome(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayerOrException();
        if (!player.getPersistentData().contains(HOME_KEY)) {
            src.sendFailure(Component.translatable("message.mechanix.no_home"));
            return 0;
        }

        CompoundTag tag = player.getPersistentData().getCompound(HOME_KEY);
        ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("dim")));
        ServerLevel target = src.getServer().getLevel(dim);
        if (target == null) {
            src.sendFailure(Component.translatable("message.mechanix.no_home"));
            return 0;
        }

        player.teleportTo(target, tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"),
                tag.getFloat("yaw"), player.getXRot());
        player.fallDistance = 0.0F;
        src.sendSuccess(() -> Component.translatable("message.mechanix.home_tp"), false);
        return 1;
    }

    private static int heal(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayerOrException();
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0F);
        src.sendSuccess(() -> Component.translatable("message.mechanix.healed"), false);
        return 1;
    }
}
