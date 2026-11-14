package com.example.mechanix.network;

import com.example.mechanix.client.ClientHooks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Сервер -> клиент: полные данные RPG игрока. */
public class SyncPacket {
    private final CompoundTag tag;

    public SyncPacket(CompoundTag tag) {
        this.tag = tag;
    }

    public static void encode(SyncPacket p, FriendlyByteBuf buf) {
        buf.writeNbt(p.tag);
    }

    public static SyncPacket decode(FriendlyByteBuf buf) {
        CompoundTag t = buf.readNbt();
        return new SyncPacket(t == null ? new CompoundTag() : t);
    }

    public static void handle(SyncPacket p, Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHooks.handleSync(p.tag));
        ctx.get().setPacketHandled(true);
    }
}
