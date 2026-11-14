package com.example.mechanix.network;

import com.example.mechanix.rpg.Rpg;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Клиент -> сервер: нажата клавиша способности (0-3 заклинания, 4 способность расы). */
public class CastPacket {
    private final int slot;

    public CastPacket(int slot) {
        this.slot = slot;
    }

    public static void encode(CastPacket p, FriendlyByteBuf buf) {
        buf.writeVarInt(p.slot);
    }

    public static CastPacket decode(FriendlyByteBuf buf) {
        return new CastPacket(buf.readVarInt());
    }

    public static void handle(CastPacket p, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender != null) Rpg.cast(sender, p.slot);
        ctx.get().setPacketHandled(true);
    }
}
