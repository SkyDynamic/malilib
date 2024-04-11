package fi.dy.masa.malilib.network.handler.client;

import com.google.common.collect.ArrayListMultimap;
import fi.dy.masa.malilib.network.payload.MaLibByteBuf;
import fi.dy.masa.malilib.network.payload.PayloadType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.CustomPayload;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ClientPlayHandler<T extends CustomPayload> implements IClientPlayHandler
{
    private static final ClientPlayHandler<CustomPayload> INSTANCE = new ClientPlayHandler<>();
    private final ArrayListMultimap<PayloadType, IPluginClientPlayHandler<T>> handlers = ArrayListMultimap.create();
    public static IClientPlayHandler getInstance()
    {
        return INSTANCE;
    }

    private ClientPlayHandler() { }

    @Override
    @SuppressWarnings("unchecked")
    public <P extends CustomPayload> void registerClientPlayHandler(IPluginClientPlayHandler<P> handler)
    {
        PayloadType type = handler.getPayloadType();

        if (type.exists(type))
        {
            if (!this.handlers.containsEntry(type, handler))
            {
                this.handlers.put(type, (IPluginClientPlayHandler<T>) handler);
                handler.registerPlayPayload(type);
                //handler.registerConfigPayload(type);
                // Don't register Receivers until Server/World fully joined.
            }
        }
    }

    @Override
    public <P extends CustomPayload> void unregisterClientPlayHandler(IPluginClientPlayHandler<P> handler)
    {
        PayloadType type = handler.getPayloadType();

        if (type.exists(type))
        {
            if (this.handlers.remove(type, handler))
            {
                handler.unregisterPlayHandler(type);
                //handler.unregisterConfigHandler(type);
            }
        }
    }

    /**
     * LOCAL API CALLS DO NOT USE ANYWHERE ELSE (DANGEROUS!)
     */
    public void reset(PayloadType type)
    {
        if (!this.handlers.isEmpty())
        {
            for (IPluginClientPlayHandler<T> handler : this.handlers.get(type))
            {
                handler.reset(type);
            }
        }
    }
   public void registerPlayPayload(PayloadType type)
   {
       if (!this.handlers.isEmpty())
       {
           for (IPluginClientPlayHandler<T> handler : this.handlers.get(type))
           {
               handler.registerPlayPayload(type);
           }
       }
   }
    public void registerPlayHandler(PayloadType type)
    {
        if (!this.handlers.isEmpty())
        {
            for (IPluginClientPlayHandler<T> handler : this.handlers.get(type))
            {
                handler.registerPlayHandler(type);
            }
        }
    }
    public void unregisterPlayHandler(PayloadType type)
    {
        if (!this.handlers.isEmpty())
        {
            for (IPluginClientPlayHandler<T> handler : this.handlers.get(type))
            {
                handler.unregisterPlayHandler(type);
            }
        }
    }
   public <P extends CustomPayload> void receiveS2CPlayPayload(PayloadType type, P payload, ClientPlayNetworking.Context ctx)
   {
       if (!this.handlers.isEmpty())
       {
           for (IPluginClientPlayHandler<T> handler : this.handlers.get(type))
           {
               handler.receiveS2CPlayPayload(type, payload, ctx);
           }
       }
   }
    public <P extends CustomPayload> void receiveS2CPlayPayload(PayloadType type, P payload, ClientPlayNetworkHandler networkHandler, CallbackInfo ci)
    {
        if (!this.handlers.isEmpty())
        {
            for (IPluginClientPlayHandler<T> handler : this.handlers.get(type))
            {
                handler.receiveS2CPlayPayload(type, payload, networkHandler, ci);
            }
        }
    }
   public void decodeS2CNbtCompound(PayloadType type, NbtCompound data)
   {
       if (!this.handlers.isEmpty())
       {
           for (IPluginClientPlayHandler<T> handler : this.handlers.get(type))
           {
               handler.decodeS2CNbtCompound(type, data);
           }
       }
   }
    public void decodeS2CByteBuf(PayloadType type, MaLibByteBuf data)
    {
        if (!this.handlers.isEmpty())
        {
            for (IPluginClientPlayHandler<T> handler : this.handlers.get(type))
            {
                handler.decodeS2CByteBuf(type, data);
            }
        }
    }
}
