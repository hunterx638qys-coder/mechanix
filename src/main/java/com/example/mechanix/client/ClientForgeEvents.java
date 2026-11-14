package com.example.mechanix.client;

import com.example.mechanix.MechanixMod;
import com.example.mechanix.network.CastPacket;
import com.example.mechanix.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Обработка клавиш (только клиент). */
@Mod.EventBusSubscriber(modid = MechanixMod.MODID, value = Dist.CLIENT)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (KeyBindings.MENU.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new RpgScreen());
        }
        for (int i = 0; i < KeyBindings.SPELLS.length; i++) {
            while (KeyBindings.SPELLS[i].consumeClick()) {
                if (mc.screen == null) ModNetwork.CHANNEL.sendToServer(new CastPacket(i));
            }
        }
        while (KeyBindings.ABILITY.consumeClick()) {
            if (mc.screen == null) ModNetwork.CHANNEL.sendToServer(new CastPacket(4));
        }
    }
}
