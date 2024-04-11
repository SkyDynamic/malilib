package fi.dy.masa.malilib.network.payload.channel;

import fi.dy.masa.malilib.network.payload.MaLibByteBuf;
import fi.dy.masa.malilib.network.payload.PayloadManager;
import fi.dy.masa.malilib.network.payload.PayloadType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/**
 * Example Payload Type for extending a PacketByteBuf
 */
public record MaLibBufPayload(MaLibByteBuf byteBuf) implements CustomPayload
{
    public static final Id<MaLibBufPayload> TYPE = new Id<>(PayloadManager.INSTANCE.getIdentifier(PayloadType.MALILIB_BYTEBUF));
    public static final PacketCodec<PacketByteBuf, MaLibBufPayload> CODEC = CustomPayload.codecOf(MaLibBufPayload::write, MaLibBufPayload::new);

    public MaLibBufPayload(PacketByteBuf input)
    {
        this(new MaLibByteBuf(input.readBytes(input.readableBytes())));
    }

    private void write(PacketByteBuf output)
    {
        output.writeBytes(byteBuf);
    }

    @Override
    public Id<? extends CustomPayload> getId()
    {
        return TYPE;
    }
}
