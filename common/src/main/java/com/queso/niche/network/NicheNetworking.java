package com.queso.niche.network;

import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public final class NicheNetworking {

    public static Consumer<ServerPlayer> headshotKillSender = player -> {};

    public static void sendHeadshotKill(ServerPlayer player) {
        headshotKillSender.accept(player);
    }

    private NicheNetworking() {}
}
