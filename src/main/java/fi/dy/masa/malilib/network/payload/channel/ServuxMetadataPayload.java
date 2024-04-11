package fi.dy.masa.malilib.network.payload.channel;

import fi.dy.masa.malilib.network.payload.PayloadManager;
import fi.dy.masa.malilib.network.payload.PayloadType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * Intended as a new future Servux Data Provider for providing server-based Metadata / Rules (I.e, SPAWN_CHUNK_RADIUS, etc)
 */
public record ServuxMetadataPayload(NbtCompound data) implements CustomPayload
{
    public static final Id<ServuxMetadataPayload> TYPE = new Id<>(PayloadManager.INSTANCE.getIdentifier(PayloadType.SERVUX_METADATA));
    public static final PacketCodec<PacketByteBuf, ServuxMetadataPayload> CODEC = CustomPayload.codecOf(ServuxMetadataPayload::write, ServuxMetadataPayload::new);
    public static final String KEY = PayloadManager.INSTANCE.getKey(PayloadType.SERVUX_METADATA);

    public ServuxMetadataPayload(PacketByteBuf buf) { this(buf.readNbt()); }

    private void write(PacketByteBuf buf) { buf.writeNbt(data); }

    @Override
    public Id<? extends CustomPayload> getId() { return TYPE; }
}
