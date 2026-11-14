package com.example.mechanix.network;

import com.example.mechanix.MechanixMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MechanixMod.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {}

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(SyncPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncPacket::encode).decoder(SyncPacket::decode).consumerMainThread(SyncPacket::handle).add();
        CHANNEL.messageBuilder(OpenScreenPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenScreenPacket::encode).decoder(OpenScreenPacket::decode).consumerMainThread(OpenScreenPacket::handle).add();
        CHANNEL.messageBuilder(ActionPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ActionPacket::encode).decoder(ActionPacket::decode).consumerMainThread(ActionPacket::handle).add();
        CHANNEL.messageBuilder(CastPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CastPacket::encode).decoder(CastPacket::decode).consumerMainThread(CastPacket::handle).add();
    }

    public static void sendTo(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
