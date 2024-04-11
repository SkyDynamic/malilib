package fi.dy.masa.malilib.network.handler;

import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface IServerCommonNetworkBase
{
     <H extends ServerCommonNetworkHandler> void handleServerPayload(H handler, CustomPayloadC2SPacket packet, MinecraftServer server, CallbackInfo ci);
}
