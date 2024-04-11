package fi.dy.masa.malilib.network.payload;

import fi.dy.masa.malilib.MaLiLib;
import fi.dy.masa.malilib.MaLiLibReference;
import fi.dy.masa.malilib.network.handler.client.ClientConfigHandler;
import fi.dy.masa.malilib.network.handler.server.ServerConfigHandler;
import fi.dy.masa.malilib.network.handler.client.ClientPlayHandler;
import fi.dy.masa.malilib.network.handler.server.ServerPlayHandler;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This is made to "manage" the payload types and do the actual channel registrations via the Fabric Network API (4.0.0+)
 * From here, we Map the payload CODEC and TYPE into a HashMap; for our own reference by the Payloads based on their PayloadType.
 * This was done in an attempt to make the remaining functions more abstract.
 */
public class PayloadManager
{
    public static final PayloadManager INSTANCE = new PayloadManager();
    public static PayloadManager getInstance() { return INSTANCE; }
    private final Map<PayloadType, PayloadCodec> TYPES = new HashMap<>();

    public PayloadManager() {}

    /**
     * Registers a Payload Type with PayloadManager
     * -
     * @param type (PayloadType ENUM)
     * @param key (Payload KEY value, used for a sort of "default" data field mapping)
     * @param namespace (Identifier Namespace, ie, servux)
     * @param path (Identifier path, ie, structures)
     */
    public void register(PayloadType type, String key, String namespace, String path)
    {
        if (!TYPES.containsKey(type))
        {
            PayloadCodec codec = new PayloadCodec(type, key, namespace, path);
            TYPES.put(type, codec);
            MaLiLib.printDebug("PayloadManager#register(): registering a new PayloadCodec id: {} // {}:{}", codec.getId().hashCode(), codec.getId().getNamespace(), codec.getId().getPath());

        }
        else
        {
            MaLiLib.logger.error("PayloadManager#register(): type {} already exists.", type.toString());
        }
    }

    /**
     * Actual Fabric API Play Channel registration.
     */
    public <T extends CustomPayload> void registerPlayChannel(PayloadType type, CustomPayload.Id<T> id, PacketCodec<PacketByteBuf, T> packetCodec)
    {
        PayloadCodec codec = getPayloadCodec(type);

        // Never Attempt to "re-register" a channel or bad things will happen.  Kittens harmed, etc.
        if (codec == null || codec.isPlayRegistered())
            return;

        codec.registerPlayCodec();

        // Checks with Fabric APIs IMPL layer (I don't think they are confident with their code yet)
        if (PayloadTypeRegistryImpl.PLAY_S2C.get(id) != null || PayloadTypeRegistryImpl.PLAY_C2S.get(id) != null)
        {
            // This just saved Minecraft from crashing, your welcome.
            MaLiLib.logger.error("registerPlayChannel(): blocked duplicate Play Channel registration attempt for: {}.", id.id().toString());
        }
        else
        {
            MaLiLib.printDebug("PayloadManager#registerPlayChannel(): [Fabric-API] registering Play C2S Channel: {}", id.id().toString());
            PayloadTypeRegistry.playC2S().register(id, packetCodec);
            PayloadTypeRegistry.playS2C().register(id, packetCodec);
            // We need to register the channel bi-directionally for it to work.
        }
    }

    /**
     * Actual Fabric API Config Channel registration.
     */
    public <T extends CustomPayload> void registerConfigChannel(PayloadType type, CustomPayload.Id<T> id, PacketCodec<PacketByteBuf, T> packetCodec)
    {
        PayloadCodec codec = getPayloadCodec(type);

        // Never Attempt to "re-register" a channel or bad things will happen.  Kittens harmed, etc.
        if (codec == null || codec.isConfigRegistered())
            return;
        codec.registerConfigCodec();

        // Checks with Fabric APIs IMPL layer (I don't think they are confident with their code yet)
        if (PayloadTypeRegistryImpl.CONFIGURATION_S2C.get(id) != null || PayloadTypeRegistryImpl.CONFIGURATION_C2S.get(id) != null)
        {
            // This just saved Minecraft from crashing, your welcome.
            MaLiLib.logger.error("registerConfigChannel(): blocked duplicate Configuration Channel registration attempt for: {}.", id.id().toString());
        }
        else
        {
            MaLiLib.printDebug("PayloadManager#registerConfigChannel(): [Fabric-API] registering Configuration C2S Channel: {}", id.id().toString());
            PayloadTypeRegistry.configurationC2S().register(id, packetCodec);
            PayloadTypeRegistry.configurationS2C().register(id, packetCodec);
            // We need to register the channel bi-directionally for it to work.
        }
    }

    /**
     * Abstract method for CustomPayload's to define their PACKET_CODEC value.
     */
    @Nullable
    public PayloadCodec getPayloadCodec(PayloadType type)
    {
        return TYPES.getOrDefault(type, null);
    }

    /**
     * Abstract method for CustomPayload's to define their PACKET_TYPE value, derived from the channel Identifier
     */
    @Nullable
    public Identifier getIdentifier(PayloadType type)
    {
        return TYPES.getOrDefault(type, null).getId();
    }

    /**
     * The Payload "KEY" field is simply for declaring any special "default" key Values for data if none are known,
     * Such as for example nbt.getString(KEY) -- These are not required, but can prove to be very useful.
     */
    @Nullable
    public String getKey(PayloadType type)
    {
        return TYPES.getOrDefault(type, null).getKey();
    }

    /**
     * Search for a registered Payload type by Identifier
     */
    @Nullable
    public PayloadType getPayloadType(Identifier id)
    {
        for (PayloadType type : TYPES.keySet())
        {
            PayloadCodec codec = TYPES.get(type);
            if (codec != null)
            {
                if (codec.getId().equals(id))
                {
                    return type;
                }
            }
        }

        return null;
    }

    /**
     * Forces a reset() signal on all registered payloads
     */
    public void resetPayloads()
    {
        MaLiLib.printDebug("PayloadManager#resetPayloads(): sending reset() to all registered Payload listeners.");

        for (PayloadType type : TYPES.keySet())
        {
            if (TYPES.get(type).isPlayRegistered())
            {
                if (MaLiLibReference.isClient())
                {
                    ((ClientPlayHandler<?>) ClientPlayHandler.getInstance()).reset(type);
                }
                if (MaLiLibReference.isServer() || MaLiLibReference.isOpenToLan() || MaLiLibReference.isDedicated())
                {
                    ((ServerPlayHandler<?>) ServerPlayHandler.getInstance()).reset(type);
                }
            }
            if (TYPES.get(type).isConfigRegistered())
            {
                if (MaLiLibReference.isClient())
                {
                    ((ClientConfigHandler<?>) ClientConfigHandler.getInstance()).reset(type);
                }
                if (MaLiLibReference.isServer() || MaLiLibReference.isOpenToLan() || MaLiLibReference.isDedicated())
                {
                    ((ServerConfigHandler<?>) ServerConfigHandler.getInstance()).reset(type);
                }
            }
        }
    }

    public void verifyAllPayloads()
    {
        MaLiLib.printDebug("PayloadManager#verifyAllPayloads(): sending registerPayloads() to all registered Payload listeners.");

        for (PayloadType type : TYPES.keySet())
        {
            if (!TYPES.get(type).isPlayRegistered())
            {
                if (MaLiLibReference.isClient())
                {
                    ((ClientPlayHandler<?>) ClientPlayHandler.getInstance()).registerPlayPayload(type);
                }
                if (MaLiLibReference.isServer() || MaLiLibReference.isOpenToLan() || MaLiLibReference.isDedicated())
                {
                    ((ServerPlayHandler<?>) ServerPlayHandler.getInstance()).registerPlayPayload(type);
                }
            }
            if (!TYPES.get(type).isConfigRegistered())
            {
                if (MaLiLibReference.isClient())
                {
                    ((ClientConfigHandler<?>) ClientConfigHandler.getInstance()).registerConfigPayload(type);
                }
                if (MaLiLibReference.isServer() || MaLiLibReference.isOpenToLan() || MaLiLibReference.isDedicated())
                {
                    ((ServerConfigHandler<?>) ServerConfigHandler.getInstance()).registerConfigPayload(type);
                }
            }
        }
    }

    /**
     * Forces a Type Handler Registration signal on all registered payloads
     */
    public void registerAllHandlers()
    {
        MaLiLib.printDebug("PayloadManager#registerAllHandlers(): sending registerHandlers() to all registered Payload listeners.");

        for (PayloadType type : TYPES.keySet())
        {
            if (TYPES.get(type).isPlayRegistered())
            {
                if (MaLiLibReference.isClient())
                {
                    ((ClientPlayHandler<?>) ClientPlayHandler.getInstance()).registerPlayHandler(type);
                }
                if (MaLiLibReference.isServer() || MaLiLibReference.isOpenToLan() || MaLiLibReference.isDedicated())
                {
                    ((ServerPlayHandler<?>) ServerPlayHandler.getInstance()).registerPlayHandler(type);
                }
            }
            if (TYPES.get(type).isConfigRegistered())
            {
                if (MaLiLibReference.isClient())
                {
                    ((ClientConfigHandler<?>) ClientConfigHandler.getInstance()).registerConfigHandler(type);
                }
                if (MaLiLibReference.isServer() || MaLiLibReference.isOpenToLan() || MaLiLibReference.isDedicated())
                {
                    ((ServerConfigHandler<?>) ServerConfigHandler.getInstance()).registerConfigHandler(type);
                }
            }
        }
    }
}
