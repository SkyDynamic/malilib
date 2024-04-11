package fi.dy.masa.malilib.network.payload.channel;

import fi.dy.masa.malilib.network.payload.PayloadManager;
import fi.dy.masa.malilib.network.payload.PayloadType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * Intended as a new future Servux Data Provider for sending Entity NBT data (i.e., Mob Health Information, etc.)
 */
public record ServuxEntitiesPayload(NbtCompound data) implements CustomPayload
{
    public static final Id<ServuxEntitiesPayload> TYPE = new Id<>(PayloadManager.INSTANCE.getIdentifier(PayloadType.SERVUX_ENTITIES));
    public static final PacketCodec<PacketByteBuf, ServuxEntitiesPayload> CODEC = CustomPayload.codecOf(ServuxEntitiesPayload::write, ServuxEntitiesPayload::new);
    public static final String KEY = PayloadManager.INSTANCE.getKey(PayloadType.SERVUX_ENTITIES);

    public ServuxEntitiesPayload(PacketByteBuf buf) { this(buf.readNbt()); }

    private void write(PacketByteBuf buf) { buf.writeNbt(data); }

    @Override
    public Id<? extends CustomPayload> getId() { return TYPE; }
}
