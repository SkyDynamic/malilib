package fi.dy.masa.malilib.network.handler.server;

import net.minecraft.network.packet.CustomPayload;

public interface IServerPlayHandler
{
    <P extends CustomPayload> void registerServerPlayHandler(IPluginServerPlayHandler<P> handler);

    <P extends CustomPayload> void unregisterServerPlayHandler(IPluginServerPlayHandler<P> handler);
}
