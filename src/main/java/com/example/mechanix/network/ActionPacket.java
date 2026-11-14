package com.example.mechanix.network;

import com.example.mechanix.rpg.Rpg;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Клиент -> сервер: действие из меню (раса, характеристики, класс, заклинания). */
public class ActionPacket {
    public static final int STAT = 1;
    public static final int CLASS = 2;
    public static final int LEARN = 3;
    public static final int BIND = 4;
    public static final int RACE = 5;

    private final int type;
    private final int a;
    private final String s;

    public ActionPacket(int type, int a, String s) {
        this.type = type;
        this.a = a;
        this.s = s;
    }

    public static void encode(ActionPacket p, FriendlyByteBuf buf) {
        buf.writeVarInt(p.type);
        buf.writeVarInt(p.a);
        buf.writeUtf(p.s, 64);
    }

    public static ActionPacket decode(FriendlyByteBuf buf) {
        return new ActionPacket(buf.readVarInt(), buf.readVarInt(), buf.readUtf(64));
    }

    public static void handle(ActionPacket p, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender != null) Rpg.handleAction(sender, p.type, p.a, p.s);
        ctx.get().setPacketHandled(true);
    }
}
