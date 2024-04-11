package fi.dy.masa.malilib.network.payload;

import net.minecraft.util.Identifier;

/**
 * Provides an interface to the PayloadCodec class for getting various data on Payload / Channels
 * that have been registered with PayloadManager
 */
public interface IPayloadType
{
    PayloadType getType();
    String getKey();
    String getNamespace();
    String getPath();
    Identifier getId();
    void registerPlayCodec();
    void registerConfigCodec();
    boolean isPlayRegistered();
    boolean isConfigRegistered();
}
