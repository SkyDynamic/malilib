package fi.dy.masa.malilib.network.payload.channel;

import fi.dy.masa.malilib.network.payload.PayloadManager;
import fi.dy.masa.malilib.network.payload.PayloadType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * Used for handling Mainline Carpet Mod's NBT packet channel
 */
public record CarpetHelloPayload(NbtCompound data) implements CustomPayload
{
    public static final Id<CarpetHelloPayload> TYPE = new Id<>(PayloadManager.INSTANCE.getIdentifier(PayloadType.CARPET_HELLO));
    public static final PacketCodec<PacketByteBuf, CarpetHelloPayload> CODEC = CustomPayload.codecOf(CarpetHelloPayload::write, CarpetHelloPayload::new);
    public static final String KEY = PayloadManager.INSTANCE.getKey(PayloadType.CARPET_HELLO);

    public CarpetHelloPayload(PacketByteBuf buf) { this(buf.readNbt()); }
    private void write(PacketByteBuf buf) { buf.writeNbt(data); }
    @Override
    public Id<? extends CustomPayload> getId() { return TYPE; }
}
