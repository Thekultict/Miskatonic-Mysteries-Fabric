package com.miskatonicmysteries.common.handler.networking.packet.s2c;

import com.miskatonicmysteries.common.feature.interfaces.Sanity;
import com.miskatonicmysteries.common.handler.networking.PacketHandler;
import com.miskatonicmysteries.common.lib.Constants;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ExpandSanityPacket {
    public static final Identifier ID = new Identifier(Constants.MOD_ID, "sanity_expansion");

    public static void send(PlayerEntity player, String name, int amount) {
        PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
        data.writeString(name);
        data.writeInt(amount);
        PacketHandler.sendToPlayer(player, data, ID);
    }

    public static void handle(MinecraftClient client, ClientPlayNetworkHandler networkHandler, PacketByteBuf packetByteBuf, PacketSender sender) {
        String name = packetByteBuf.readString();
        int amount = packetByteBuf.readInt();
        client.execute(() -> ((Sanity) client.player).addSanityCapExpansion(name, amount));
    }
}
