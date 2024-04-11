package fi.dy.masa.malilib.network.payload.channel;

import fi.dy.masa.malilib.network.payload.PayloadType;
import fi.dy.masa.malilib.network.payload.PayloadManager;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * The functional Production Servux Structures Data Provider.
 */
public record ServuxStructuresPayload(NbtCompound data) implements CustomPayload
{
    public static final Id<ServuxStructuresPayload> TYPE = new Id<>(PayloadManager.INSTANCE.getIdentifier(PayloadType.SERVUX_STRUCTURES));
    public static final PacketCodec<PacketByteBuf, ServuxStructuresPayload> CODEC = CustomPayload.codecOf(ServuxStructuresPayload::write, ServuxStructuresPayload::new);
    public static final String KEY = PayloadManager.INSTANCE.getKey(PayloadType.SERVUX_STRUCTURES);

    public ServuxStructuresPayload(PacketByteBuf buf) { this(buf.readNbt()); }

    private void write(PacketByteBuf buf) { buf.writeNbt(data); }

    @Override
    public Id<? extends CustomPayload> getId() { return TYPE; }
}
