package com.example.mechanix.network;

import com.example.mechanix.client.ClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Сервер -> клиент: открыть экран (0 = выбор расы). */
public class OpenScreenPacket {
    private final int id;

    public OpenScreenPacket(int id) {
        this.id = id;
    }

    public static void encode(OpenScreenPacket p, FriendlyByteBuf buf) {
        buf.writeVarInt(p.id);
    }

    public static OpenScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenScreenPacket(buf.readVarInt());
    }

    public static void handle(OpenScreenPacket p, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.openScreen(p.id));
        ctx.get().setPacketHandled(true);
    }
}
