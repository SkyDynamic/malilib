package fi.dy.masa.malilib.network.handler.client;

import net.minecraft.network.packet.CustomPayload;

public interface IClientConfigHandler
{
    <P extends CustomPayload> void registerClientConfigHandler(IPluginClientConfigHandler<P> handler);

    <P extends CustomPayload> void unregisterClientConfigHandler(IPluginClientConfigHandler<P> handler);
}
