package fi.dy.masa.malilib.network.handler;

import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.MaLiLibReference;
import fi.dy.masa.malilib.network.payload.PayloadType;
import fi.dy.masa.malilib.network.payload.channel.*;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * This probably cannot be made too abstract, because it references items in the static context directly (i.e., Specific Payload types)
 */
public class ClientCommonHandlerRegister
{
    public static final ClientCommonHandlerRegister INSTANCE = new ClientCommonHandlerRegister();
    public static ClientCommonHandlerRegister getInstance() { return INSTANCE; }

    public <T extends CustomPayload> void registerPlayHandler(CustomPayload.Id<T> type, ClientPlayNetworking.PlayPayloadHandler<T> handler)
    {
        if (MaLiLibReference.isClient())
        {
            MaLiLib.printDebug("ClientCommonHandlerRegister#registerPlayHandler(): for type {}", type.id().toString());
            ClientPlayNetworking.registerGlobalReceiver(type, handler);
        }
        else
        {
            MaLiLib.logger.error("ClientCommonHandlerRegister#registerPlayHandler(): Blocked registerGlobalReceiver due to not being in a CLIENT environment.");
        }
    }

    public <T extends CustomPayload> void unregisterPlayHandler(CustomPayload.Id<T> type)
    {
        if (MaLiLibReference.isClient())
        {
            MaLiLib.printDebug("ClientCommonHandlerRegister#unregisterPlayHandler(): for type {}", type.id().toString());
            ClientPlayNetworking.unregisterGlobalReceiver(type.id());
        }
        else
        {
            MaLiLib.logger.error("ClientCommonHandlerRegister#unregisterPlayHandler(): Blocked unregisterGlobalReceiver due to not being in a CLIENT environment.");
        }
    }

    public <T extends CustomPayload> void registerConfigHandler(CustomPayload.Id<T> type, ClientConfigurationNetworking.ConfigurationPayloadHandler<T> handler)
    {
        if (MaLiLibReference.isClient())
        {
            MaLiLib.printDebug("ClientCommonHandlerRegister#registerConfigHandler(): for type {}", type.id().toString());
            ClientConfigurationNetworking.registerGlobalReceiver(type, handler);
        }
        else
        {
            MaLiLib.logger.error("ClientCommonHandlerRegister#registerConfigHandler(): Blocked registerGlobalReceiver due to not being in a CLIENT environment.");
        }
    }

    public <T extends CustomPayload> void unregisterConfigHandler(CustomPayload.Id<T> type)
    {
        if (MaLiLibReference.isClient())
        {
            MaLiLib.printDebug("ClientCommonHandlerRegister#unregisterConfigHandler(): for type {}", type.id().toString());
            ClientConfigurationNetworking.unregisterGlobalReceiver(type);
        }
        else
        {
            MaLiLib.logger.error("ClientCommonHandlerRegister#unregisterConfigHandler(): Blocked unregisterGlobalReceiver due to not being in a CLIENT environment.");
        }
    }

    // TODO --> An entry here needs to exist for every MaLiLib Payload type
    @SuppressWarnings("unchecked")
    public <T extends CustomPayload> CustomPayload.Id<T> getPayloadType(PayloadType type)
    {
        //MaLiLib.printDebug("ClientCommonHandlerRegister#getPayload(): type {}", type.toString());
        if (type == PayloadType.CARPET_HELLO)
        {
            return (CustomPayload.Id<T>) CarpetHelloPayload.TYPE;
        }
        else if (type == PayloadType.MALILIB_BYTEBUF)
        {
            return (CustomPayload.Id<T>) MaLibBufPayload.TYPE;
        }
        else if (type == PayloadType.SERVUX_BLOCKS)
        {
            return (CustomPayload.Id<T>) ServuxBlocksPayload.TYPE;
        }
        else if (type == PayloadType.SERVUX_ENTITIES)
        {
            return (CustomPayload.Id<T>) ServuxEntitiesPayload.TYPE;
        }
        else if (type == PayloadType.SERVUX_LITEMATICS)
        {
            return (CustomPayload.Id<T>) ServuxMetadataPayload.TYPE;
        }
        else if (type == PayloadType.SERVUX_METADATA)
        {
            return (CustomPayload.Id<T>) ServuxMetadataPayload.TYPE;
        }
        else if (type == PayloadType.SERVUX_STRUCTURES)
        {
            return (CustomPayload.Id<T>) ServuxStructuresPayload.TYPE;
        }
        else
        {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public <B extends ByteBuf, T extends CustomPayload> PacketCodec<B, T> getPacketCodec(PayloadType type)
    {
        //MaLiLib.printDebug("ClientCommonHandlerRegister#getPacketCodec(): type {}", type.toString());
        if (type == PayloadType.CARPET_HELLO)
        {
            return (PacketCodec<B, T>) CarpetHelloPayload.CODEC;
        }
        else if (type == PayloadType.MALILIB_BYTEBUF)
        {
            return (PacketCodec<B, T>) MaLibBufPayload.CODEC;
        }
        else if (type == PayloadType.SERVUX_BLOCKS)
        {
            return (PacketCodec<B, T>) ServuxBlocksPayload.CODEC;
        }
        else if (type == PayloadType.SERVUX_ENTITIES)
        {
            return (PacketCodec<B, T>) ServuxEntitiesPayload.CODEC;
        }
        else if (type == PayloadType.SERVUX_LITEMATICS)
        {
            return (PacketCodec<B, T>) ServuxLitematicsPayload.CODEC;
        }
        else if (type == PayloadType.SERVUX_METADATA)
        {
            return (PacketCodec<B, T>) ServuxMetadataPayload.CODEC;
        }
        else if (type == PayloadType.SERVUX_STRUCTURES)
        {
            return (PacketCodec<B, T>) ServuxStructuresPayload.CODEC;
        }
        else
        {
            return null;
        }
    }
}
