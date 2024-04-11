package fi.dy.masa.malilib.event;

import fi.dy.masa.malilib.interfaces.IPlayerListener;

public interface IPlayerManager
{
    void registerPlayerHandler(IPlayerListener handler);
    void unregisterPlayerHandler(IPlayerListener handler);
}
