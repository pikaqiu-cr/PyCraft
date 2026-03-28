package io.github.sweetzonzi.py_port.network.python.payload;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class PlayerNameHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // 头顶名字
        player.setCustomName(Component.literal("NewName"));
        player.setCustomNameVisible(true);

    }
}