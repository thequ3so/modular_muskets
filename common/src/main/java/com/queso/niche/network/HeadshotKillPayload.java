package com.queso.niche.network;

import com.queso.niche.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record HeadshotKillPayload() implements CustomPacketPayload {

    public static final HeadshotKillPayload INSTANCE = new HeadshotKillPayload();

    public static final Type<HeadshotKillPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "headshot_kill"));

    public static final StreamCodec<FriendlyByteBuf, HeadshotKillPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
