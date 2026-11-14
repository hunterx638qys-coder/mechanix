package com.example.mechanix.client;

import com.example.mechanix.rpg.RpgData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

/** Клиентские обработчики сетевых пакетов (вызываются только на клиенте). */
public final class ClientHooks {
    /** Растёт при каждой синхронизации, чтобы экраны знали, что пора обновиться. */
    public static int version = 0;

    private ClientHooks() {}

    public static void handleSync(CompoundTag tag) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.getPersistentData().put(RpgData.KEY, tag.copy());
        version++;
    }

    public static void openScreen(int id) {
        Minecraft mc = Minecraft.getInstance();
        if (id == 0) {
            mc.setScreen(new RaceScreen());
        }
    }
}
