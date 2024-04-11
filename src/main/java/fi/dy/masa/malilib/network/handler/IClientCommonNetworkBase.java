package fi.dy.masa.malilib.network.handler;

import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.CustomPayload;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public interface IClientCommonNetworkBase
{
     <H extends ClientCommonNetworkHandler> void handleClientPayload(H handler, CustomPayload payload, CallbackInfo ci);
}
